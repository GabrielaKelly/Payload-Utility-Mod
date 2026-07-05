package net.payload.module.modules.world;

import com.google.common.collect.Lists;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;
import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.*;
import net.payload.utils.block.BlockIterator;
import net.payload.utils.block.BlockUtils;
import net.payload.utils.block.LitematicaUtils;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.player.PlayerUtils;
import net.payload.utils.render.Render3D;

import java.util.*;
import java.util.function.Supplier;

public class LitematicaPrinter extends Module implements TickListener, Render3DListener {

	// Setting Groups
	private final SettingGroup generalSettings = SettingGroup.Builder.builder()
			.id("litematica_general")
			.displayName("General")
			.description("General printer settings")
			.build();

	private final SettingGroup blacklistSettings = SettingGroup.Builder.builder()
			.id("litematica_blacklist")
			.displayName("Blacklist")
			.description("Block blacklist settings")
			.build();

	private final SettingGroup renderingSettings = SettingGroup.Builder.builder()
			.id("litematica_rendering")
			.displayName("Rendering")
			.description("Visual settings for the printer")
			.build();

	// General Settings
	private final FloatSetting printingRange = FloatSetting.builder()
			.id("litematica_printingRange")
			.displayName("Range")
			.description("The block place range")
			.defaultValue(3f)
			.minValue(1f)
			.maxValue(6f)
			.step(1f)
			.build();

	private final FloatSetting printingDelay = FloatSetting.builder()
			.id("litematica_printing_delay")
			.displayName("Tick Delay")
			.description("Delay between printing blocks in ticks")
			.defaultValue(2f)
			.minValue(0f)
			.maxValue(50f)
			.step(0.5f)
			.build();

	private final FloatSetting blocksPerTick = FloatSetting.builder()
			.id("litematica_blocks_per_tick")
			.displayName("Blocks/Tick")
			.description("How many blocks place per tick")
			.defaultValue(1f)
			.minValue(1f)
			.maxValue(40f)
			.step(1f)
			.build();

	private final BooleanSetting advanced = BooleanSetting.builder()
			.id("litematica_advanced")
			.displayName("Advanced")
			.description("Checks for block angles before placing")
			.defaultValue(false)
			.build();

	private final BooleanSetting airPlace = BooleanSetting.builder()
			.id("litematica_air_place")
			.displayName("Air Place")
			.description("Allow placement in the air")
			.defaultValue(false)
			.build();

	private final BooleanSetting belowOnly = BooleanSetting.builder()
			.id("litematica_below")
			.displayName("Below Only")
			.description("Only place blocks below your feet")
			.defaultValue(false)
			.build();

	private final BooleanSetting clickPlace = BooleanSetting.builder()
			.id("litematica_click_place")
			.displayName("Click Place")
			.description("Good for fixing some bugs sometimes")
			.defaultValue(false)
			.build();

	private final BooleanSetting visibleCheck = BooleanSetting.builder()
			.id("litematica_sidecheck")
			.displayName("Visible Check")
			.description("Checks to see if you can place blocks")
			.defaultValue(true)
			.build();

	private final BooleanSetting swing = BooleanSetting.builder()
			.id("litematica_swing")
			.displayName("Swing")
			.description("Swing hand when placing")
			.defaultValue(false)
			.build();

	private final BooleanSetting returnHand = BooleanSetting.builder()
			.id("litematica_return_slot")
			.displayName("Return Slot")
			.description("Return to old slot")
			.defaultValue(false)
			.build();

	private final BooleanSetting rotate = BooleanSetting.builder()
			.id("litematica_rotate")
			.displayName("Rotate")
			.description("Rotate to the blocks being placed")
			.defaultValue(false)
			.build();

	private final BooleanSetting dirtGrass = BooleanSetting.builder()
			.id("litematica_dirt_as_grass")
			.displayName("Dirt As Grass")
			.description("Use dirt instead of grass")
			.defaultValue(true)
			.build();

	private final EnumSetting<SortAlgorithm> firstAlgorithm = EnumSetting.<SortAlgorithm>builder()
			.id("litematica_first_sorting_mode")
			.displayName("First Priority")
			.description("The blocks you want to place first")
			.defaultValue(SortAlgorithm.None)
			.build();

	private final EnumSetting<SortingSecond> secondAlgorithm = EnumSetting.<SortingSecond>builder()
			.id("litematica_second_sorting_mode")
			.displayName("Backup Priority")
			.description("Second pass of sorting eg. place first blocks higher and closest to you")
			.defaultValue(SortingSecond.None)
			.build();

	// Whitelist Settings
	private final BooleanSetting blacklistToggle = BooleanSetting.builder()
			.id("litematica_blacklist_toggle")
			.displayName("Use Blacklist")
			.description("Toggles the blacklist")
			.defaultValue(true)
			.build();

	private final BlocksSetting blacklist = BlocksSetting.builder()
			.id("litematica_blacklist")
			.displayName("Blacklist")
			.description("Blocks to never place")
			.defaultValue(new HashSet<Block>(Lists.newArrayList()))
			.build();

	// Rendering Settings
	private final BooleanSetting renderBlocks = BooleanSetting.builder()
			.id("litematica_render_placed_blocks")
			.displayName("Render")
			.description("Renders block placements")
			.defaultValue(true)
			.build();

	private final FloatSetting fadeTime = FloatSetting.builder()
			.id("litematica_fade_time")
			.displayName("Unload Ticks")
			.description("Time for the rendering to fade, in ticks")
			.defaultValue(3f)
			.minValue(1f)
			.maxValue(100f)
			.step(1f)
			.build();

	private final ColorSetting color = ColorSetting.builder()
			.id("litematica_color")
			.displayName("Color")
			.description("The cubes color")
			.defaultValue(new Color(255, 95, 100))
			.build();

	private final BooleanSetting mapArtMode = BooleanSetting.builder()
			.id("litematica_mapart")
			.displayName("Mapart Mode")
			.description("Moves like a snake for mapart")
			.defaultValue(false)
			.build();

	public LitematicaPrinter() {
		super("LitematicaPrinter");
		this.setCategory(Category.of("World"));
		this.setDescription("Places loaded litematica configs to autobuild whatever you want");

		// Add settings to general group
		generalSettings.addSetting(printingRange);
		generalSettings.addSetting(printingDelay);
		generalSettings.addSetting(blocksPerTick);
		generalSettings.addSetting(advanced);
		generalSettings.addSetting(visibleCheck);
		generalSettings.addSetting(belowOnly);
		generalSettings.addSetting(airPlace);
		generalSettings.addSetting(clickPlace);
		generalSettings.addSetting(swing);
		generalSettings.addSetting(returnHand);
		generalSettings.addSetting(rotate);
		generalSettings.addSetting(dirtGrass);
		generalSettings.addSetting(firstAlgorithm);
		generalSettings.addSetting(secondAlgorithm);

		// Add settings to whitelist group
		blacklistSettings.addSetting(blacklistToggle);
		blacklistSettings.addSetting(blacklist);

		// Add settings to rendering group
		renderingSettings.addSetting(renderBlocks);
		renderingSettings.addSetting(fadeTime);
		renderingSettings.addSetting(color);

		this.addSetting(mapArtMode);

		// Add groups to module
		this.addSetting(generalSettings);
		this.addSetting(blacklistSettings);
		this.addSetting(renderingSettings);

		// Register settings with SettingManager
		SettingManager.registerSetting(generalSettings);
		SettingManager.registerSetting(printingRange);
		SettingManager.registerSetting(printingDelay);
		SettingManager.registerSetting(blocksPerTick);
		SettingManager.registerSetting(advanced);
		SettingManager.registerSetting(visibleCheck);
		SettingManager.registerSetting(airPlace);
		SettingManager.registerSetting(swing);
		SettingManager.registerSetting(returnHand);
		SettingManager.registerSetting(rotate);
		SettingManager.registerSetting(dirtGrass);
		SettingManager.registerSetting(firstAlgorithm);
		SettingManager.registerSetting(secondAlgorithm);

		SettingManager.registerSetting(blacklistSettings);
		SettingManager.registerSetting(blacklistToggle);
		SettingManager.registerSetting(blacklist);

		SettingManager.registerSetting(renderingSettings);
		SettingManager.registerSetting(renderBlocks);
		SettingManager.registerSetting(fadeTime);
		SettingManager.registerSetting(color);
	}

	private int timer;
	private int usedSlot = -1;
	private final List<BlockPos> toSort = new ArrayList<>();
	private final List<Pair<Integer, BlockPos>> placed_fade = new ArrayList<>();
	MinecraftClient mc = MinecraftClient.getInstance();

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
	}

	@Override
	public void onEnable() {

		try {
			// Try to access a Litematica class to check if it's available
			Class.forName("fi.dy.masa.litematica.world.SchematicWorldHandler");
			Class.forName("fi.dy.masa.litematica.world.WorldSchematic");
			Class.forName("fi.dy.masa.litematica.data.DataManager");

			// If the class is found, proceed with normal initialization
			Payload.getInstance().eventManager.AddListener(TickListener.class, this);
			Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);

		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			// Handle the missing dependency
			handleMissingLitematica();
		}
	}

	private void handleMissingLitematica() {
		// Disable the module
		this.state.setValue(false);

		// Log detailed error information
		System.out.println("Failed to enable LitematicaPrinter: Litematica is not installed");
		System.out.println("This module requires Litematica mod to function properly");
		System.out.println("Please install Litematica to use this feature");

		// Display a visible notification to the user
		if (MC.player != null) {
			sendErrorMessage("LitematicaPrinter requires the Litematica mod to be installed.");
		}
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(TickEvent.Pre event) {

	}

	@Override
	public void onTick(TickEvent.Post event) {
		if (mc.player == null || mc.world == null) {
			placed_fade.clear();
			this.state.setValue(false);
			return;
		}

		placed_fade.forEach(s -> s.setLeft(s.getLeft() - 1));
		placed_fade.removeIf(s -> s.getLeft() <= 0);

		WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
		if (worldSchematic == null) {
			placed_fade.clear();
			toggle();
			return;
		}

		toSort.clear();


		if (timer >= printingDelay.get()) {
			BlockIterator.register(Math.round(printingRange.get() + 1), Math.round(printingRange.get() + 1), (pos, blockState) -> {
				BlockState required = worldSchematic.getBlockState(pos);

				if (
						mc.player.getBlockPos().isWithinDistance(pos, Math.round(printingRange.get()))
								&& blockState.isReplaceable()
								&& !required.isLiquid()
								&& !required.isAir()
								&& blockState.getBlock() != required.getBlock()
								&& DataManager.getRenderLayerRange().isPositionWithinRange(pos)
								&& !mc.player.getBoundingBox().intersects(Vec3d.of(pos), Vec3d.of(pos).add(1, 1, 1))
								&& required.canPlaceAt(mc.world, pos)
				) {

					if (visibleCheck.getValue()) {
						boolean isBlockInLineOfSight = LitematicaUtils.isBlockInLineOfSight(pos, required);
						if (
								airPlace.get() && isBlockInLineOfSight || !airPlace.get() && BlockUtils.getPlaceSide(pos) != null && isBlockInLineOfSight
						) {
							if (blacklistToggle.getValue() && blacklist.get().contains(required.getBlock())) {
								return;
							}

							toSort.add(new BlockPos(pos));
						}
					} else {
						if (
								airPlace.get() || !airPlace.get() && BlockUtils.getPlaceSide(pos) != null
						) {
							if (blacklistToggle.getValue() && blacklist.get().contains(required.getBlock())) {
								return;
							}

							toSort.add(new BlockPos(pos));
						}
					}
				}
			});

			BlockIterator.after(() -> {
				if (firstAlgorithm.get() != SortAlgorithm.None) {
					if (firstAlgorithm.get().applySecondSorting) {
						if (secondAlgorithm.get() != SortingSecond.None) {
							toSort.sort(secondAlgorithm.get().algorithm);
						}
					}
					toSort.sort(firstAlgorithm.get().algorithm);
				}


				int placed = 0;
				for (BlockPos pos : toSort) {

					BlockState state = worldSchematic.getBlockState(pos);
					Item item = state.getBlock().asItem();

					if (dirtGrass.get() && item == Items.GRASS_BLOCK)
						item = Items.DIRT;

					// replace block states here

					if ((pos.getY() > MC.player.getY() - 1) && belowOnly.get()) {
						return;
					}

					if (switchItem(item, state, () -> place(state, pos))) {
						timer = 0;
						placed++;
						if (renderBlocks.get()) {
							placed_fade.add(new Pair<>(Math.round(fadeTime.get()), new BlockPos(pos)));
						}
						if (placed >= blocksPerTick.get()) {
							return;
						}
					}
				}
			});


		} else timer++;
	}

	@Override
	public void onRender(Render3DEvent event) {
		placed_fade.forEach(s -> {
			float fadeRatio = 1.0f - ((float)s.getLeft() / fadeTime.get());
			Color a = new Color(color.get().getRed(), color.get().getGreen(), color.get().getBlue(), (int)(fadeRatio * color.get().getReverseAlphaInt()));
			Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), new Box(s.getRight()), a, 1.0f);
		});
	}

	@SuppressWarnings("unused")
	public enum SortAlgorithm {
		None(false, (a, b) -> 0),
		TopDown(true, Comparator.comparingInt(value -> value.getY() * -1)),
		DownTop(true, Comparator.comparingInt(Vec3i::getY)),
		Nearest(false, Comparator.comparingDouble(value -> MC.player != null ? PlayerUtils.squaredDistance(MC.player.getX(), MC.player.getY(), MC.player.getZ(), value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5) : 0)),
		Furthest(false, Comparator.comparingDouble(value -> MC.player != null ? (PlayerUtils.squaredDistance(MC.player.getX(), MC.player.getY(), MC.player.getZ(), value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5)) * -1 : 0));


		final boolean applySecondSorting;
		final Comparator<BlockPos> algorithm;

		SortAlgorithm(boolean applySecondSorting, Comparator<BlockPos> algorithm) {
			this.applySecondSorting = applySecondSorting;
			this.algorithm = algorithm;
		}
	}

	@SuppressWarnings("unused")
	public enum SortingSecond {
		None(SortAlgorithm.None.algorithm),
		Nearest(SortAlgorithm.Nearest.algorithm),
		Furthest(SortAlgorithm.Furthest.algorithm);

		final Comparator<BlockPos> algorithm;

		SortingSecond(Comparator<BlockPos> algorithm) {
			this.algorithm = algorithm;
		}
	}

	public boolean place(BlockState required, BlockPos pos) {

		if (mc.player == null || mc.world == null) return false;
		if (!mc.world.getBlockState(pos).isReplaceable()) return false;

		Direction wantedSide = advanced.get() ? dir(required) : null;
		SlabType wantedSlabType = advanced.get() && required.contains(Properties.SLAB_TYPE) ? required.get(Properties.SLAB_TYPE) : null;
		BlockHalf wantedBlockHalf = advanced.get() && required.contains(Properties.BLOCK_HALF) ? required.get(Properties.BLOCK_HALF) : null;
		Direction wantedHorizontalOrientation = advanced.get() && required.contains(Properties.HORIZONTAL_FACING) ? required.get(Properties.HORIZONTAL_FACING) : null;
		Direction.Axis wantedAxies = advanced.get() && required.contains(Properties.AXIS) ? required.get(Properties.AXIS) : null;
		Direction wantedHopperOrientation = advanced.get() && required.contains(Properties.HOPPER_FACING) ? required.get(Properties.HOPPER_FACING) : null;

		Direction placeSide = LitematicaUtils.getPlaceSide(
				pos,
				required,
				wantedSlabType,
				wantedBlockHalf,
				wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation,
				wantedAxies,
				wantedSide);

		if (airPlace.getValue()) {
			OtherBlockUtils.airPlaceBlock(pos, rotate.getValue());
			return true;
		}
		else if (clickPlace.get()) {
			OtherBlockUtils.clickBlock(pos, placeSide, rotate.getValue(), Hand.MAIN_HAND, Payload.getInstance().moduleManager.antiCheat.packetPlace.getValue());
			return true;
		}
		else {
			return LitematicaUtils.place(pos, placeSide, wantedSlabType, wantedBlockHalf, wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation, wantedAxies, airPlace.get(), swing.get(), rotate.get(), Math.round(printingRange.get()));
		}
	}

	/**
	 * Attempts to switch to a specific item and perform an action with it.
	 * Returns the item to the original slot if the action fails.
	 *
	 * @param item The item to switch to
	 * @param state The block state required (for compatibility checking)
	 * @param action The action to perform with the item
	 * @return true if the item was successfully used, false otherwise
	 */
	private boolean switchItem(Item item, BlockState state, Supplier<Boolean> action) {
		if (MC.player == null) return false;

		int selectedSlot = MC.player.getInventory().selectedSlot;
		boolean isCreative = MC.player.getAbilities().creativeMode;

		// Case 1: Already holding the correct item
		if (MC.player.getMainHandStack().getItem() == item) {
			if (action.get()) {
				usedSlot = MC.player.getInventory().selectedSlot;
				return true;
			}
			return false;
		}

		// Case 2: The item was previously used and is still in the same slot
		if (usedSlot != -1 && MC.player.getInventory().getStack(usedSlot).getItem() == item) {
			int originalSlot = MC.player.getInventory().selectedSlot;
			InventoryUtil.switchToSlot(usedSlot);

			if (action.get()) {
				return true;
			} else {
				InventoryUtil.switchToSlot(originalSlot);
				return false;
			}
		}

		// Case 3: Find the item in hotbar
		int hotbarSlot = InventoryUtil.findItem(item);
		if (hotbarSlot != -1) {
			int originalSlot = MC.player.getInventory().selectedSlot;
			InventoryUtil.switchToSlot(hotbarSlot);

			if (action.get()) {
				usedSlot = hotbarSlot;
				return true;
			} else {
				InventoryUtil.switchToSlot(originalSlot);
				return false;
			}
		}

		// Case 4: Find the item in inventory and move it to hotbar
		int inventorySlot = InventoryUtil.findItemInventorySlot(item);
		if (inventorySlot != -1) {
			// Find an empty hotbar slot or use the last used slot
			int targetHotbarSlot = -1;

			// First try to find an empty slot in hotbar
			for (int i = 0; i < 9; i++) {
				if (MC.player.getInventory().getStack(i).isEmpty()) {
					targetHotbarSlot = i;
					break;
				}
			}

			// If no empty slot found, use the usedSlot if available
			if (targetHotbarSlot == -1 && usedSlot != -1) {
				targetHotbarSlot = usedSlot;
			}

			// If we have a target slot, move the item there and use it
			if (targetHotbarSlot != -1) {
				int originalSlot = MC.player.getInventory().selectedSlot;

				// Move item from inventory to hotbar
				InventoryUtil.inventorySwap(inventorySlot, targetHotbarSlot);
				InventoryUtil.switchToSlot(targetHotbarSlot);

				// Ensure client and server are in sync
				InventoryUtil.syncToClient();

				if (action.get()) {
					usedSlot = targetHotbarSlot;
					return true;
				} else {
					InventoryUtil.switchToSlot(originalSlot);
					return false;
				}
			}
		}

		// Case 5: Creative mode - create the item
		if (isCreative) {
			// Find an empty slot in hotbar or use slot 0
			int emptySlot = 0;
			for (int i = 0; i < 9; i++) {
				if (MC.player.getInventory().getStack(i).isEmpty()) {
					emptySlot = i;
					break;
				}
			}

			// Create the item in the selected slot
			MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + emptySlot, item.getDefaultStack()));

			// Switch to the slot and perform action
			int originalSlot = MC.player.getInventory().selectedSlot;
			InventoryUtil.switchToSlot(emptySlot);

			if (action.get()) {
				usedSlot = emptySlot;
				return true;
			} else {
				InventoryUtil.switchToSlot(originalSlot);
				return false;
			}
		}

		// No suitable item found
		return false;
	}

	private Direction dir(BlockState state) {
		if (state.contains(Properties.FACING)) return state.get(Properties.FACING);
		else if (state.contains(Properties.AXIS)) return Direction.from(state.get(Properties.AXIS), Direction.AxisDirection.POSITIVE);
		else if (state.contains(Properties.HORIZONTAL_AXIS)) return Direction.from(state.get(Properties.HORIZONTAL_AXIS), Direction.AxisDirection.POSITIVE);
		else return Direction.UP;
	}
}
