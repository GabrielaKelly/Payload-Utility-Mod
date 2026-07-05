

/**
 * Sneak Module
 */
package net.payload.module.modules.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;

public class MoveFix extends Module implements TravelListener, KeyDownListener, JumpListener, UpdateVelocityListener {

	// Enum definition
	public enum UpdateMode {
		Movement,
		Mouse,
		Both
	}

	// Settings
	public final EnumSetting<UpdateMode> updateMode = EnumSetting.<UpdateMode>builder()
			.id("movefix_updatemode")
			.displayName("Update Mode")
			.description("Determines how rotations are updated")
			.defaultValue(UpdateMode.Both)
			.build();

	public FloatSetting tickssetting = FloatSetting.builder().id("movefix_preserve").displayName("Preserve Ticks")
			.description("The pitch angle for throwing XP bottles.").defaultValue(10.0f).minValue(0f).maxValue(20f)
			.step(1f).build();


	private final BooleanSetting grim = BooleanSetting.builder()
			.id("movefix_grim")
			.displayName("Grim")
			.description("Grim compatibility mode")
			.defaultValue(true)
			.build();

	private final BooleanSetting travel = BooleanSetting.builder()
			.id("movefix_travel")
			.displayName("Travel")
			.description("Enable travel functionality")
			.defaultValue(true)
			.build();

	// Static fields for rotation handling
	public static float fixRotation;
	public static float fixPitch;
	private float prevYaw;
	private float prevPitch;

	private final MinecraftClient MC = MinecraftClient.getInstance();

	public MoveFix() {
		super("MoveFix");
		this.setCategory(Category.of("Client"));
		this.setDescription("Holds packets when inactive, made for Grim");

		this.addSetting(updateMode);
		this.addSetting(grim);
		this.addSetting(tickssetting);
		this.addSetting(travel);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TravelListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(JumpListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(UpdateVelocityListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TravelListener.class, this);
		Payload.getInstance().eventManager.AddListener(KeyDownListener.class, this);
		Payload.getInstance().eventManager.AddListener(JumpListener.class, this);
		Payload.getInstance().eventManager.AddListener(UpdateVelocityListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (nullCheck()) return;

		if (!grim.getValue()) return;
		//if (HoleSnap.INSTANCE.isOn()) return;
		if (MC.player.isRiding() || Payload.getInstance().moduleManager.freecam.state.getValue())
			return;

		float forward = MC.player.input.movementForward;
		float sideways = MC.player.input.movementSideways;
		float delta = (MC.player.getYaw() - Payload.getInstance().rotationManager.rotationYaw) * 0.017453292F;
		float cos = MathHelper.cos(delta);
		float sin = MathHelper.sin(delta);
		MC.player.input.movementSideways = (float)Math.round(sideways * cos - forward * sin);
		MC.player.input.movementForward = (float)Math.round(forward * cos + sideways * sin);
	}

	@Override
	public void onTravelPre(TravelEvent.Pre event) {
		if (nullCheck()) return;

		if (!grim.getValue() || !travel.getValue()) return;

		if (MC.player.isRiding())
			return;

			prevYaw = MC.player.getYaw();
			prevPitch = MC.player.getPitch();
			MC.player.setYaw(fixRotation);
			MC.player.setPitch(fixPitch);
			MC.player.setYaw(prevYaw);
			MC.player.setPitch(prevPitch);
	}

	@Override
	public void onTravelPost(TravelEvent.Post event) {
		if (nullCheck()) return;

		if (!grim.getValue() || !travel.getValue()) return;

		if (MC.player.isRiding())
			return;

			MC.player.setYaw(prevYaw);
			MC.player.setPitch(prevPitch);
	}

	@Override
	public void onJumpPre(JumpEvent.Pre event) {
		if (nullCheck()) return;

		if (!grim.getValue()) return;
		if (MC.player.isRiding())
			return;

			prevYaw = MC.player.getYaw();
			prevPitch = MC.player.getPitch();
			MC.player.setYaw(fixRotation);
			MC.player.setPitch(fixPitch);
	}

	@Override
	public void onJumpPost(JumpEvent.Post event) {
		if (nullCheck()) return;

		if (!grim.getValue()) return;
		if (MC.player.isRiding())
			return;

		MC.player.setYaw(prevYaw);
		MC.player.setPitch(prevPitch);
	}

	@Override
	public void onUpdateVel(UpdateVelocityEvent event) {
		if (nullCheck()) return;

		if (!grim.getValue() || travel.getValue()) return;

		if (MC.player.isRiding())
			return;

		event.cancel();
		event.setVelocity(movementInputToVelocity(event.getMovementInput(), event.getSpeed(), fixRotation));
	}

	private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
		double d = movementInput.lengthSquared();
		if (d < 1.0E-7) {
			return Vec3d.ZERO;
		} else {
			Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
			float f = MathHelper.sin(yaw * 0.017453292F);
			float g = MathHelper.cos(yaw * 0.017453292F);
			return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
		}
	}
}