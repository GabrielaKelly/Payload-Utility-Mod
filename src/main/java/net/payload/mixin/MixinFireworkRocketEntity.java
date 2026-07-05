package net.payload.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.particle.ParticleTypes; // class_2398
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.payload.Payload;
import net.payload.event.events.RemoveFireworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.payload.PayloadClient.MC;

@Mixin(FireworkRocketEntity.class) // Mixin for FireworkRocketEntity
public class MixinFireworkRocketEntity {

    @Shadow
    private int life;

    public MixinFireworkRocketEntity() {
        // Constructor
    }

    @Inject(
            method = {"tick"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/FireworkRocketEntity;updateRotation()V",
                    shift = At.Shift.AFTER
            )},
            cancellable = true
    )
    public void hookTickPre(CallbackInfo cir) {

        if (Payload.getInstance().moduleManager.fireworkPlus.state.getValue()) {
            RemoveFireworkEvent removeFireworkEvent = new RemoveFireworkEvent((FireworkRocketEntity) (Object) this);
            Payload.getInstance().eventManager.Fire(removeFireworkEvent);

            if (Payload.getInstance().moduleManager.fireworkPlus.isExtendingFirework() && !Payload.getInstance().moduleManager.fireworkPlus.isGrimAC()) {
                life--;
            }

            else if (removeFireworkEvent.isCancelled() && Payload.getInstance().moduleManager.fireworkPlus.isGrimAC()) {
                cir.cancel(); // Cancel the tick method

                if (this.life == 0 && !removeFireworkEvent.getRocketEntity().hasNoGravity()) { // Check specific conditions
                    MC.world.playSound((PlayerEntity) null, removeFireworkEvent.getRocketEntity().getX(), removeFireworkEvent.getRocketEntity().getY(), removeFireworkEvent.getRocketEntity().getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 3.0f, 1.0f);
                    MC.world.addParticle(ParticleTypes.FIREWORK, removeFireworkEvent.getRocketEntity().getX(), removeFireworkEvent.getRocketEntity().getY(), removeFireworkEvent.getRocketEntity().getZ(), 0, 0, 0);
                }

                if (MC.world.isClient && this.life % 2 < 2) { // Check if client-side and specific condition
                    MC.world.addParticle(ParticleTypes.FIREWORK, removeFireworkEvent.getRocketEntity().getX(), removeFireworkEvent.getRocketEntity().getY(), removeFireworkEvent.getRocketEntity().getZ(), MC.world.random.nextFloat() * 0.05, -removeFireworkEvent.getRocketEntity().getVelocity().y * 0.5, MC.world.random.nextFloat() * 0.05); // Add smoke particles
                }
            }
        }
    }
}





    /*@Inject(
            method = {"tick"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/FireworkRocketEntity;updateRotation()V",
                    shift = At.Shift.AFTER
            )},
            cancellable = true
    )
    private void hookTickPre(CallbackInfo ci) {
        PayloadClient PAYLOAD = Payload.getInstance();
        FireworkPlus fireworkPlus = (FireworkPlus) PAYLOAD.moduleManager.fireworkPlus;


        RemoveFireworkEvent removeFireworkEvent = new RemoveFireworkEvent((FireworkRocketEntity) (Object) this);
        Payload.getInstance().eventManager.Fire(removeFireworkEvent);

        if (removeFireworkEvent.isCancelled()) { // Check if the event is canceled
            ci.cancel(); // Cancel the tick method
            if (this.life == 0 && !removeFireworkEvent.getRocketEntity().hasNoGravity()) { // Check specific conditions
                MC.player.sendMessage(Text.of("Sending noise"));
                MC.world.playSound((PlayerEntity)null, removeFireworkEvent.getRocketEntity().getX(), removeFireworkEvent.getRocketEntity().getY(), removeFireworkEvent.getRocketEntity().getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 3.0f, 1.0f);
                MC.world.addParticle(ParticleTypes.FLAME, removeFireworkEvent.getRocketEntity().getX(), removeFireworkEvent.getRocketEntity().getY(), removeFireworkEvent.getRocketEntity().getZ(), 0, 0, 0);
            }
            ++this.life; // Increment the field

            if (MC.world.isClient && this.life % 2 < 2) { // Check if client-side and specific condition
                MC.player.sendMessage(Text.of("Sending particles"));
                MC.world.addParticle(ParticleTypes.SMOKE, removeFireworkEvent.getRocketEntity().getX(), removeFireworkEvent.getRocketEntity().getY(), removeFireworkEvent.getRocketEntity().getZ(), MC.world.random.nextFloat() * 0.05, -removeFireworkEvent.getRocketEntity().getVelocity().y * 0.5, MC.world.random.nextFloat() * 0.05); // Add smoke particles
            }
        }
    }
}*/
