package net.payload.mixin;

import net.payload.module.modules.render.Freelook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.Payload;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static net.payload.PayloadClient.MC;

@Mixin(Camera.class)
public class CameraMixin {
	@Shadow
	private boolean ready;

	@Shadow
	private BlockView area;

	@Shadow
	private boolean thirdPerson;

	@Shadow
	private float lastTickDelta;

	@Inject(at = {
			@At("HEAD") }, method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", cancellable = true)
	private void onCameraUdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView,
			float tickDelta, CallbackInfo ci) {
		if (Payload.getInstance().moduleManager.freecam.state.getValue() && MC.player != null) {
			this.ready = true;
			this.area = area;
			this.lastTickDelta = tickDelta;
			this.thirdPerson = thirdPerson;
			ci.cancel();
		}
		else if (Payload.getInstance().moduleManager.movieMode.state.getValue() && MC.player != null && Payload.getInstance().moduleManager.movieMode.movieModeCheck() && !Payload.getInstance().moduleManager.freecam.state.getValue()) {
			this.ready = true;
			this.area = area;
			this.lastTickDelta = tickDelta;
			this.thirdPerson = thirdPerson;
			ci.cancel();
		}
	}

	@Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
	private void onClipToSpace(float f, CallbackInfoReturnable<Float> cir) {
		if (Payload.getInstance().moduleManager.cameraClip.state.getValue()) {
			cir.setReturnValue(Payload.getInstance().moduleManager.cameraClip.getDistance());
		}
	}

	@Inject(at = {
			@At("HEAD") }, method = "getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;", cancellable = true)
	private void onGetSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir) {
		if (Payload.getInstance().moduleManager.movieMode.state.getValue() && Payload.getInstance().moduleManager.movieMode.movieModeCheck() && MC.player != null && !Payload.getInstance().moduleManager.freecam.state.getValue()) {
			cir.setReturnValue(CameraSubmersionType.NONE);
		}
		else if (Payload.getInstance().moduleManager.freecam.state.getValue()) {

		}
	}

	@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
	private void onUpdateSetRotationArgs(Args args) {

		Freelook freeLook = Payload.getInstance().moduleManager.freelook;

		if (freeLook.state.getValue()) {
			args.set(0, freeLook.cameraYaw);
			args.set(1, freeLook.cameraPitch);
		}
	}
}
