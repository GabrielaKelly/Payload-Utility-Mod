

package net.payload.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.input.Input;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.PayloadClient;
import net.payload.api.event.Cancelable;
import net.payload.event.events.MovementPacketsEvent;
import net.payload.event.events.PlayerMoveEvent;
import net.payload.module.modules.client.Freecam;
import net.payload.module.modules.exploit.PacketControl;
import net.payload.module.modules.movement.*;
import net.payload.module.modules.world.Scaffold;
import net.payload.utils.rotation.RotationManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.event.events.PlayerHealthEvent;
import net.payload.event.events.SendMovementPacketEvent;
import net.payload.gui.GuiManager;
import net.payload.mixin.interfaces.ICamera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;

import java.util.List;

import static net.payload.PayloadClient.MC;

@Cancelable
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntityMixin {

	@Shadow
	public Input input;

	@Shadow
	protected boolean isCamera() {
		return false;
	}

	@Shadow
	private ClientPlayNetworkHandler networkHandler;

	@Shadow
	protected abstract void sendMovementPackets();

	@Shadow public abstract float getYaw(float tickDelta);

	@Shadow public abstract float getPitch(float tickDelta);

	@Inject(at = { @At("HEAD") }, method = "setShowsDeathScreen(Z)V")
	private void onShowDeathScreen(boolean state, CallbackInfo ci) {
		GuiManager hudManager = Payload.getInstance().guiManager;

		if (state && hudManager.isClickGuiOpen()) {
			hudManager.setClickGuiOpen(false);
		}
	}

	@Inject(method = "pushOutOfBlocks",
			at = @At("HEAD"),
			cancellable = true)
	private void onPushOutOfBlocksHook(double x, double d, CallbackInfo info) {
		if (Payload.getInstance().moduleManager.antiknockback.state.getValue() && Payload.getInstance().moduleManager.antiknockback.noPush.getValue()) {
			info.cancel();
		}
	}

	@Inject(at = { @At("HEAD") }, method = "isCamera()Z", cancellable = true)
	private void onIsCamera(CallbackInfoReturnable<Boolean> cir) {
		Freecam freecam = (Freecam) Payload.getInstance().moduleManager.freecam;
		if (freecam.state.getValue()) {
			cir.setReturnValue(true);
		}
	}

	@Override
	public void onIsSpectator(CallbackInfoReturnable<Boolean> cir) {
		if (Payload.getInstance().moduleManager.freecam.state.getValue()) {
			cir.setReturnValue(true);
		}
	}

	@Override
	public void onSetHealth(float health, CallbackInfo ci) {
		PlayerHealthEvent event = new PlayerHealthEvent(null, health);
		Payload.getInstance().eventManager.Fire(event);
	}

	@Override
	protected void onGetOffGroundSpeed(CallbackInfoReturnable<Float> cir) {
		if (Payload.getInstance().moduleManager.fly.state.getValue()) {
			Fly fly = (Fly) Payload.getInstance().moduleManager.fly;
			cir.setReturnValue((float) fly.getSpeed());
		} 
	}

	@Override
	public void onGetStepHeight(CallbackInfoReturnable<Float> cir) {
		Step stepHack = (Step) Payload.getInstance().moduleManager.step;
		if (stepHack.state.getValue()) {
			cir.setReturnValue(cir.getReturnValue());
		}
	}

	@Override
	public void onGetJumpVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
		PayloadClient payload = Payload.getInstance();
		HighJump higherJump = (HighJump) payload.moduleManager.higherjump;
		if (higherJump.state.getValue()) {
			cir.setReturnValue(higherJump.getJumpHeightMultiplier());
		}
	}

	@ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isWalking()Z"))
	private boolean modifyIsWalking(boolean original) {

		Sprint s = Payload.getInstance().moduleManager.sprint;
		Scaffold a = Payload.getInstance().moduleManager.scaffold;

		if (a.getNoSprint() && a.state.getValue()) {
			return original;
		}

		if (!s.returnRage() || !s.state.getValue()) return original;

		float forwards = Math.abs(input.movementSideways);
		float sideways = Math.abs(input.movementForward);

		return (MC.player.isTouchingWater() ? (forwards > 1.0E-5F || sideways > 1.0E-5F) : (forwards > 0.8 || sideways > 0.8));
	}

	@ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z"))
	private boolean modifyMovement(boolean original) {

		Sprint s = Payload.getInstance().moduleManager.sprint;

		if (!s.returnRage() || !s.state.getValue()) return original;

		return Math.abs(input.movementSideways) > 1.0E-5F || Math.abs(input.movementForward) > 1.0E-5F;
	}

	@WrapWithCondition(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V", ordinal = 3))
	private boolean wrapSetSprinting(ClientPlayerEntity instance, boolean b) {
		Sprint s = Payload.getInstance().moduleManager.sprint;

		return !s.returnRage();
	}

	@Override
	public void onTickNewAi(CallbackInfo ci) {
		if (Payload.getInstance().moduleManager.freecam.state.getValue())
			ci.cancel();
	}

	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
	public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {

		PlayerMoveEvent event = new PlayerMoveEvent(movement.x, movement.y, movement.z);
		Payload.getInstance().eventManager.Fire(event);
		ci.cancel();
		if (!event.isCancelled()) {
			super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
		}
/*
		if (Payload.getInstance().moduleManager.chorusExploit.state.getValue()) {
			PlayerMoveEvent event = new PlayerMoveEvent(movement.x, movement.y, movement.z);
			Payload.getInstance().eventManager.Fire(event);
			if (event.isCancelled()) {
				ci.cancel();
			}
		}
		else if (Payload.getInstance().moduleManager.strafe.state.getValue()) {
			PlayerMoveEvent event = new PlayerMoveEvent(movement.x, movement.y, movement.z);
			Payload.getInstance().eventManager.Fire(event);
			ci.cancel();
			if (!event.isCancelled()) {
				super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
			}
		}

 */
	}

	@Override
	public void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		if (Payload.getInstance().moduleManager.freecam.state.getValue()) {
			float f = (float) cursorDeltaY * 0.15f;
			float g = (float) cursorDeltaX * 0.15f;

			MinecraftClient mc = MinecraftClient.getInstance();
			Camera camera = mc.gameRenderer.getCamera();
			ICamera icamera = (ICamera) camera;

			float newYaw = camera.getYaw() + g;
			float newPitch = Math.min(90, Math.max(camera.getPitch() + f, -90));

			icamera.setCameraRotation(newYaw, newPitch);
			ci.cancel();
		}
	}
	
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
	private void onTickHasVehicleBeforeSendPackets(CallbackInfo info) {
		SendMovementPacketEvent.Pre sendMovementPacketPreEvent = new SendMovementPacketEvent.Pre();
		Payload.getInstance().eventManager.Fire(sendMovementPacketPreEvent);
	}

	@Shadow
	@Final
	private List<ClientPlayerTickable> tickables;

	@Shadow
	private boolean autoJumpEnabled;

	@Shadow
	private double lastX;
	@Shadow
	private double lastBaseY;
	@Shadow
	private double lastZ;
	@Shadow
	private float lastYaw;
	@Shadow
	private float lastPitch;
	@Shadow
	private boolean lastOnGround;
	@Shadow
	private boolean lastSneaking;
	@Final
	@Shadow
	protected MinecraftClient client;
	@Shadow
	private int ticksSinceLastPositionPacketSent;


	@Shadow private boolean lastHorizontalCollision;

	@Shadow public abstract boolean isSneaking();

	@Shadow protected abstract void sendSprintingPacket();

	@Redirect(method = "tickMovement",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"),
			require = 0)
	private boolean tickMovementHook(ClientPlayerEntity player) {
		if (Payload.getInstance().moduleManager.noslowdown.noSlow()) {
			return false;
		}
		return player.isUsingItem();
	}

	@Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
	private void onSendMovementPacketsHead(CallbackInfo info) {
		info.cancel();
		try {
			SendMovementPacketEvent.Pre sendMovementPacketPreEvent = new SendMovementPacketEvent.Pre();
			Payload.getInstance().eventManager.Fire(sendMovementPacketPreEvent);
			this.sendSprintingPacket();
			boolean bl = this.isSneaking();
			if (bl != this.lastSneaking) {
				ClientCommandC2SPacket.Mode mode = bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
				this.networkHandler.sendPacket(new ClientCommandC2SPacket(this.client.player, mode));
				this.lastSneaking = bl;
			}

			if (this.isCamera() && MC.player != null) {
				PacketControl packetControl = Payload.getInstance().moduleManager.packetControl;
				RotationManager rotationManager = Payload.getInstance().rotationManager;

				double d = this.getX() - this.lastX;
				double e = this.getY() - this.lastBaseY;
				double f = this.getZ() - this.lastZ;

				float yaw = this.getYaw();
				float pitch = this.getPitch();

				Payload.getInstance().moduleManager.sprint.setYawSprint(yaw);

				MovementPacketsEvent movementPacketsEvent = new MovementPacketsEvent(yaw, pitch);
				Payload.getInstance().eventManager.Fire(movementPacketsEvent);
				yaw = movementPacketsEvent.getYaw();
				pitch = movementPacketsEvent.getPitch();
				rotationManager.rotationYaw = yaw;
				rotationManager.rotationPitch = pitch;

				double g = yaw - rotationManager.lastYaw;//this.lastYaw;
				double h = pitch - rotationManager.lastPitch;//this.lastPitch;
				++this.ticksSinceLastPositionPacketSent;
				boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20 || (packetControl.state.getValue() && packetControl.positionSync.getValue() && packetControl.positionCacheTimer.passed(packetControl.positionDelay.getValue()));
				boolean bl3 = (g != 0.0 || h != 0.0 || (packetControl.state.getValue() && packetControl.rotationSync.getValue() && packetControl.rotationCacheTimer.passed(packetControl.rotationDelay.getValue())));
				if (packetControl.state.getValue() && packetControl.TimerBypass.getValue()) {
					bl3 = packetControl.full;
				}


				if (this.hasVehicle()) {
					Vec3d vec3d = this.getVelocity();
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.x, -999.0, vec3d.z, yaw, pitch, this.isOnGround(), this.horizontalCollision));
					bl2 = false;
				} else if (bl2 && bl3) {
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(this.getX(), this.getY(), this.getZ(), yaw, pitch, this.isOnGround(), this.horizontalCollision));
				} else if (bl2) {
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.getX(), this.getY(), this.getZ(), this.isOnGround(), this.horizontalCollision));
				} else if (bl3) {
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, this.isOnGround(), this.horizontalCollision));
				} else if (this.lastOnGround != this.isOnGround() || packetControl.state.getValue() && packetControl.groundSync.getValue() && packetControl.groundCacheTimer.passed(packetControl.rotationDelay.getValue())) {
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(this.isOnGround(), this.horizontalCollision));
				}

				if (bl2) {
					this.lastX = this.getX();
					this.lastBaseY = this.getY();
					this.lastZ = this.getZ();
					this.ticksSinceLastPositionPacketSent = 0;
				}

				if (bl3) {
					this.lastYaw = yaw;
					this.lastPitch = pitch;
				}

				this.lastOnGround = this.isOnGround();
				this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
			}
			//Alien.EVENT_BUS.post(new UpdateWalkingPlayerEvent(Event.Stage.Post));
		} catch (Exception e) {
			e.printStackTrace();
		}
/*
		SendMovementPacketEvent.Pre sendMovementPacketPreEvent = new SendMovementPacketEvent.Pre();
		Payload.getInstance().eventManager.Fire(sendMovementPacketPreEvent);

		if (this.isCamera() && MC.player != null) {
			float yaw = this.getYaw();
			float pitch = this.getPitch();
			Payload.getInstance().moduleManager.sprint.setYawSprint(yaw);
			Payload.getInstance().moduleManager.antiknockback.setYawVelocity(yaw);
			Payload.getInstance().moduleManager.antiknockback.setPitchVelocity(pitch);
		}

 */
	}

	@Inject(method = "sendMovementPackets", at = @At("TAIL"))
	private void onSendMovementPacketsTail(CallbackInfo info) {
		SendMovementPacketEvent.Post sendMovementPacketPostEvent = new SendMovementPacketEvent.Post();

		Payload.getInstance().eventManager.Fire(sendMovementPacketPostEvent);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 1, shift = At.Shift.AFTER))
	private void onTickHasVehicleAfterSendPackets(CallbackInfo info) {
		SendMovementPacketEvent.Post sendMovementPacketPostEvent = new SendMovementPacketEvent.Post();

		Payload.getInstance().eventManager.Fire(sendMovementPacketPostEvent);
	}
}
