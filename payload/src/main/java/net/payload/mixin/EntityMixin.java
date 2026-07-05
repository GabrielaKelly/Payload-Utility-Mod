

package net.payload.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.LivingEntityMoveEvent;
import net.payload.event.events.PlayerMoveEvent;
import net.payload.event.events.UpdateVelocityEvent;
import net.payload.module.modules.movement.ElytraPacket;
import net.payload.module.modules.render.EntityESP;
import net.payload.module.modules.render.Freelook;
import net.payload.module.modules.render.PlayerESP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

import static net.payload.PayloadClient.MC;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	protected DataTracker dataTracker;

	@Shadow
	public abstract boolean isSubmergedIn(TagKey<Fluid> fluidTag);

	@Shadow
	public abstract boolean isOnGround();

    @Shadow
    public void move(MovementType type, Vec3d movement) {
    }

    @Shadow public abstract EntityType<?> getType();

	@Shadow public float fallDistance;

	@Shadow public abstract float getYaw();

    @Shadow public abstract float getPitch();

    @Shadow public abstract double getX();

	@Shadow public abstract boolean hasVehicle();

	@Shadow public abstract Vec3d getVelocity();

	@Shadow public abstract double getY();

	@Shadow public abstract double getZ();

	@Shadow public boolean horizontalCollision;


	@Inject(method = "updateVelocity", at = {@At("HEAD")}, cancellable = true)
	public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
		if(MC.player == null || MC.world == null) return;

		if ((Object) this == MC.player) {
			UpdateVelocityEvent event = new UpdateVelocityEvent(movementInput, speed, MC.player.getYaw(), movementInputToVelocity(movementInput, speed, MC.player.getYaw()));
			Payload.getInstance().eventManager.Fire(event);
			if (event.isCancelled()) {
				ci.cancel();
				MC.player.setVelocity(MC.player.getVelocity().add(event.getVelocity()));
			}
		}
	}

	@Shadow
	private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
		double d = movementInput.lengthSquared();
		if (d < 1.0E-7) {
			return Vec3d.ZERO;
		} else {
			Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply((double) speed);
			float f = MathHelper.sin(yaw * 0.017453292F);
			float g = MathHelper.cos(yaw * 0.017453292F);
			return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
		}
	}

	@Inject(at = {
			@At("HEAD") }, method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable = true)
	private void onIsInvisibleCheck(PlayerEntity message, CallbackInfoReturnable<Boolean> cir) {
		if (Payload.getInstance().moduleManager.playeresp.state.getValue() && Payload.getInstance().moduleManager.playeresp.antiInvis.getValue()) {
			cir.setReturnValue(false);
		}
	}


	@Inject(method = "move", at = @At("HEAD"), cancellable = false)
	private void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (Payload.getInstance().moduleManager.entitySpeed.state.getValue()) {
			if ((Object) this == MC.player) {
				Payload.getInstance().eventManager.Fire(PlayerMoveEvent.get(type, movement));
			} else if ((Object) this instanceof LivingEntity) {
				Payload.getInstance().eventManager.Fire(LivingEntityMoveEvent.get((LivingEntity) (Object) this, movement));
			}
		}
	}

	@Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
	void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
		EntityESP esp = Payload.getInstance().moduleManager.entityesp;
		PlayerESP pesp = Payload.getInstance().moduleManager.playeresp;

		if (pesp != null && pesp.state.getValue() && pesp.mode.getValue() == PlayerESP.Mode.Glow) {
			Entity entity = (Entity) (Object) this;
			if (entity instanceof PlayerEntity && entity != MC.player) {
				cir.setReturnValue(true);
			}
		}

		if (esp != null && esp.state.getValue() && esp.returnmode() == EntityESP.Mode.Glow) {

				Entity entity = (Entity) (Object) this;

				if (entity instanceof PlayerEntity || entity.getControllingPassenger() == MC.player || entity instanceof ItemFrameEntity) {
					return;
				}

				if (entity instanceof AnimalEntity && esp.showPassiveEntities.getValue()) {
					cir.setReturnValue(true); // Passive entities glow
					return;
				}
				if (entity instanceof Monster && esp.showEnemies.getValue()) {
					cir.setReturnValue(true); // Hostile mobs glow
					return;
				}

				if (entity instanceof ItemEntity && esp.showItems.getValue()) {
					cir.setReturnValue(true); // Items glow
					return;
				}

				if (!(entity instanceof AnimalEntity || entity instanceof Monster || entity instanceof ItemEntity)
						&& esp.showMiscEntities.getValue()) {
					cir.setReturnValue(true); // Miscellaneous entities glow
					return;
				}
			}
		}

	@Inject(
			method = {"getPose"},
			at = {@At("HEAD")},
			cancellable = true
	)
	private void getPoseHook(CallbackInfoReturnable<EntityPose> info) {
		// Check if the current entity is the player
		Entity entity = (Entity) (Object) this;

		if (entity == MC.player) {
			// Check if the ElytraFly module is active and can use packet flying
			ElytraPacket elytraPacket = Payload.getInstance().moduleManager.elytraPacket;

			if (elytraPacket.canPacketEfly()) {
				// Override the pose to "STANDING"
				info.setReturnValue(EntityPose.STANDING);
			}
		}

		if ((Object) this != MC.player) return;

		ItemStack chest = MC.player.getEquippedStack(EquipmentSlot.CHEST);

		if (chest.getItem() != Items.ELYTRA) {
			return;
		} else if (Payload.getInstance().moduleManager.elytraBounce.state.getValue() && MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
			info.setReturnValue(EntityPose.GLIDING);
		}
	}

	@Inject(at = { @At("HEAD") }, method = "getStepHeight()F", cancellable = true)
	public void onGetStepHeight(CallbackInfoReturnable<Float> cir) {
		return;
	}

	@Inject(at = { @At("HEAD") }, method = "getJumpVelocityMultiplier()F", cancellable = true)
	public void onGetJumpVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
		return;
	}

	@Inject(at = { @At("HEAD") }, method = "changeLookDirection(DD)V", cancellable = true)
	public void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {

	}

	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		if ((Object) this != MC.player) return;

		Freelook freelook = Payload.getInstance().moduleManager.freelook;
		if (freelook.state.getValue()) {
			freelook.cameraYaw += (float) (cursorDeltaX / 8);
			freelook.cameraPitch += (float) (cursorDeltaY / 8);

			if (Math.abs(freelook.cameraPitch) > 90.0F) freelook.cameraPitch = freelook.cameraPitch > 0.0F ? 90.0F : -90.0F;
			ci.cancel();
		}
	}

	@Inject(method = "isInLava", at = @At("HEAD"), cancellable = true)
	public void isInLavaHook(CallbackInfoReturnable<Boolean> cir) {
		if ((Payload.getInstance().moduleManager.noWaterCollision.state.getValue()) && MC.player != null && ((Entity) (Object) this).getId() == MC.player.getId()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isTouchingWater", at = @At("HEAD"), cancellable = true)
	public void isTouchingWaterHook(CallbackInfoReturnable<Boolean> cir) {
		if ((Payload.getInstance().moduleManager.noWaterCollision.state.getValue()) && MC.player != null && ((Entity) (Object) this).getId() == MC.player.getId()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "setSwimming", at = @At("HEAD"), cancellable = true)
	public void setSwimmingHook(boolean swimming, CallbackInfo ci) {
		if ((Payload.getInstance().moduleManager.noWaterCollision.state.getValue()) && MC.player != null && ((Entity) (Object) this).getId() == MC.player.getId()) {
			ci.cancel();
		}
	}
	
}
