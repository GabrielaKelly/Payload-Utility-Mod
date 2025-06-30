package net.payload.module.modules.world;

import java.util.HashSet;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.BlockStateEvent;
import net.payload.event.events.LookAtEvent;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.BlockStateListener;
import net.payload.event.listeners.LookAtListener;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.misc.InstantRebreak;
import net.payload.module.modules.misc.PacketMine;
import net.payload.settings.types.BlocksSetting;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.render.Render3D;

import static net.payload.utils.block.BlockUtils.canBreak;

public class Nuker extends Module implements TickListener, BlockStateListener, LookAtListener, Render3DListener {

	private BooleanSetting creative = BooleanSetting.builder().id("nuker_creative").displayName("Creative")
			.description("Creative").defaultValue(false).build();

	private FloatSetting radius = FloatSetting.builder().id("nuker_radius").displayName("Radius").description("Radius")
			.defaultValue(3f).minValue(0f).maxValue(15f).step(1f).build();

	private BlocksSetting blacklist = BlocksSetting.builder().id("nuker_blacklist").displayName("Blacklist")
			.description("Blocks that will not be broken by Nuker.").defaultValue(new HashSet<Block>()).build();

	private BooleanSetting rotate = BooleanSetting.builder().id("nuker_rotate").displayName("Rotate")
			.description("Rotate to block to mine").defaultValue(false).build();

	private BooleanSetting autoTool = BooleanSetting.builder().id("nuker_autotool").displayName("AutoTool")
			.description("Uses appropriate tool").defaultValue(true).build();

	private ColorSetting color = ColorSetting.builder().id("nuker_color").displayName("Color").description("Color")
			.defaultValue(new Color(0f, 1f, 1f)).build();

	private BooleanSetting packetMine = BooleanSetting.builder().id("nuker_packetmine").displayName("PacketMine")
			.description("Uses packet mine instead of regular breaking").defaultValue(false).build();

	private BooleanSetting onlyAbove = BooleanSetting.builder().id("nuker_onlyabove").displayName("No Below")
			.description("Mines around instead of below").defaultValue(false).build();

	private BooleanSetting nearestFirst = BooleanSetting.builder().id("nuker_nearest_first").displayName("Nearest First")
			.description("Mines the nearest blocks first instead of the furthest").defaultValue(true).build();

	private BooleanSetting auraPause = BooleanSetting.builder().id("nuker_pauseaura").displayName("KillAura Pause")
			.description("Mines the nearest blocks first instead of the furthest").defaultValue(true).build();

	private BooleanSetting walk = BooleanSetting.builder().id("nuker_walk").displayName("Empty Walk")
			.description("Walks forward if no blocks detected").defaultValue(false).build();

	private BlockPos currentBlockToBreak = null;
	private int breakingTicks = 0;
	private static final int MAX_BREAK_TICKS = 20; // Reset breaking after this many ticks to prevent stalling

	public Nuker() {
		super("Nuker");
		this.setCategory(Category.of("World"));
		this.setDescription("Destroys blocks around the player, works best with AutoRebreak or PacketMine");

		this.addSetting(blacklist);
		this.addSetting(creative);
		this.addSetting(radius);
		this.addSetting(packetMine);
		this.addSetting(color);
		this.addSetting(rotate);
		this.addSetting(autoTool);
		this.addSetting(nearestFirst);
		this.addSetting(walk);
		this.addSetting(auraPause);
		this.addSetting(onlyAbove);
	}

	public void setRadius(int radius) {
		this.radius.setValue((float) radius);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(BlockStateListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		currentBlockToBreak = null;
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(BlockStateListener.class, this);
		Payload.getInstance().eventManager.AddListener(LookAtListener.class, this);
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
	}

	@Override
	public void onToggle() {
	}

	@Override
	public void onBlockStateChanged(BlockStateEvent event) {
		if (currentBlockToBreak != null) {
			BlockPos blockPos = event.getBlockPos();
			BlockState oldBlockState = event.getPreviousBlockState();
			BlockState newBlockState = event.getBlockState();

			if (newBlockState != null && oldBlockState != null) {
				if (blockPos.equals(currentBlockToBreak) &&
						(!newBlockState.isAir() && oldBlockState.isAir() || newBlockState.isAir())) {
					currentBlockToBreak = null;
					breakingTicks = 0;
				}
			}
		}
	}

	private BlockPos getNextBlock() {
		// Get the effective radius as an integer
		int range = (int) Math.floor(radius.getValue());
		BlockPos playerPos = BlockPos.ofFloored(MC.player.getPos());
		int playerY = playerPos.getY();

		BlockPos bestPos = null;
		double bestDistance = nearestFirst.getValue() ? Double.MAX_VALUE : -1;

		// Iterate through blocks in a square pattern
		for (int y = range; y >= -range; y--) {
			for (int x = -range; x <= range; x++) {
				for (int z = -range; z <= range; z++) {
					BlockPos blockPos = playerPos.add(x, y, z);

					// Skip blocks below the player if No Below is enabled
					if (onlyAbove.getValue() && blockPos.getY() < playerY) {
						continue;
					}

					BlockState state = MC.world.getBlockState(blockPos);
					Block block = state.getBlock();

					if (PacketMine.godBlocks.contains(block) || state.isAir() || blacklist.getValue().contains(block) || !canBreak(blockPos, state)) {
						continue;
					}

					double distance = blockPos.getSquaredDistance(playerPos);
					if (bestPos == null || (nearestFirst.getValue() && distance < bestDistance) || (!nearestFirst.getValue() && distance > bestDistance)) {
						bestPos = blockPos;
						bestDistance = distance;
					}
				}
			}
		}
		return bestPos;
	}

	@Override
	public void onTick(Pre event) {
		// Get the effective radius as an integer
		if (auraPause.getValue() && Payload.getInstance().moduleManager.killaura.getAuraEntity() != null) {
			return;
		}
		
		int range = (int) Math.floor(radius.getValue());
		BlockPos playerPos = BlockPos.ofFloored(MC.player.getPos());
		int playerY = playerPos.getY();

		if (creative.getValue()) {
			// Iterate through blocks in a square pattern
			for (int y = range; y >= -range; y--) {
				for (int x = -range; x <= range; x++) {
					for (int z = -range; z <= range; z++) {
						BlockPos blockPos = playerPos.add(x, y, z);

						// Skip blocks below the player if No Below is enabled
						if (onlyAbove.getValue() && blockPos.getY() < playerY) {
							continue;
						}

						BlockState state = MC.world.getBlockState(blockPos);
						Block block = state.getBlock();

						if (PacketMine.godBlocks.contains(block) || state.isAir() || blacklist.getValue().contains(block)) {
							continue;
						}

						MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
								Action.START_DESTROY_BLOCK, blockPos, Direction.NORTH));
						MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
								Action.STOP_DESTROY_BLOCK, blockPos, Direction.NORTH));
						MC.player.swingHand(Hand.MAIN_HAND);
					}
				}
			}
		} else {
			// Manage PacketMine module if needed
			if (packetMine.getValue()) {
				if (!Payload.getInstance().moduleManager.packetMine.state.getValue()) {
					sendChatMessage("Enabled PacketMine for Nuker");
					Payload.getInstance().moduleManager.packetMine.toggle();
				}
			}

			// Find new block to break if needed
			if (currentBlockToBreak == null) {
				currentBlockToBreak = getNextBlock();
				breakingTicks = 0;
				if (walk.getValue()) {
					MC.options.forwardKey.setPressed(true);
				}
			}

			if (currentBlockToBreak != null) {
				if (walk.getValue()) {
					MC.options.forwardKey.setPressed(false);
				}
				// Check if the block is still within the square radius
				int dx = Math.abs(currentBlockToBreak.getX() - playerPos.getX());
				int dy = Math.abs(currentBlockToBreak.getY() - playerPos.getY());
				int dz = Math.abs(currentBlockToBreak.getZ() - playerPos.getZ());

				if (dx > range || dy > range || dz > range ||
						(onlyAbove.getValue() && currentBlockToBreak.getY() < playerY) ||
						MC.world.getBlockState(currentBlockToBreak).isAir() ||
						breakingTicks > MAX_BREAK_TICKS) {
					currentBlockToBreak = null;
					breakingTicks = 0;
				} else {
					if (packetMine.getValue()) {
						autoTool(currentBlockToBreak);
						PacketMine.INSTANCE.mine(currentBlockToBreak);
					} else {
						// Vanilla breaking logic
						Direction direction = getOptimalDirection(currentBlockToBreak);

						// Check for InstantRebreak support
						InstantRebreak ir = Payload.getInstance().moduleManager.instantRebreak;
						if (ir != null && ir.state.getValue() && ir.blockPos.equals(currentBlockToBreak) && ir.shouldMine()) {
							autoTool(currentBlockToBreak);
							ir.sendPacket();
						} else {
							// Normal breaking
							if (breakingTicks == 0) {
								// Start breaking
								autoTool(currentBlockToBreak);
								Payload.getInstance().rotationManager.lookAt(currentBlockToBreak, direction);
								MC.interactionManager.attackBlock(currentBlockToBreak, direction);
							} else {
								// Continue breaking
								Payload.getInstance().rotationManager.lookAt(currentBlockToBreak, direction);
								MC.interactionManager.updateBlockBreakingProgress(currentBlockToBreak, direction);
							}

							// Always swing hand for visual feedback
							MC.player.swingHand(Hand.MAIN_HAND);
							breakingTicks++;
						}
					}
				}
			}
		}
	}

	private Direction getOptimalDirection(BlockPos pos) {
		// First try to find the most accessible face
		Vec3d eyesPos = new Vec3d(MC.player.getX(), MC.player.getY() + MC.player.getEyeHeight(MC.player.getPose()), MC.player.getZ());
		Vec3d blockCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		double bestDistance = Double.MAX_VALUE;
		Direction bestDirection = Direction.UP; // Default

		for (Direction direction : Direction.values()) {
			Vec3d facePos = new Vec3d(
					blockCenter.x + direction.getOffsetX() * 0.5,
					blockCenter.y + direction.getOffsetY() * 0.5,
					blockCenter.z + direction.getOffsetZ() * 0.5
			);

			double distance = eyesPos.distanceTo(facePos);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestDirection = direction;
			}
		}

		return bestDirection;
	}

	@Override
	public void onTick(Post event) {
	}

	@Override
	public void onLook(LookAtEvent event) {

	}

	public void autoTool(BlockPos pos) {
		if (autoTool.getValue()) {
			int slot = Payload.getInstance().moduleManager.autoTool.getOutsideTool(pos);
			InventoryUtil.switchToSlot(slot);
		}
	}

	@Override
	public void onRender(Render3DEvent event) {
		if (currentBlockToBreak != null && !packetMine.getValue()) {
			Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), new Box(currentBlockToBreak), color.getValue(), 1.0f);
		}
	}
}