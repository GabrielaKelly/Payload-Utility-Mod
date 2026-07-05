

package net.payload.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.payload.Payload;
import net.payload.event.events.AttackEntityEvent;
import net.payload.event.events.BreakBlockEvent;
import net.payload.event.events.StartBreakingBlockEvent;
import net.payload.interfaces.IClientPlayerInteractionManager;
import net.payload.module.modules.misc.BreakDelay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.payload.PayloadClient.MC;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin implements IClientPlayerInteractionManager {
	@Shadow
	protected abstract void syncSelectedSlot();

	@Shadow
	private ItemStack selectedStack;

	@Shadow
	private float currentBreakingProgress;

	@Override
	public float getCurrentBreakingProgress() {
		return this.currentBreakingProgress;
	}

	@Override
	public void setCurrentBreakingProgress(float progress) {
		this.currentBreakingProgress = progress;
	}

	@ModifyVariable(method = "isCurrentlyBreaking", at = @At("STORE"))
	private ItemStack stack(ItemStack stack) {
		if (Payload.getInstance().moduleManager.interactTweaks.state.getValue()) {
			return Payload.getInstance().moduleManager.interactTweaks.noReset() ? this.selectedStack : stack;
		}
		return stack;
	}

	@Inject(method = "cancelBlockBreaking", at = @At("HEAD"), cancellable = true)
	private void hookCancelBlockBreaking(CallbackInfo callbackInfo) {
		if (Payload.getInstance().moduleManager.interactTweaks.noAbort() && Payload.getInstance().moduleManager.interactTweaks.state.getValue())
			callbackInfo.cancel();
	}

	/*

	Meteor broke this feature for compatibility.

	@Redirect(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
	private float deltaChange (BlockState blockState, PlayerEntity player, BlockView world, BlockPos pos){
		float delta = blockState.calcBlockBreakingDelta(player, world, pos);

		if (Payload.getInstance().moduleManager.breakDelay.preventInstaBreak() && delta >= 1) {
			return 0;
		}
		return delta;
	}
	 */

	@ModifyConstant(method = "updateBlockBreakingProgress", constant = @Constant(intValue = 5))
	private int MiningCooldownFix(int value) {
		if (Payload.getInstance().moduleManager.breakDelay.state.getValue()) {
			BreakDelay bd = Payload.getInstance().moduleManager.breakDelay;

			if (bd.state.getValue() && bd.isBreaking && !bd.preventInstaBreak()) {
				return Math.round(bd.cooldownVal());

			} else if (bd.state.getValue() && bd.isBreaking && bd.preventInstaBreak()) {
				if (Math.round(bd.cooldownVal()) > 1.0f) {
					return Math.round(bd.cooldownVal());
				} else {
					return Math.round(bd.cooldownVal() + 0.9f);
				}
			} else return 5;
		}
		else return 5;
	}

	@Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
	private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> info) {

		BlockState state = MC.world.getBlockState(blockPos);

		StartBreakingBlockEvent eb = new StartBreakingBlockEvent(blockPos, state, direction);

		Payload.getInstance().eventManager.Fire(eb);

			if (eb.isCancelled()) {
				info.cancel();
			}
	}

	@Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
	private void onBreakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
		BreakBlockEvent bbe = new BreakBlockEvent(blockPos);
		Payload.getInstance().eventManager.Fire(bbe);

		if (bbe.isCancelled()) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
	private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo info) {
		AttackEntityEvent eb = new AttackEntityEvent();
		Payload.getInstance().eventManager.Fire(eb);

		if (eb.isCancelled()) {
			info.cancel();
		}
	}

	@Override
	public void payload$syncSelected() {
		syncSelectedSlot();
	}

}
