package net.payload.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.EntityRemoveEvent;
import net.payload.event.events.EntitySpawnEvent;
import net.payload.module.modules.render.WorldTweaks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.payload.PayloadClient.MC;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Shadow @Nullable public abstract Entity getEntityById(int id);

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        WorldTweaks worldTweaks = Payload.getInstance().moduleManager.worldTweaks;

        if (worldTweaks.state.getValue() && worldTweaks.cskycolor.getValue()) {
            cir.setReturnValue(worldTweaks.skycolor.getValue().getColorAsInt());
        }
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    public void addEntityHook(Entity entity, CallbackInfo ci) {
        if(MC.player == null) return;

        EntitySpawnEvent ees = new EntitySpawnEvent(entity);
        Payload.getInstance().eventManager.Fire(ees);
        if (ees.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo info) {
        if (getEntityById(entityId) != null) {

            EntityRemoveEvent ere = new EntityRemoveEvent(getEntityById(entityId), removalReason);
            Payload.getInstance().eventManager.Fire(ere);
        }
    }
}
