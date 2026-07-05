package net.payload.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.payload.event.events.JumpEvent;
import net.payload.module.modules.movement.ElytraBounce;
import net.payload.module.modules.movement.Sprint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.Payload;
import net.payload.module.modules.combat.AntiKnockback;
import net.payload.module.modules.render.NoRender;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import static net.payload.PayloadClient.MC;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {
	@Inject(at = { @At("HEAD") }, method = "setHealth(F)V")
	public void onSetHealth(float health, CallbackInfo ci) {
		return;
	}

	@Inject(at = { @At("HEAD") }, method = "tickNewAi()V", cancellable = true)
	public void onTickNewAi(CallbackInfo ci) {

	}

	@Inject(at = {
			@At("HEAD") }, method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable = true)
	public void onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
		return;
	}

	@Inject(method = "spawnItemParticles", at = @At("HEAD"), cancellable = true)
	private void spawnItemParticles(ItemStack stack, int count, CallbackInfo info) {
		NoRender norender = (NoRender) Payload.getInstance().moduleManager.norender;
		if (norender.state.getValue() && norender.getNoEatParticles()
				&& stack.getComponents().contains(DataComponentTypes.FOOD))
			info.cancel();
	}

	@Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
	private void hookNoPush(CallbackInfo info) {
		AntiKnockback antiKnockback = Payload.getInstance().moduleManager.antiknockback;
		if (antiKnockback.state.getValue() && antiKnockback.getNoPush())
			info.cancel();
	}

	@Unique
	private boolean previousElytra = false;

	@Inject(method = "isGliding", at = @At("TAIL"), cancellable = true)
	public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
		boolean elytra = cir.getReturnValue();

		ElytraBounce elytraBounce = Payload.getInstance().moduleManager.elytraBounce;

		if (previousElytra && !elytra && elytraBounce.state.getValue() && MC.player != null) {
			cir.setReturnValue(elytraBounce.recastElytra(MC.player));
		}
		previousElytra = elytra;
	}

	@ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
	private float modifyGetYaw(float original) {
		if ((Object) this != MC.player) return original;

		Sprint s = Payload.getInstance().moduleManager.sprint;

		if (!s.returnRage() || !s.jumpFix.getValue() || !s.state.getValue()) return original;

		float forward = Math.signum(MC.player.input.movementForward);
		float strafe = 90 * Math.signum(MC.player.input.movementSideways);
		if (forward != 0) strafe *= (forward * 0.5f);

		original -= strafe;
		if (forward < 0) original -= 180;

		return original;
	}

	@ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
	private boolean modifyIsSprinting(boolean original) {

		Sprint s = Payload.getInstance().moduleManager.sprint;

		if ((Object) this != MC.player || !s.returnRage() || !s.state.getValue()) return original;

		// only add the extra velocity if you're actually moving, otherwise you'll jump in place and move forward
		return original && (Math.abs(MC.player.input.movementForward) > 1.0E-5F || Math.abs(MC.player.input.movementSideways) > 1.0E-5F);
	}

	@Inject(method = "jump", at = @At("HEAD"))
	private void onJumpPre(CallbackInfo ci) {
		JumpEvent.Pre je = new JumpEvent.Pre();

		if ((Object) this == MC.player) {
			Payload.getInstance().eventManager.Fire(je);
		}
	}

	@Inject(method = "jump", at = @At("RETURN"))
	private void onJumpPost(CallbackInfo ci) {
		JumpEvent.Post je = new JumpEvent.Post();

		if ((Object) this == MC.player) {
			Payload.getInstance().eventManager.Fire(je);
		}
	}
}
