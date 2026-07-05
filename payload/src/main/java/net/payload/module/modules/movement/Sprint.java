package net.payload.module.modules.movement;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.payload.Payload;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.events.SendPacketEvent;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.event.listeners.SendPacketListener;
import net.payload.event.listeners.TickListener;
import net.payload.mixin.ClientPlayerEntityAccessor;
import net.payload.mixin.interfaces.IPlayerInteractEntityC2SPacket;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;

public class Sprint extends Module implements TickListener, ReceivePacketListener, SendPacketListener {

	public enum Mode {
		Legit, Rage
	}

	private final EnumSetting<Sprint.Mode> mode = EnumSetting.<Sprint.Mode>builder()
			.id("sprint_mode")
			.displayName("Mode")
			.description("Sprint mode")
			.defaultValue(Mode.Rage)
			.build();

	public BooleanSetting jumpFix = BooleanSetting.builder()
			.id("sprint_jump")
			.displayName("Force Jumping")
			.defaultValue(true)
			.build();

	public BooleanSetting keepSprint = BooleanSetting.builder()
			.id("sprint_force")
			.displayName("Keep Sprint")
			.defaultValue(true)
			.build();

	public BooleanSetting unsprintOnHit = BooleanSetting.builder()
			.id("sprint_disablehit")
			.displayName("Unsprint on hit")
			.defaultValue(false)
			.build();

	public BooleanSetting unsprintInWater = BooleanSetting.builder()
			.id("sprint_disablewater")
			.displayName("Unsprint in water")
			.defaultValue(false)
			.build();

	float rotationYaw;

	public Sprint() {
		super("Sprint");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Forces the player to sprint");

		this.addSetting(mode);
		this.addSetting(jumpFix);
		this.addSetting(keepSprint);
		this.addSetting(unsprintOnHit);
		this.addSetting(unsprintInWater);
	}

	public boolean returnRage() {
        return mode.getValue() == Mode.Rage;
	}

	@Override
	public void onDisable() {
		MC.player.setSprinting(false);
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {
		if (MC.player != null) {
			if (shouldSprint()) MC.player.setSprinting(true);
		}
	}

	@Override
	public void onTick(Post event) {

	}

	@Override
	public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
		if (nullCheck()) return;

		if (!unsprintOnHit.getValue() || !(readPacketEvent.GetPacket() instanceof IPlayerInteractEntityC2SPacket packet))
			return;

		MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
		MC.player.setSprinting(false);
	}


	@Override
	public void onSendPacket(SendPacketEvent event) {
		if (nullCheck()) return;

		if (!unsprintOnHit.getValue() || !keepSprint.getValue() || MC.player == null) return;
		if (!(event.GetPacket() instanceof IPlayerInteractEntityC2SPacket packet)) {
			return;
		}

		if (shouldSprint() && !MC.player.isSprinting()) {
			MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
			MC.player.setSprinting(true);
		}

	}

	public boolean shouldSprint() {
		if (MC.player == null) {
			return false;
		}

		if (Payload.getInstance().moduleManager.scaffold.getNoSprint() && Payload.getInstance().moduleManager.scaffold.state.getValue()) {
			return false;
		}

		if (Payload.getInstance().moduleManager.elytraBounce.state.getValue() && Payload.getInstance().moduleManager.elytraBounce.sprint.getValue()) {
			return false;
		}

		if (unsprintInWater.getValue() && (MC.player.isTouchingWater() || MC.player.isSubmergedInWater())) return false;

		if (Mode.Legit == mode.getValue()) {

			if (Payload.getInstance().moduleManager.moveFix.state.getValue()) {
				return MC.player.input.movementForward == 1;
			} else {
				return MC.options.forwardKey.isPressed() && MathHelper.angleBetween(MC.player.getYaw(), rotationYaw) < 40;
			}
		}

			boolean strictSprint = MC.player.forwardSpeed > 1.0E-5F
					&& ((ClientPlayerEntityAccessor) MC.player).invokeCanSprint()
					&& (!MC.player.horizontalCollision || MC.player.collidedSoftly)
					&& !(MC.player.isTouchingWater() && !MC.player.isSubmergedInWater());

			return this.state.getValue();
		}

	public boolean stopSprinting() {
		return !this.state.getValue() || !keepSprint.getValue();
	}

	public void setYawSprint(float g) {
		rotationYaw = g;
	}

	public static float getSprintYaw(float yaw) {
		if (MC.options.forwardKey.isPressed() && !MC.options.backKey.isPressed()) {
			if (MC.options.leftKey.isPressed() && !MC.options.rightKey.isPressed()) {
				yaw -= 45f;
			} else if (MC.options.rightKey.isPressed() && !MC.options.leftKey.isPressed()) {
				yaw += 45f;
			}
			// Forward movement - no change to yaw
		} else if (MC.options.backKey.isPressed() && !MC.options.forwardKey.isPressed()) {
			yaw += 180f;
			if (MC.options.leftKey.isPressed() && !MC.options.rightKey.isPressed()) {
				yaw += 45f;
			} else if (MC.options.rightKey.isPressed() && !MC.options.leftKey.isPressed()) {
				yaw -= 45f;
			}
		} else if (MC.options.leftKey.isPressed() && !MC.options.rightKey.isPressed()) {
			yaw -= 90f;
		} else if (MC.options.rightKey.isPressed() && !MC.options.leftKey.isPressed()) {
			yaw += 90f;
		}
		return MathHelper.wrapDegrees(yaw);
	}

}
