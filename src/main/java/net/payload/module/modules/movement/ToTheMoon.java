package net.payload.module.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.utils.math.CacheTimer;

public class ToTheMoon extends Module implements TickListener {

    private final CacheTimer engineTimer = new CacheTimer();

    int engineStart = 0;

    public ToTheMoon() {
        super("ToTheMoon");
        this.setName("ToTheMoon");
        this.setCategory(Category.of("Movement"));
        this.setDescription("Turn me on and find out");

    }

    @Override
    public void onToggle() {
    }


    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        engineStart = 0;
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);

        if (nullCheck()) return;

        ClientPlayerEntity player = MC.player;

        if (player.getVehicle() instanceof BoatEntity) {
            engineStart = 1;
            this.engineTimer.reset();
            MC.player.sendMessage(Text.of("Starting Engines...."), true);
            MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_LEVER_CLICK, 1f, 1f);


        } else {
            MC.player.sendMessage(Text.of("Please sit in a boat and re-enable."), true);
            this.toggle();
        }


    }

    @Override
    public void onTick(TickEvent.Pre event) {

        if (nullCheck()) return;

        if (MC.player.getVehicle() instanceof BoatEntity && (engineStart == 1)) {
            if (this.engineTimer.passed(1000)) {
                MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_LADDER_BREAK, 3f, 0.5f);
            }

            if (this.engineTimer.passed(5000)) {
                MC.player.sendMessage(Text.of("Initializing boosters..."), true);
                MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_BONE_BLOCK_BREAK, 3f, 0.3f);
            }

            if (this.engineTimer.passed(9000)) {
                MC.player.sendMessage(Text.of("Take off in: 5"), true);
            }

            if (this.engineTimer.passed(10000)) {
                MC.player.sendMessage(Text.of("Take off in: 4"), true);
            }

            if (this.engineTimer.passed(11000)) {
                MC.player.sendMessage(Text.of("Take off in: 3"), true);
            }

            if (this.engineTimer.passed(12000)) {
                MC.player.sendMessage(Text.of("Take off in: 2"), true);
            }

            if (this.engineTimer.passed(13000)) {
                MC.player.sendMessage(Text.of("Take off in: 1"), true);
            }

            if (this.engineTimer.passed(15000) && engineStart == 1) {
                MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, 3f, 1f);

                    MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_LAVA_POP, 3f, 0.2f);
                    BoatEntity boat = (BoatEntity) MC.player.getVehicle();

                    boat.addVelocity(0, 0.05, 0);

                    boat.setYaw(boat.getYaw() + 10.0f);

                    MC.world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, MC.player.getX(), MC.player.getY() + 0.5f, MC.player.getZ(), 0, -0.5f, 0);
                    MC.world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, MC.player.getX() + 0.2, MC.player.getY() + 0.5f, MC.player.getZ(), 0, -0.5f, 0);
                    MC.world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, MC.player.getX() - 0.2, MC.player.getY() + 0.5f, MC.player.getZ(), 0, -0.5f, 0);

                    MC.world.addParticle(ParticleTypes.FLAME, MC.player.getX(), MC.player.getY() + 0.5f, MC.player.getZ(), 0, -0.5f, 0);
                    MC.world.addParticle(ParticleTypes.FLAME, MC.player.getX() + 0.2, MC.player.getY() + 0.5f, MC.player.getZ(), 0, -0.5f, 0);
                    MC.world.addParticle(ParticleTypes.FLAME, MC.player.getX() - 0.2, MC.player.getY() + 0.5f, MC.player.getZ(), 0, -0.5f, 0);
            }

        }
    }

    @Override
    public void onTick(TickEvent.Post event) {
        // Post-tick logic can be added here if needed
    }
}
