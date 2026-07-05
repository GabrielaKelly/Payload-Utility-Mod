
package net.payload.module.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.*;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.MovementUtil;
import net.payload.utils.player.combat.CombatUtil;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.render.Render3D;

public class SelfTrap extends Module implements EntityRemoveListener, GameLeftListener, TickListener, Render3DListener, PlayerMoveEventListener, SendMovementPacketListener {

	private final SettingGroup generalSettings;
	private final SettingGroup placementSettings;
	private final SettingGroup behaviorSettings;
	private final SettingGroup renderSettings;

	private final FloatSetting placeDelay = FloatSetting.builder()
			.id("selftrap_place_delay")
			.displayName("Place Delay")
			.description("Delay between block placements in milliseconds.")
			.defaultValue(50f)
			.minValue(0f)
			.maxValue(500f)
			.step(1f)
			.build();

	private final FloatSetting blocksPer = FloatSetting.builder()
			.id("selftrap_blocks_per")
			.displayName("Blocks Per")
			.description("Number of blocks to place per tick.")
			.defaultValue(1f)
			.minValue(1f)
			.maxValue(8f)
			.step(1f)
			.build();

	private final BooleanSetting detectMining = BooleanSetting.builder()
			.id("selftrap_detect_mining")
			.displayName("Detect Mining")
			.description("Detects when someone is mining your trap blocks.")
			.defaultValue(false)
			.build();

	private final BooleanSetting onlyTick = BooleanSetting.builder()
			.id("selftrap_only_tick")
			.displayName("Only Tick")
			.description("Only places blocks on game ticks.")
			.defaultValue(true)
			.build();

	private final BooleanSetting rotate = BooleanSetting.builder()
			.id("selftrap_rotate")
			.displayName("Rotate")
			.description("Rotates player view when placing blocks.")
			.defaultValue(true)
			.build();

	private final BooleanSetting packetPlace = BooleanSetting.builder()
			.id("selftrap_packet_place")
			.displayName("Packet Place")
			.description("Uses packets to place blocks instead of normal placement.")
			.defaultValue(true)
			.build();

	private final BooleanSetting breakCrystal = BooleanSetting.builder()
			.id("selftrap_break")
			.displayName("Break")
			.description("Breaks crystals that would prevent block placement.")
			.defaultValue(true)
			.build();

	private final BooleanSetting eatPause = BooleanSetting.builder()
			.id("selftrap_eating_pause")
			.displayName("Eating Pause")
			.description("Pauses the module while eating.")
			.defaultValue(true)
			.build();

	private final BooleanSetting usingPause = BooleanSetting.builder()
			.id("selftrap_using_pause")
			.displayName("Using Pause")
			.description("Pauses the module while using items.")
			.defaultValue(true)
			.build();

	private final BooleanSetting center = BooleanSetting.builder()
			.id("selftrap_center")
			.displayName("Center")
			.description("Centers player on block before trapping.")
			.defaultValue(true)
			.build();

	public final BooleanSetting extend = BooleanSetting.builder()
			.id("selftrap_extend")
			.displayName("Extend")
			.description("Extends the trap to nearby blocks.")
			.defaultValue(true)
			.build();

	private final BooleanSetting inventory = BooleanSetting.builder()
			.id("selftrap_inventory_swap")
			.displayName("Inventory Swap")
			.description("Allows swapping to blocks from inventory.")
			.defaultValue(true)
			.build();

	public final BooleanSetting inAir = BooleanSetting.builder()
			.id("selftrap_in_air")
			.displayName("In Air")
			.description("Allows self-trap to work while in air.")
			.defaultValue(true)
			.build();

	private final BooleanSetting moveDisable = BooleanSetting.builder()
			.id("selftrap_auto_disable")
			.displayName("Auto Disable")
			.description("Automatically disables the module when moving.")
			.defaultValue(true)
			.build();

	private final BooleanSetting jumpDisable = BooleanSetting.builder()
			.id("selftrap_jump_disable")
			.displayName("Jump Disable")
			.description("Automatically disables the module when jumping.")
			.defaultValue(true)
			.build();

	private final BooleanSetting enderChest = BooleanSetting.builder()
			.id("selftrap_ender_chest")
			.displayName("Ender Chest")
			.description("Uses ender chests for trapping if available.")
			.defaultValue(true)
			.build();

	private final BooleanSetting head = BooleanSetting.builder()
			.id("selftrap_head")
			.displayName("Head")
			.description("Places blocks at head level.")
			.defaultValue(true)
			.build();

	private final BooleanSetting feet = BooleanSetting.builder()
			.id("selftrap_feet")
			.displayName("Feet")
			.description("Places blocks at feet level.")
			.defaultValue(true)
			.build();

	private final BooleanSetting chest = BooleanSetting.builder()
			.id("selftrap_chest")
			.displayName("Chest")
			.description("Places blocks at chest level.")
			.defaultValue(true)
			.build();

	private final BooleanSetting render = BooleanSetting.builder()
			.id("selftrap_render")
			.displayName("Render")
			.description("Renders placement positions")
			.defaultValue(true)
			.build();

	private final ColorSetting color = ColorSetting.builder()
			.id("selftrap_color")
			.displayName("Color")
			.description("Color of the render")
			.defaultValue(new Color(0, 255, 0, 75))
			.build();

	private final FloatSetting lineWidth = FloatSetting.builder()
			.id("selftrap_line_width")
			.displayName("Line Width")
			.description("Width of outline lines")
			.defaultValue(1.5f)
			.minValue(0.1f)
			.maxValue(5f)
			.step(0.1f)
			.build();

	public SelfTrap() {
		super("SelfTrap");

		this.setCategory(Category.of("Combat"));
		this.setDescription("Self traps you to prevent CPVP faceplacing");

		// Initialize setting groups
		generalSettings = SettingGroup.Builder.builder()
				.id("selftrap_general")
				.displayName("General")
				.description("General self trap settings")
				.build();

		placementSettings = SettingGroup.Builder.builder()
				.id("selftrap_placement")
				.displayName("Placement")
				.description("Block placement settings")
				.build();

		behaviorSettings = SettingGroup.Builder.builder()
				.id("selftrap_behavior")
				.displayName("Behavior")
				.description("Module behavior settings")
				.build();

		renderSettings = SettingGroup.Builder.builder()
				.id("selftrap_render_group")
				.displayName("Render")
				.description("Visual settings")
				.build();

		// Add settings to groups
		generalSettings.addSetting(placeDelay);
		generalSettings.addSetting(blocksPer);
		generalSettings.addSetting(onlyTick);
		generalSettings.addSetting(rotate);
		generalSettings.addSetting(packetPlace);

		placementSettings.addSetting(breakCrystal);
		placementSettings.addSetting(center);
		placementSettings.addSetting(extend);
		placementSettings.addSetting(inventory);
		placementSettings.addSetting(enderChest);
		placementSettings.addSetting(head);
		placementSettings.addSetting(feet);
		placementSettings.addSetting(chest);

		behaviorSettings.addSetting(detectMining);
		behaviorSettings.addSetting(eatPause);
		behaviorSettings.addSetting(usingPause);
		behaviorSettings.addSetting(inAir);
		behaviorSettings.addSetting(moveDisable);
		behaviorSettings.addSetting(jumpDisable);

		renderSettings.addSetting(render);
		renderSettings.addSetting(color);
		renderSettings.addSetting(lineWidth);

		// Add groups to module
		this.addSetting(generalSettings);
		this.addSetting(placementSettings);
		this.addSetting(behaviorSettings);
		this.addSetting(renderSettings);

		// Register settings with SettingManager
		SettingManager.registerSetting(generalSettings);
		SettingManager.registerSetting(placeDelay);
		SettingManager.registerSetting(blocksPer);
		SettingManager.registerSetting(onlyTick);
		SettingManager.registerSetting(rotate);
		SettingManager.registerSetting(packetPlace);

		SettingManager.registerSetting(placementSettings);
		SettingManager.registerSetting(breakCrystal);
		SettingManager.registerSetting(center);
		SettingManager.registerSetting(extend);
		SettingManager.registerSetting(inventory);
		SettingManager.registerSetting(enderChest);
		SettingManager.registerSetting(head);
		SettingManager.registerSetting(feet);
		SettingManager.registerSetting(chest);

		SettingManager.registerSetting(behaviorSettings);
		SettingManager.registerSetting(detectMining);
		SettingManager.registerSetting(eatPause);
		SettingManager.registerSetting(usingPause);
		SettingManager.registerSetting(inAir);
		SettingManager.registerSetting(moveDisable);
		SettingManager.registerSetting(jumpDisable);

		SettingManager.registerSetting(renderSettings);
		SettingManager.registerSetting(render);
		SettingManager.registerSetting(color);
		SettingManager.registerSetting(lineWidth);
	}

	double startX = 0;
	double startY = 0;
	double startZ = 0;
	int progress = 0;
	private boolean shouldCenter = true;
	private final CacheTimer timer = new CacheTimer();

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(PlayerMoveEventListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(SendMovementPacketListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(EntityRemoveListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);

	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
		Payload.getInstance().eventManager.AddListener(PlayerMoveEventListener.class, this);
		Payload.getInstance().eventManager.AddListener(SendMovementPacketListener.class, this);
		Payload.getInstance().eventManager.AddListener(EntityRemoveListener.class, this);
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);

		if (nullCheck()) {
			if (moveDisable.getValue() || jumpDisable.getValue()) this.toggle();
			return;
		}
		startX = MC.player.getX();
		startY = MC.player.getY();
		startZ = MC.player.getZ();
		shouldCenter = true;
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onDelete(EntityRemoveEvent event) {
		if (MC.player != null && event.getEntity() == MC.player && this.state.getValue()) {
			this.toggle();
		}
	}

	@Override
	public void onGameLeft(GameLeftEvent event) {
		if (this.state.getValue()) {
			this.toggle();
		}
	}

	@Override
	public void onRender(Render3DEvent event) {
		if (!render.getValue() || nullCheck()) return;

		// Get player position
		BlockPos playerPos = EntityUtil.getPlayerPos(true);

		// Render blocks based on enabled settings
		if (feet.getValue()) {
			renderSurroundBlocks(event, playerPos);
		}

		if (chest.getValue()) {
			renderSurroundBlocks(event, playerPos.up());
		}

		if (head.getValue()) {
			renderPos(event, playerPos.up(2));
		}
	}

	/**
	 * Renders all surround blocks for a given base position
	 * @param event The render event
	 * @param pos The base position to render surround blocks for
	 */
	private void renderSurroundBlocks(Render3DEvent event, BlockPos pos) {
		// Render base surround positions
		for (Direction dir : Direction.values()) {
			if (dir == Direction.UP || dir == Direction.DOWN) continue;

			BlockPos offsetPos = pos.offset(dir);
			renderPos(event, offsetPos);

			// Render extended positions if enabled
			if (extend.getValue() && selfIntersectPos(offsetPos)) {
				for (Direction dir2 : Direction.values()) {
					if (dir2 == Direction.UP || dir2 == Direction.DOWN) continue;

					BlockPos offsetPos2 = offsetPos.offset(dir2);
					renderPos(event, offsetPos2);

					if (selfIntersectPos(offsetPos2)) {
						for (Direction dir3 : Direction.values()) {
							if (dir3 == Direction.UP || dir3 == Direction.DOWN) continue;
							BlockPos offsetPos3 = offsetPos2.offset(dir3);
							renderPos(event, offsetPos3);
						}
					}
				}
			}
		}
	}

	/**
	 * Renders a single block position with appropriate checks
	 * @param event The render event
	 * @param pos The position to render
	 */
	private void renderPos(Render3DEvent event, BlockPos pos) {
		if (pos == null) return;

		// Only render positions that can be placed on
		if (!OtherBlockUtils.canPlace(pos, 6, true)) return;

		// Skip positions that are being mined if detect mining is enabled
		if (detectMining.getValue() && Payload.getInstance().breakManager.isMining(pos)) return;

		// Create box for the block position
		Box box = new Box(
				pos.getX(), pos.getY(), pos.getZ(),
				pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
		);

		// Render filled box and outline
		Render3D.draw3DBox(event.GetMatrix(), event.getCamera(),box, color.getValue(), lineWidth.get());
	}
	
	@Override
	public void onTick(TickEvent.Pre event) {
		onUpdate();
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	@Override
	public void onMove(PlayerMoveEvent event) {
		if (nullCheck() || !center.getValue() || MC.player.isGliding()) {
			return;
		}

		BlockPos blockPos = EntityUtil.getPlayerPos(true);
		if (MC.player.getX() - blockPos.getX() - 0.5 <= 0.2 && MC.player.getX() - blockPos.getX() - 0.5 >= -0.2 && MC.player.getZ() - blockPos.getZ() - 0.5 <= 0.2 && MC.player.getZ() - 0.5 - blockPos.getZ() >= -0.2) {
			if (shouldCenter && (MC.player.isOnGround() || MovementUtil.isMoving())) {
				event.setX(0);
				event.setZ(0);
				shouldCenter = false;
			}
		} else {
			if (shouldCenter) {
				Vec3d centerPos = EntityUtil.getPlayerPos(true).toCenterPos();
				float rotation = getRotationTo(MC.player.getPos(), centerPos).x;
				float yawRad = rotation / 180.0f * 3.1415927f;
				double dist = MC.player.getPos().distanceTo(new Vec3d(centerPos.x, MC.player.getY(), centerPos.z));
				double cappedSpeed = Math.min(0.2873, dist);
				double x = -(float) Math.sin(yawRad) * cappedSpeed;
				double z = (float) Math.cos(yawRad) * cappedSpeed;
				event.setX(x);
				event.setZ(z);
			}
		}
	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
		if (!onlyTick.getValue()) {
			onUpdate();
		}
	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Post event) {

	}

	public void onUpdate() {
		if (!timer.passed(placeDelay.getValue())) return;
		progress = 0;
		if (!MovementUtil.isMoving() && !MC.options.jumpKey.isPressed()) {
			startX = MC.player.getX();
			startY = MC.player.getY();
			startZ = MC.player.getZ();
		}
		BlockPos pos = EntityUtil.getPlayerPos(true);

		double distanceToStart = MathHelper.sqrt((float) MC.player.squaredDistanceTo(startX, startY, startZ));

		if (getBlock() == -1) {
			sendErrorMessage("Missing Obsidian or EChests");
			this.toggle();
			return;
		}
		if ((moveDisable.getValue() && distanceToStart > 1.0 || jumpDisable.getValue() && Math.abs(startY - MC.player.getY()) > 0.5)) {
			this.toggle();
			return;
		}
		if (usingPause.getValue() && MC.player.isUsingItem()) {
			return;
		}

		if (!inAir.getValue() && !MC.player.isOnGround()) return;
		if (head.getValue()) {
			tryPlaceBlock(pos.up(2));
		}
		if (feet.getValue()) doSurround(pos);
		if (chest.getValue()) doSurround(pos.up());
	}

	private void doSurround(BlockPos pos) {
		for (Direction i : Direction.values()) {
			if (i == Direction.UP) continue;
			BlockPos offsetPos = pos.offset(i);
			if (OtherBlockUtils.getPlaceSide(offsetPos) != null) {
				tryPlaceBlock(offsetPos);
			} else if (OtherBlockUtils.canReplace(offsetPos)) {
				tryPlaceBlock(getHelperPos(offsetPos));
			}
			if (selfIntersectPos(offsetPos) && extend.getValue()) {
				for (Direction i2 : Direction.values()) {
					if (i2 == Direction.UP) continue;
					BlockPos offsetPos2 = offsetPos.offset(i2);
					if (selfIntersectPos(offsetPos2)) {
						for (Direction i3 : Direction.values()) {
							if (i3 == Direction.UP) continue;
							tryPlaceBlock(offsetPos2);
							BlockPos offsetPos3 = offsetPos2.offset(i3);
							tryPlaceBlock(OtherBlockUtils.getPlaceSide(offsetPos3) != null || !OtherBlockUtils.canReplace(offsetPos3) ? offsetPos3 : getHelperPos(offsetPos3));
						}
					}
					tryPlaceBlock(OtherBlockUtils.getPlaceSide(offsetPos2) != null || !OtherBlockUtils.canReplace(offsetPos2) ? offsetPos2 : getHelperPos(offsetPos2));
				}
			}
		}
	}
	private void tryPlaceBlock(BlockPos pos) {
		if (pos == null) return;
		if (detectMining.getValue() && Payload.getInstance().breakManager.isMining(pos)) return;
		if (!(progress < blocksPer.getValue())) return;
		int block = getBlock();
		if (block == -1) return;

		if (!OtherBlockUtils.canPlace(pos, 6, true)) return;
		if (breakCrystal.getValue()) {
			CombatUtil.attackCrystal(pos, rotate.getValue(), eatPause.getValue());
		} else if (OtherBlockUtils.hasEntity(pos, false)) return;
		int old = MC.player.getInventory().selectedSlot;
		doSwap(block);
		OtherBlockUtils.placeBlock(pos, rotate.getValue(), packetPlace.getValue());
		if (inventory.getValue()) {
			doSwap(block);
			EntityUtil.syncInventory();
		} else {
			doSwap(old);
		}
		progress++;
		timer.reset();
	}

	public static boolean selfIntersectPos(BlockPos pos) {
		return MC.player.getBoundingBox().intersects(new Box(pos));
	}
	private void doSwap(int slot) {
		if (inventory.getValue()) {
			InventoryUtil.inventorySwap(slot, MC.player.getInventory().selectedSlot);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}

	private int getBlock() {
		if (inventory.getValue()) {
			if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
				return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
			}
			return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
		} else {
			if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
				return InventoryUtil.findBlock(Blocks.OBSIDIAN);
			}
			return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
		}
	}

	public BlockPos getHelperPos(BlockPos pos) {
		for (Direction i : Direction.values()) {
			if (detectMining.getValue() && Payload.getInstance().breakManager.isMining(pos.offset(i))) continue;
			if (!OtherBlockUtils.isStrictDirection(pos.offset(i), i.getOpposite())) continue;
			if (OtherBlockUtils.canPlace(pos.offset(i))) return pos.offset(i);
		}
		return null;
	}

	public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
		Vec3d vec3d = posTo.subtract(posFrom);
		return getRotationFromVec(vec3d);
	}

	private static Vec2f getRotationFromVec(Vec3d vec) {
		double d = vec.x;
		double d2 = vec.z;
		double xz = Math.hypot(d, d2);
		d2 = vec.z;
		double d3 = vec.x;
		double yaw = normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
		double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
		return new Vec2f((float) yaw, (float) pitch);
	}

	private static double normalizeAngle(double angleIn) {
		double angle = angleIn;
		if ((angle %= 360.0) >= 180.0) {
			angle -= 360.0;
		}
		if (angle < -180.0) {
			angle += 360.0;
		}
		return angle;
	}
}