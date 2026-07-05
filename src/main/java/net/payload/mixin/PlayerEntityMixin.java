

package net.payload.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.TravelEvent;
import net.payload.module.modules.misc.InteractTweaks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.PayloadClient;
import net.payload.module.modules.combat.Reach;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.tag.FluidTags;

import static net.payload.PayloadClient.MC;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {

	@Shadow
	private PlayerInventory inventory;

	@Shadow
	public abstract boolean isSpectator();

    @Shadow @Final protected static TrackedData<Byte> PLAYER_MODEL_PARTS;

    @Inject(method = "getAttackCooldownProgress", at = @At("HEAD"), cancellable = true)
	public void onGetAttackCooldownProgress(float baseTime, CallbackInfoReturnable<Float> cir) {
		PayloadClient PAYLOAD = Payload.getInstance();

		if (Payload.getInstance().moduleManager.noAttackDelay.state.getValue()) {
			//Fish basetime
			baseTime = Payload.getInstance().moduleManager.noAttackDelay.getBaseTime();
			cir.setReturnValue(Math.min(1.0F, baseTime * 0.5f));
		}
	}

	@Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
	public void onGetBlockBreakingSpeed(BlockState blockState, CallbackInfoReturnable<Float> ci) {
		InteractTweaks it = Payload.getInstance().moduleManager.interactTweaks;

		// If fast break is enabled.
		if (it.state.getValue() && it.fastBreak.getValue()) {
			// Multiply the break speed and override the return value.
			float speed = inventory.getBlockBreakingSpeed(blockState);

			speed *= it.fastBreakFloat.getValue();

			if (isSubmergedIn(FluidTags.WATER) || isSubmergedIn(FluidTags.LAVA) || !isOnGround()) {
					speed /= 5.0F;
			}

			ci.setReturnValue(speed);
		}
	}

	@Inject(at = {@At("HEAD")}, method = "getOffGroundSpeed()F", cancellable = true)
	protected void onGetOffGroundSpeed(CallbackInfoReturnable<Float> cir) {
		return;
	}

	@Inject(at = {@At("HEAD")}, method = "getBlockInteractionRange()D", cancellable = true)
	private void onBlockInteractionRange(CallbackInfoReturnable<Double> cir) {
		if (Payload.getInstance().moduleManager.reach.state.getValue()) {
			Reach reach = (Reach) Payload.getInstance().moduleManager.reach;
			cir.setReturnValue((double) reach.getReach());
		}
	}

	@Inject(at = {@At("HEAD")}, method = "getEntityInteractionRange()D", cancellable = true)
	private void onEntityInteractionRange(CallbackInfoReturnable<Double> cir) {
		if (Payload.getInstance().moduleManager.reach.state.getValue()) {
			Reach reach = (Reach) Payload.getInstance().moduleManager.reach;
			cir.setReturnValue((double) reach.getReach());
		}
	}

	@Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
	protected void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
		if (Payload.getInstance().moduleManager.scaffold.state.getValue() && Payload.getInstance().moduleManager.scaffold.getSafewalk()) {
			info.setReturnValue(true);
		}
		else if (Payload.getInstance().moduleManager.scaffold.state.getValue() && Payload.getInstance().moduleManager.scaffold.getSafewalk()) {
			info.setReturnValue(true);
		}
		else if (Payload.getInstance().moduleManager.safewalk.state.getValue()) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onEntityTick(CallbackInfo ci) {
		if (MC.player != null && Payload.getInstance().moduleManager.nofall.state.getValue()) {
			if (Payload.getInstance().moduleManager.nofall.getFallingSpoofMode()) {
				if (getType() == MC.player.getType()) {
					float currentFallDistance = this.fallDistance;
					if (currentFallDistance > Payload.getInstance().moduleManager.nofall.getFallDist() || Payload.getInstance().moduleManager.nofall.alwaysOn.getValue()) {
						this.fallDistance = 0.0f;
					}
				}
			}
		}
	}
	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void onTravelPre(Vec3d movementInput, CallbackInfo ci) {
		PlayerEntity player = (PlayerEntity) (Object) this;
		if(player != MC.player)
			return;

		TravelEvent.Pre event = new TravelEvent.Pre(player);
		Payload.getInstance().eventManager.Fire(event);
		if (event.isCancelled()) {
			ci.cancel();
			TravelEvent.Post eventp = new TravelEvent.Post(player);
			Payload.getInstance().eventManager.Fire(eventp);
		}
	}

	@Inject(method = "travel", at = @At("RETURN"))
	private void onTravelPost(Vec3d movementInput, CallbackInfo ci) {
		PlayerEntity player = (PlayerEntity) (Object) this;
		if(player != MC.player)
			return;

		TravelEvent.Post event = new TravelEvent.Post(player);
		Payload.getInstance().eventManager.Fire(event);
	}
}
