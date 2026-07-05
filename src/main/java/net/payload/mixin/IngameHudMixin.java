

package net.payload.mixin;

import net.payload.Payload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.payload.event.events.Render2DEvent;
import net.payload.module.modules.render.NoRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;

@Mixin(InGameHud.class)
public class IngameHudMixin {
	@Inject(at = @At("HEAD"), method = "renderPlayerList(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	private void onRenderPlayerList(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		Render2DEvent renderEvent = new Render2DEvent(context, tickCounter);
		Payload.getInstance().eventManager.Fire(renderEvent);
	}

//	@Inject(at = { @At(value = "TAIL") }, method = {
//			"renderAutosaveIndicator(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V" })
//	private void onRender(DrawContext context, RenderTickCounter tickDelta, CallbackInfo ci) {
//
//	}

	@Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderVignetteOverlay(DrawContext context, Entity entity, CallbackInfo ci) {
		NoRender norender = (NoRender) Payload.getInstance().moduleManager.norender;

		if (norender.state.getValue() && norender.getNoVignette())
			ci.cancel();
	}

	@ModifyArgs(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal = 0))
	private void onRenderPumpkinOverlay(Args args) {
		NoRender norender = (NoRender) Payload.getInstance().moduleManager.norender;

		if (norender.state.getValue() && norender.getNoPumpkinOverlay())
			args.set(2, 0f);
	}

	@ModifyArgs(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal = 1))
	private void onRenderPowderSnowOverlay(Args args) {
		NoRender norender = (NoRender) Payload.getInstance().moduleManager.norender;

		if (norender.state.getValue() && norender.getNoPowderSnowOverlay())
			args.set(2, 0f);
	}

	@Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
		NoRender norender = (NoRender) Payload.getInstance().moduleManager.norender;

		if (norender.state.getValue() && norender.getNoPortalOverlay())
			ci.cancel();
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if (Payload.getInstance().guiManager.isClickGuiOpen()) {
			ci.cancel();
			return;
		}
		NoRender norender = (NoRender) Payload.getInstance().moduleManager.norender;
		if (norender.state.getValue() && norender.getNoCrosshair())
			ci.cancel();
	}
}
