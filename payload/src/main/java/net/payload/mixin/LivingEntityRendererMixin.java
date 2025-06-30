package net.payload.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.payload.Payload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.payload.PayloadClient.MC;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

	@Inject(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/MinecraftClient;getInstance()Lnet/minecraft/client/MinecraftClient;",
			ordinal = 0),
			method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
			cancellable = true)
	private void shouldForceLabel(LivingEntity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
		if (Payload.getInstance().moduleManager.nametags.state.getValue() && Payload.getInstance().moduleManager.nametags.isMinecraft() && MC.player != null && MC.world != null) {

			if (Payload.getInstance().moduleManager.nametags.shouldForcePlayerNametags() && entity instanceof PlayerEntity) {
				cir.setReturnValue(true);
			}
			if (Payload.getInstance().moduleManager.nametags.shouldForceMobNametags() && Payload.getInstance().moduleManager.nametags.isMinecraft() && !(entity instanceof PlayerEntity)) {
				cir.setReturnValue(true);
			}
		}
		else if (Payload.getInstance().moduleManager.nametags.state.getValue() && MC.player != null && MC.world != null) {
			cir.setReturnValue(false);
		}
	}
}
