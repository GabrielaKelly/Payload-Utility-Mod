
package net.payload.module.modules.misc;

import net.minecraft.particle.ParticleTypes;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.math.CacheTimer;

public class PlayerTrail extends Module implements TickListener {

    public enum Mode {
        Bubbles, Firework, Flame, Explosion, Heart, Gust, Criticals, Smoke, Angry, Dragon, Ink
    }

    public BooleanSetting moveonly = BooleanSetting.builder()
            .id("playertrail_move")
            .displayName("On move")
            .description("only spawns particles on movement")
            .defaultValue(true)
            .build();

    public BooleanSetting headspawn = BooleanSetting.builder()
            .id("playertrail_headspawn")
            .displayName("Head spawning")
            .description("spawn on head")
            .defaultValue(false)
            .build();

    public BooleanSetting bodyspawn = BooleanSetting.builder()
            .id("playertrail_bodyspawn")
            .displayName("Body spawning")
            .description("spawn on body")
            .defaultValue(false)
            .build();

    private final EnumSetting<PlayerTrail.Mode> mode = EnumSetting.<PlayerTrail.Mode>builder()
            .id("playertrail_mode")
            .displayName("Mode")
            .description("Trail mode")
            .defaultValue(Mode.Flame)
            .build();

    private FloatSetting delay = FloatSetting.builder().id("playertrail_delay").displayName("Spawn Delay")
            .description("delay")
            .defaultValue(0.2f)
            .minValue(0f)
            .maxValue(1f)
            .step(0.05f)
            .build();

    private double Y;

    private final CacheTimer spawnDelayTimer = new CacheTimer();

    public PlayerTrail() {
        super("PlayerTrail");
        this.setCategory(Category.of("Misc"));
        this.setDescription("(clientside) puts trail behind you");
        this.addSetting(mode);
        this.addSetting(delay);
        this.addSetting(moveonly);
        this.addSetting(headspawn);
        this.addSetting(bodyspawn);
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        if (moveonly.getValue()) {
            if (MC.options.forwardKey.isPressed() || MC.options.backKey.isPressed() || MC.options.leftKey.isPressed() || MC.options.rightKey.isPressed()) {
                ParticleSpawn();
            }
        }
        else ParticleSpawn();
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onToggle() {
    }

    public void ParticleSpawn() {
        if (nullCheck()) return;

        if (MC.player != null && MC.world != null && spawnDelayTimer.passed(delay.getValue() * 1000)) {

        if (headspawn.getValue()) {
            Y = MC.player.getY() + 2;
        }
        else if (bodyspawn.getValue()) {
            Y = MC.player.getY() + 1;
        }
        else Y = MC.player.getY();

        switch (mode.getValue()) {
                case Mode.Bubbles -> MC.world.addParticle(ParticleTypes.BUBBLE, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Firework -> MC.world.addParticle(ParticleTypes.FIREWORK, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Flame -> MC.world.addParticle(ParticleTypes.FLAME, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Explosion -> MC.world.addParticle(ParticleTypes.EXPLOSION, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Heart -> MC.world.addParticle(ParticleTypes.HEART, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Ink -> MC.world.addParticle(ParticleTypes.SQUID_INK, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Gust -> MC.world.addParticle(ParticleTypes.GUST, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Criticals -> MC.world.addParticle(ParticleTypes.CRIT, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Smoke -> MC.world.addParticle(ParticleTypes.SMOKE, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Angry -> MC.world.addParticle(ParticleTypes.ANGRY_VILLAGER, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
                case Mode.Dragon -> MC.world.addParticle(ParticleTypes.DRAGON_BREATH, MC.player.getX(), Y, MC.player.getZ(), 0, 0, 0);
            }
            spawnDelayTimer.reset();
        }
    }

}