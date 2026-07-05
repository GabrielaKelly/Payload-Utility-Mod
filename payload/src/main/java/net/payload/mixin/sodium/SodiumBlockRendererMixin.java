package net.payload.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.payload.Payload;
import net.payload.PayloadClient;
import net.payload.module.modules.render.XRay;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;

@Mixin(value = BlockRenderer.class)
public abstract class SodiumBlockRendererMixin {
	@Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
	private void onRenderModel(BakedModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
		PayloadClient payload = Payload.getInstance();
		XRay xray = (XRay) payload.moduleManager.xray;
		if (xray.state.getValue() && !xray.isXRayBlock(state.getBlock())) {
			ci.cancel();
		}
	}
}