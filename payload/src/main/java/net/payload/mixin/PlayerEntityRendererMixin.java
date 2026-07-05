package net.payload.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.payload.Payload;
import net.payload.module.modules.client.AntiAim;
import net.payload.module.modules.movement.ElytraBounce;
import net.payload.utils.Interpolation;
import net.payload.utils.rotation.RotationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import static net.payload.PayloadClient.MC;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    // Rotations
    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("RETURN"))
    private void updateRenderState$rotations(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float f, CallbackInfo info) {
        RotationManager rotman = Payload.getInstance().rotationManager;
        AntiAim antiAim = Payload.getInstance().moduleManager.antiAim;
        ElytraBounce eb = Payload.getInstance().moduleManager.elytraBounce;

        if (player == MC.player && antiAim.state.getValue() && MC.player != null) {
            float spinSpeed = antiAim.getSpinSpeed();

            state.bodyYaw += spinSpeed;
            state.pitch = antiAim.getHeadPitch();
        }
        /*
        else if (MC.player != null && player == MC.player && eb.state.getValue() && MC.player.isGliding()) {
            state.pitch = eb.getPitch();
        }

         */
        else if (MC.player != null && player == MC.player) {

            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);

            if (Payload.getInstance().moduleManager.rotations.renderYaw.getValue()) {
                state.bodyYaw = Interpolation.interpolateFloat(Payload.getInstance().rotationManager.lastYaw, Payload.getInstance().rotationManager.rotationYaw, tickDelta);
                state.yawDegrees = 0;
            }

            if (Payload.getInstance().moduleManager.rotations.renderPitch.getValue()) {
                state.pitch = Interpolation.interpolateFloat(Payload.getInstance().rotationManager.lastPitch, Payload.getInstance().rotationManager.rotationPitch, tickDelta);
            }
        }
    }
}
