package net.payload.module.modules.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.LookAtEvent;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.*;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.client.AntiCheat;
import net.payload.settings.types.*;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.MovementUtil;
import net.payload.utils.render.Render3D;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Scaffold extends Module implements TickListener, Render3DListener, LookAtListener {
	// General Settings

	public enum RotationMode {
		None, Payload, Grim
	}

	private final FloatSetting priority = FloatSetting.builder()
			.id("scaffold_prio")
			.displayName("Priority")
			.defaultValue(20f)
			.minValue(0f)
			.maxValue(100f)
			.step(5f)
			.build();

	private final BooleanSetting packetPlace = BooleanSetting.builder()
			.id("scaffold_packetplace")
			.displayName("Packet Place")
			.description("Allows faster upward scaffolding")
			.defaultValue(false)
			.build();

	private final BooleanSetting hotbarSwap = BooleanSetting.builder()
			.id("scaffold_hotbar")
			.displayName("Hotbar Swap")
			.description("Switches to block")
			.defaultValue(false)
			.build();

	private final BooleanSetting tower = BooleanSetting.builder()
			.id("scaffold_tower")
			.displayName("Tower")
			.description("Allows faster upward scaffolding")
			.defaultValue(false)
			.build();

	private final EnumSetting<Scaffold.RotationMode> rotationMode = EnumSetting.<Scaffold.RotationMode>builder()
			.id("scaffold_rotations")
			.displayName("Rotation Mode")
			.defaultValue(RotationMode.Payload)
			.build();

	private final BooleanSetting safewalk = BooleanSetting.builder()
			.id("scaffold_safewalk")
			.displayName("SafeWalk")
			.description("Reduces faulty falls")
			.defaultValue(true)
			.build();

	private final BooleanSetting noSprint = BooleanSetting.builder()
			.id("scaffold_nosprint")
			.displayName("NoSprint")
			.description("Reduces faulty falls")
			.defaultValue(false)
			.build();

	private final BooleanSetting checkFov = BooleanSetting.builder()
			.id("scaffold_checkfov")
			.displayName("Check Fov")
			.description("")
			.defaultValue(false)
			.build();

	private final FloatSetting fov = FloatSetting.builder()
			.id("scaffold_fov")
			.displayName("Fov")
			.description("Scaffold placement radius")
			.defaultValue(5f)
			.minValue(0f)
			.maxValue(30f)
			.step(1f)
			.build();

	// Render Settings
	private final BooleanSetting render = BooleanSetting.builder()
			.id("scaffold_render")
			.displayName("Render")
			.defaultValue(true)
			.build();

	private final ColorSetting sideColor = ColorSetting.builder()
			.id("scaffold_side_color")
			.displayName("Side Color")
			.description("Color of placed block sides")
			.defaultValue(new Color(197, 137, 232, 90))
			.build();

	public FloatSetting lineThickness = FloatSetting.builder()
			.id("scaffold_thickness")
			.displayName("Line Thickness")
			.defaultValue(2f)
			.minValue(0f)
			.maxValue(5f)
			.step(0.1f)
			.build();

	private BlockPos pos;
	private static Vec3d lastVec3d;

	private final CacheTimer timer = new CacheTimer();

	// List to store blocks with their fade timers
	private final List<Pair<Integer, BlockPos>> fadingBlocks = new ArrayList<>();

	private Vec3d vec;

	public Scaffold() {
		super("Scaffold");
		this.setCategory(Category.of("World"));
		this.setDescription("Automatically places blocks under you for traveling or building");

		// Add settings
		this.addSetting(rotationMode);
		this.addSetting(safewalk);
		this.addSetting(priority);
		this.addSetting(packetPlace);
		this.addSetting(hotbarSwap);
		this.addSetting(tower);
		this.addSetting(noSprint);

		// FOV settings
		this.addSetting(checkFov);
		this.addSetting(fov);

		// Render settings
		this.addSetting(render);
		this.addSetting(sideColor);
		this.addSetting(lineThickness);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);

		lastVec3d = null;
		pos = null;
		fadingBlocks.clear();
	}

	public boolean getNoSprint() {
		return this.noSprint.get();
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.AddListener(LookAtListener.class, this);
	}

	private final CacheTimer towerTimer = new CacheTimer();

	@Override
	public void onTick(Pre event) {
		// Update fade timers and remove expired blocks
		fadingBlocks.forEach(pair -> pair.setLeft(pair.getLeft() - 1));
		fadingBlocks.removeIf(pair -> pair.getLeft() <= 0);

		if (nullCheck()) return;
		int block = InventoryUtil.findBlock();
		if (block == -1) return;
		BlockPos placePos = MC.player.getBlockPos().down();
		if (OtherBlockUtils.clientCanPlace(placePos, false)) {
			int old = MC.player.getInventory().selectedSlot;
			if (OtherBlockUtils.getPlaceSide(placePos) == null) {
				double distance = 1000;
				BlockPos bestPos = null;
				for (Direction i : Direction.values()) {
					if (i == Direction.UP) continue;
					if (OtherBlockUtils.canPlace(placePos.offset(i))) {
						if (bestPos == null || MC.player.squaredDistanceTo(placePos.offset(i).toCenterPos()) < distance) {
							bestPos = placePos.offset(i);
							distance = MC.player.squaredDistanceTo(placePos.offset(i).toCenterPos());
						}
					}
				}
				if (bestPos != null) {
					placePos = bestPos;
				} else {
					return;
				}
			}
			if (rotationMode.getValue() == RotationMode.Payload) {
				Direction side = OtherBlockUtils.getPlaceSide(placePos);
				vec = (placePos.offset(side).toCenterPos().add(side.getOpposite().getVector().getX() * 0.5, side.getOpposite().getVector().getY() * 0.5, side.getOpposite().getVector().getZ() * 0.5));
				timer.reset();
				if (!faceVector(vec)) return;
			}
			if (MC.player.isSprinting() && noSprint.getValue()) {
				MC.player.setSprinting(false);
			}

			if (hotbarSwap.getValue()) {
				swap(block, false);
				OtherBlockUtils.placeBlock(placePos, false, packetPlace.getValue());
			}
			else {
				InventoryUtil.switchToSlot(block);
				OtherBlockUtils.placeBlock(placePos, false, packetPlace.getValue());
				InventoryUtil.switchToSlot(old);
			}

			if (rotationMode.getValue() == RotationMode.Payload && AntiCheat.INSTANCE.snapBack.getValue()) {
				Payload.getInstance().rotationManager.snapBack();
			}

			pos = placePos;

			if (render.get()) {
				fadingBlocks.add(new Pair<>(3, new BlockPos(placePos)));
			}

			if (tower.getValue() && MC.options.jumpKey.isPressed() && !MovementUtil.isMoving()) {
				MovementUtil.setMotionY(0.42);
				MovementUtil.setMotionX(0);
				MovementUtil.setMotionZ(0);
				if (this.towerTimer.passed(1500L)) {
					MovementUtil.setMotionY(-0.28);
					this.towerTimer.reset();
				}
			} else {
				this.towerTimer.reset();
			}
		}
	}

	@Override
	public void onRender(Render3DEvent event) {
		if (render.get()) {
			fadingBlocks.forEach(s -> {
				float fadeRatio = 1.0f - ((float)s.getLeft() / 3);
				Color a = new Color(sideColor.get().getRed(), sideColor.get().getGreen(), sideColor.get().getBlue(), (int)(fadeRatio * sideColor.get().getReverseAlphaInt()));
				Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), new Box(s.getRight()), a, lineThickness.getValue());
			});
		}
	}

	public boolean getSafewalk() {
		return safewalk.get();
	}

	@Override
	public void onLook(LookAtEvent event) {
		if (rotationMode.getValue() == RotationMode.Payload && vec != null) {
			event.setTarget(vec, Payload.getInstance().moduleManager.rotations.steps.getValue(), priority.get());
		}

		if (rotationMode.getValue() == RotationMode.Grim) {
			float baseYaw = MC.player.getYaw();
			float standardPitch = 80;

			boolean forward = MC.options.forwardKey.isPressed();
			boolean back = MC.options.backKey.isPressed();
			boolean left = MC.options.leftKey.isPressed();
			boolean right = MC.options.rightKey.isPressed();
			boolean jump = MC.options.jumpKey.isPressed();

			// Calculate base diagonal angles (45° intervals)
			if (forward && left) {
				setRotation(event, baseYaw + 135, standardPitch);
			}
			else if (forward && right) {
				setRotation(event, baseYaw - 135, standardPitch);
			}
			else if (back && left) {
				setRotation(event, baseYaw + 45, standardPitch);
			}
			else if (back && right) {
				setRotation(event, baseYaw - 45, standardPitch);
			}

			else if (forward) {
				setRotation(event, baseYaw + 180, standardPitch);
			}
			else if (back) {
				setRotation(event, baseYaw, standardPitch);
			}
			else if (left) {
				setRotation(event, baseYaw + 90, standardPitch);
			}
			else if (right) {
				setRotation(event, baseYaw - 90, standardPitch);
			}
			else if (jump) {
				setRotation(event, baseYaw, 90);
			}
		}
	}

	private void setRotation(LookAtEvent event, float yaw, float pitch) {
		event.setRotation(
				yaw,
				pitch,
				Payload.getInstance().moduleManager.rotations.steps.getValue(),
				priority.getValue()
		);
	}

	private boolean faceVector(Vec3d directionVec) {
		if (!checkFov.getValue()) {
			Payload.getInstance().rotationManager.lookAt(directionVec);
			return true;
		} else {
			if (Payload.getInstance().rotationManager.inFov(directionVec, fov.getValue())) {
				return true;
			}
		}
		return !checkFov.getValue();
	}

	@Override
	public void onTick(Post event) {

	}

	@Override
	public void onToggle() {

	}
}