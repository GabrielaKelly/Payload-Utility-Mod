

package net.payload.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.payload.event.events.Render3DEvent;
import net.payload.utils.render.Render3D;
import net.payload.utils.render.TextUtils;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.payload.Payload;
import net.payload.module.modules.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Shadow
	private Camera camera;

	@Shadow
	private MinecraftClient client;

	@Shadow
	private float fovMultiplier;

	@Shadow
	private float lastFovMultiplier;

	@Shadow
	private boolean renderingPanorama;

	@Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
	private void updateTargetedEntityHook(float tickDelta, CallbackInfo ci) {
		ci.cancel();
		update(tickDelta);
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
	void render3dHook(RenderTickCounter renderTickCounter, CallbackInfo ci) {
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		MatrixStack matrixStack = new MatrixStack();
		RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

		//RenderSystem.assertOnRenderThread();

		TextUtils.lastProjMat.set(RenderSystem.getProjectionMatrix());
		TextUtils.lastModMat.set(RenderSystem.getModelViewMatrix());
		TextUtils.lastWorldSpaceMatrix.set(matrixStack.peek().getPositionMatrix());


		RenderSystem.getModelViewStack().popMatrix();

		//RenderSystem.assertOnRenderThread();
	}

	@Unique
	public void update(float tickDelta) {
		Entity entity = this.client.getCameraEntity();
		if (entity != null) {
			if (this.client.world != null) {
				Profiler profiler = Profilers.get();

				profiler.push("pick");
				this.client.targetedEntity = null;
				double d;
				if (!Payload.getInstance().moduleManager.reach.state.getValue()) {
					d = 5;
				}
				else {
					d = Payload.getInstance().moduleManager.reach.getReach();
				}
				Payload.getInstance().moduleManager.interactTweaks.isActive = Payload.getInstance().moduleManager.interactTweaks.ghostHand();
				this.client.crosshairTarget = entity.raycast(d, tickDelta, false);
				Payload.getInstance().moduleManager.interactTweaks.isActive = false;
				Vec3d vec3d = entity.getCameraPosVec(tickDelta);
				boolean bl = false;
				double e = d;

				e *= e;
				if (this.client.crosshairTarget != null) {
					e = this.client.crosshairTarget.getPos().squaredDistanceTo(vec3d);
				}

				Vec3d vec3d2 = entity.getRotationVec(1.0F);
				Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
				Box box = entity.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0, 1.0, 1.0);

				if (!Payload.getInstance().moduleManager.interactTweaks.noEntityTrace()) {
					EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, (entityx) -> !entityx.isSpectator() && entityx.canHit(), e);
					if (entityHitResult != null) {
						Entity entity2 = entityHitResult.getEntity();
						Vec3d vec3d4 = entityHitResult.getPos();
						double g = vec3d.squaredDistanceTo(vec3d4);
						if (bl && g > 9.0) {
							this.client.crosshairTarget = BlockHitResult.createMissed(vec3d4, Direction.getFacing(vec3d2.x, vec3d2.y, vec3d2.z), BlockPos.ofFloored(vec3d4));
						} else if (g < e || this.client.crosshairTarget == null) {
							this.client.crosshairTarget = entityHitResult;
							if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrameEntity) {
								this.client.targetedEntity = entity2;
							}
						}
					}
				}

				profiler.pop();
			}
		}
	}

	@Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
	public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
		if (!this.renderingPanorama && (Payload.getInstance().moduleManager.fov.state.getValue() || Payload.getInstance().moduleManager.zoom.state.getValue() )) {
			float d = 70.0f;
			if (changingFov) {
				if (Payload.getInstance().moduleManager.fov.state.getValue()) {
					float fov = Payload.getInstance().moduleManager.fov.customFov.getValue();

					if (Payload.getInstance().moduleManager.zoom.state.getValue()) {
						cir.setReturnValue(Math.min(Math.max(fov - Payload.getInstance().moduleManager.zoom.zoomFactor.getValue(), 1), 177));
					} else {
						cir.setReturnValue(fov);
					}
					return;
				}
				d = this.client.options.getFov().getValue();
				d *= MathHelper.lerp(tickDelta, this.lastFovMultiplier, this.fovMultiplier);
				if (Payload.getInstance().moduleManager.zoom.state.getValue()) {
					d = (Math.min(Math.max(d - Payload.getInstance().moduleManager.zoom.zoomFactor.getValue(), 1), 177));
				}
			} else {
				if (Payload.getInstance().moduleManager.fov.state.getValue()) {
					cir.setReturnValue(Payload.getInstance().moduleManager.fov.itemFov.getValue());
					return;
				}
			}

			if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).isDead()) {
				float f = Math.min((float)((LivingEntity)camera.getFocusedEntity()).deathTime + tickDelta, 20.0F);
				d /= (1.0F - 500.0F / (f + 500.0F)) * 2.0F + 1.0F;
			}

			CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
			if (cameraSubmersionType == CameraSubmersionType.LAVA || cameraSubmersionType == CameraSubmersionType.WATER) {
				d *= MathHelper.lerp(this.client.options.getFovEffectScale().getValue(), 1.0, 0.85714287F);
			}

			cir.setReturnValue(d);
		}
	}

	@Inject(at = { @At("HEAD") }, method = {
			"bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V" }, cancellable = true)
	private void onBobViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo ci) {
		if (Payload.getInstance().moduleManager.noBob.state.getValue()) {
			ci.cancel();
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0), method = {
			"renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V" })
	private float nauseaLerp(float delta, float first, float second) {
		if (Payload.getInstance().moduleManager.norender.state.getValue()) {
			return 0;
		}
		return MathHelper.lerp(delta, first, second);
	}

	@Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
	private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
		NoRender norender = (NoRender) Payload.getInstance().moduleManager.norender;
		if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING && norender.state.getValue()
				&& norender.getNoTotemAnimation()) {
			info.cancel();
		}
	}
}
