package net.payload.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.Payload;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

	@Inject(at = @At("TAIL"), method = {
			"getAmbientOcclusionLightLevel(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F" }, cancellable = true)
	private void onGetAmbientOcclusionLightLevel(BlockView blockView, BlockPos blockPos,
			CallbackInfoReturnable<Float> cir) {
		if (!Payload.getInstance().moduleManager.xray.state.getValue())
			return;
		cir.setReturnValue(1F);
	}
}
