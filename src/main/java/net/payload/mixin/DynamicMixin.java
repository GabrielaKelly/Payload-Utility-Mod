package net.payload.mixin;

import net.payload.Payload;
import net.payload.module.modules.exploit.FluxTimer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.PayloadClient;
import net.payload.module.modules.exploit.Timer;
import net.minecraft.client.render.RenderTickCounter.Dynamic;

@Mixin(Dynamic.class)
public class DynamicMixin {
	@Shadow
	private float lastFrameDuration;

	@Inject(at = {
			@At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J", opcode = Opcodes.PUTFIELD, ordinal = 0)}, method = {
			"beginRenderTick(J)I"})
	public void onBeginRenderTick(long long_1, CallbackInfoReturnable<Integer> cir) {
		PayloadClient payload = Payload.getInstance();
		if (payload.moduleManager != null) {
			Timer timer = (Timer) Payload.getInstance().moduleManager.timer;
			FluxTimer fluxTimer = (FluxTimer) Payload.getInstance().moduleManager.fluxTimer;
			if (timer.state.getValue()) {
				lastFrameDuration *= timer.getMultiplier();
			}

			else if (!timer.state.getValue() && fluxTimer.state.getValue() && !fluxTimer.isNormal()) {
				lastFrameDuration *= fluxTimer.getMultiplier();
			}

			else if (!timer.state.getValue() && fluxTimer.state.getValue() && fluxTimer.isNormal()) {
				lastFrameDuration *= 1.0f;
			}
		}
	}
}

