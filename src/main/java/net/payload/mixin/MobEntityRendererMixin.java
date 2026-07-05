
package net.payload.mixin;

import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.entity.mob.MobEntity;
import net.payload.Payload;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.payload.PayloadClient.MC;

@Mixin(MobEntityRenderer.class)
public abstract class MobEntityRendererMixin
{
	@Inject(at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;targetedEntity:Lnet/minecraft/entity/Entity;",
		opcode = Opcodes.GETFIELD,
		ordinal = 0),
		method = "hasLabel(Lnet/minecraft/entity/mob/MobEntity;D)Z",
		cancellable = true)
	private void onHasLabel(CallbackInfoReturnable<Boolean> cir) {

		if (Payload.getInstance().moduleManager.nametags.state.getValue()) {

			if (Payload.getInstance().moduleManager.nametags.shouldForceMobNametags() && MC.player != null) {
				cir.setReturnValue(true);
			}
		}
	}

}
