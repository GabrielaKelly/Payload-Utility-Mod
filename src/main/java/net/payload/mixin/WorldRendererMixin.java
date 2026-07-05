

package net.payload.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.payload.Payload;
import net.payload.gui.colors.Color;
import net.payload.module.modules.render.EntityESP;
import net.payload.module.modules.render.PlayerESP;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.event.events.Render3DEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static net.payload.PayloadClient.MC;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Shadow
	private Frustum frustum;

	public boolean sillyval;

	@ModifyExpressionValue(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"))
	private boolean onShouldGlow(boolean original, @Local Entity entity) {
		EntityESP esp = Payload.getInstance().moduleManager.entityesp;
		PlayerESP playerEsp = Payload.getInstance().moduleManager.playeresp;
		if (entity == null) return original;

		if (entity instanceof PlayerEntity player) {
			if (playerEsp != null &&
					playerEsp.state.getValue() && playerEsp.mode.getValue() == PlayerESP.Mode.Glow && player != MC.player) {
				return true;
			}
		}
		else if (esp != null &&
				esp.state.getValue() &&
				esp.shouldGlow(entity) &&
				!esp.shouldSkip(entity)) {
			return true;
		}

		return false;
	}

	@WrapOperation(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V"))
	private void onSetGlowColor(OutlineVertexConsumerProvider instance,
								int red, int green, int blue, int alpha,
								Operation<Void> original,
								@Local LocalRef<Entity> entityRef) {
		Entity entity = entityRef.get();
		if (entity == null) {
			original.call(instance, red, green, blue, alpha);
			return;
		}


		EntityESP esp = Payload.getInstance().moduleManager.entityesp;
		PlayerESP playerEsp = Payload.getInstance().moduleManager.playeresp;

		if (entity instanceof PlayerEntity player) {
			if (playerEsp != null &&
					playerEsp.state.getValue() &&
					playerEsp.mode.getValue() == PlayerESP.Mode.Glow &&
					player != MC.player) {

				Color color = determinePlayerColor(playerEsp, player);
				applyColor(instance, color);
				return;
			}
		}
		else if (!(entity instanceof ItemFrameEntity) &&
				esp != null &&
				esp.state.getValue() &&
				esp.shouldGlow(entity) &&
				!esp.shouldSkip(entity)) {

			Color color = esp.getColor(entity);
			if (color != null) {
				applyColor(instance, color);
				return;
			}
		}

		original.call(instance, red, green, blue, alpha);
	}

	// Helper method to determine player color
	private Color determinePlayerColor(PlayerESP playerEsp, PlayerEntity player) {
		return Payload.getInstance().friendsList.contains(player)
				? playerEsp.color_friendly.getValue()
				: playerEsp.color_default.getValue();
	}

	// Helper method to apply color safely
	private void applyColor(OutlineVertexConsumerProvider instance, Color color) {
		instance.setColor(
				color.getRedInt(),
				color.getGreenInt(),
				color.getBlueInt(),
				color.getAlphaInt()
		);
	}

	/*
	@ModifyExpressionValue(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"))
	private boolean onShouldGlow(boolean original, @Local Entity entity) {

		EntityESP esp = Payload.getInstance().moduleManager.entityesp;

		if (entity instanceof PlayerEntity) {
			PlayerESP playerEsp = Payload.getInstance().moduleManager.playeresp;
			if (playerEsp != null &&
					playerEsp.state.getValue() && playerEsp.mode.getValue() == PlayerESP.Mode.Glow && entity != MC.player) {
				return true;
			}
		}

		else if (esp.state.getValue() && esp.shouldGlow(entity) && !esp.shouldSkip(entity)) {
			return esp.getColor(entity) != null || original;
		}

		return original;
	}

	 */

	@Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", cancellable = false)
	public void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
					   Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix,
					   CallbackInfo ci) {

		MinecraftClient MC = gameRenderer.getClient();
		Framebuffer oldBuffer = MC.getFramebuffer();
		oldBuffer.endWrite();

		Framebuffer frameBuffer = Payload.getInstance().guiManager.getFrameBuffer();
		frameBuffer.resize(MC.getWindow().getFramebufferWidth(), MC.getWindow().getFramebufferHeight());
		frameBuffer.beginWrite(false);

		if (Payload.getInstance().moduleManager != null) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO,
					GlStateManager.DstFactor.ONE);
			RenderSystem.disableDepthTest();
			RenderSystem.disableCull();
			GL11.glEnable(GL11.GL_LINE_SMOOTH);

			RenderSystem.getModelViewStack().pushMatrix().mul(positionMatrix);

			MatrixStack matrixStack = new MatrixStack();
			Render3DEvent renderEvent = new Render3DEvent(matrixStack, frustum, camera, tickCounter);
			Payload.getInstance().eventManager.Fire(renderEvent);

			RenderSystem.getModelViewStack().popMatrix();

			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			RenderSystem.enableDepthTest();
			RenderSystem.disableBlend();

		}

		frameBuffer.endWrite();
		oldBuffer.beginWrite(false);

		// Write frame buffer to the main framebuffer.
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO,
				GlStateManager.DstFactor.ONE);
		frameBuffer.drawInternal(MC.getWindow().getFramebufferWidth(), MC.getWindow().getFramebufferHeight());
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}

	@Inject(at = @At("HEAD"), method = "hasBlindnessOrDarkness(Lnet/minecraft/client/render/Camera;)Z", cancellable = true)
	private void onHasBlindnessOrDarknessEffect(Camera camera, CallbackInfoReturnable<Boolean> cir) {
		if (Payload.getInstance().moduleManager.norender.state.getValue()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
	private void onRenderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci) {
		if (Payload.getInstance().moduleManager.norender.state.getValue() && Payload.getInstance().moduleManager.norender.getNoWeather()) ci.cancel();
	}
/*
	@WrapOperation(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V"))
	private void onSetGlowColor(OutlineVertexConsumerProvider instance, int red, int green, int blue, int alpha, Operation<Void> original, @Local LocalRef<Entity> entity) {

		EntityESP esp = Payload.getInstance().moduleManager.entityesp;

		// PlayerESP logic
		if (entity.get() instanceof PlayerEntity player) {
			PlayerESP playerEsp = Payload.getInstance().moduleManager.playeresp;
			if (playerEsp != null &&
					playerEsp.state.getValue() &&
					playerEsp.mode.getValue() == PlayerESP.Mode.Glow &&
					player != MC.player) {

				Color color = Payload.getInstance().friendsList.contains(player)
						? playerEsp.color_friendly.getValue()
						: playerEsp.color_default.getValue();

				instance.setColor(
						color.getRedInt(),
						color.getGreenInt(),
						color.getBlueInt(),
						color.getAlphaInt()
				);
				return;
			}
		}

		// Existing EntityESP logic
		else if (esp.state.getValue() && esp.shouldGlow(entity.get()) && !esp.shouldSkip(entity.get())) {
			Color color = esp.getColor(entity.get());
			if (color != null) {
				instance.setColor(color.getRedInt(), color.getGreenInt(), color.getBlueInt(), color.getAlphaInt());
				return;
			}
		}

		original.call(instance, red, green, blue, alpha);
	}


 */
	@Inject(at = @At("HEAD"), method = "drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;DDDLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V", cancellable = true)
	private void onDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX,
			double cameraY, double cameraZ, BlockPos pos, BlockState state, int color, CallbackInfo ci) {
		if (Payload.getInstance().moduleManager.freecam.state.getValue())
			ci.cancel();
	}
}
