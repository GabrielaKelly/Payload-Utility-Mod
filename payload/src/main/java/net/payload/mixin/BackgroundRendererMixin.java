package net.payload.mixin;

import net.minecraft.client.render.Camera;
import net.payload.Payload;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
	@Inject(at = { @At("HEAD") }, method = {
			"getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;" }, cancellable = true)
	private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<?> cir) {
		if (Payload.getInstance().moduleManager.norender.state.getValue() && Payload.getInstance().moduleManager.norender.getNoFog()) {
			cir.setReturnValue(null);
		}
	}

	@ModifyArgs(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Fog;<init>(FFLnet/minecraft/client/render/FogShape;FFFF)V"))
	private static void modifyFogDistance(Args args, Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta) {
		if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN && (Payload.getInstance().moduleManager.norender.state.getValue() && Payload.getInstance().moduleManager.norender.getNoFog()) || Payload.getInstance().moduleManager.xray.state.getValue()) {
			args.set(0, viewDistance * 4);
			args.set(1, viewDistance * 4.25f);
		}
	}
}