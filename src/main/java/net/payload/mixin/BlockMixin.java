

package net.payload.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.Payload;
import net.payload.PayloadClient;
import net.payload.module.modules.render.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.Direction;

@Mixin(Block.class)
public abstract class BlockMixin implements ItemConvertible {

	@Inject(at = { @At("HEAD") }, method = {
			"shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z" }, cancellable = true)
	private static void onShouldDrawSide(BlockState state, BlockState otherState, Direction side,
										 CallbackInfoReturnable<Boolean> cir) {
		PayloadClient payload = Payload.getInstance();
		XRay xray = (XRay) payload.moduleManager.xray;
		if (xray.state.getValue()) {
			cir.setReturnValue(xray.isXRayBlock(state.getBlock()));
		}
	}

	@Inject(at = { @At("HEAD") }, method = { "getVelocityMultiplier()F" }, cancellable = true)
	private void onGetVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
		if (Payload.getInstance().moduleManager.noslowdown.soulSand()) {
			if (cir.getReturnValueF() < 1.0f)
				cir.setReturnValue(1F);
		}
	}
}
