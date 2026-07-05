package net.payload.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.module.modules.render.Nametags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.payload.PayloadClient.MC;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin
		implements SynchronousResourceReloader {

	/**
	 * TODO: Fish, make entity nametags for unnamed.
	 */

	@WrapOperation(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),
			method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V")
	private <E extends Entity, S extends EntityRenderState> void wrapRender(
			EntityRenderer<? super E, S> renderer, S state, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light,
			Operation<Void> original, E entity, double x, double y, double z,
			float tickDelta, MatrixStack matrices2,
			VertexConsumerProvider vertexConsumers2, int light2,
			EntityRenderer<? super E, S> renderer2) {

		Text originalDisplayName = state.displayName;
		Nametags healthTags = Payload.getInstance().moduleManager.nametags;

		if (healthTags.state.getValue() && healthTags.isMinecraft() && MC.player != null && MC.world != null) {
			// Handle player nametags
			if (healthTags.shouldForcePlayerNametags() && entity instanceof PlayerEntity pe && originalDisplayName != null && healthTags.showHealth()) {
					state.displayName = healthTags.addHealth(pe, originalDisplayName.copy());
			}

			// Handle mob nametags (health)
			if (entity instanceof LivingEntity le && !(entity instanceof PlayerEntity) && originalDisplayName != null && healthTags.showHealth()) {
				state.displayName = healthTags.addHealth(le, originalDisplayName.copy());
			}
		}

		// Call the original method
		original.call(renderer, state, matrices, vertexConsumers, light);
		state.displayName = originalDisplayName;  // Restore original display name

	}

	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void extendItemRenderDistance(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (Payload.getInstance().moduleManager.itemTags.state.getValue()) {
			if (entity instanceof ItemEntity) {
				Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
				Vec3d cameraPos = camera.getPos();

				double distSq = entity.getPos().squaredDistanceTo(cameraPos);

				if (distSq <= 256 * 256) {
					if (frustum.isVisible(entity.getBoundingBox())) {
						cir.setReturnValue(true);
					}
				}
			}
		}
	}
}