

package net.payload.module.modules.movement;

import net.minecraft.entity.effect.StatusEffects;
import net.payload.Payload;
import net.payload.event.events.PlayerMoveEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.PlayerMoveEventListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.player.MovementUtil;

import java.util.Objects;

import static net.payload.utils.player.MovementUtil.*;

public class Speed extends Module implements PlayerMoveEventListener, TickListener {

    private final SettingGroup strafeSettings;

    public enum Mode {
        Legit, Rage
    }

    private final EnumSetting<Speed.Mode> mode = EnumSetting.<Speed.Mode>builder()
            .id("speed_mode")
            .displayName("Mode")
            .description("Speed mode")
            .defaultValue(Mode.Rage)
            .build();

    private final BooleanSetting strafe = BooleanSetting.builder().id("speed_strafe").displayName("Strafe")
            .description("Allows you to strafe").defaultValue(false).build();

    private final BooleanSetting sneaking = BooleanSetting.builder().id("speed_sneak").displayName("Sneaking")
            .description("Allows you to sneak fast").defaultValue(false).build();

    private final FloatSetting intensity = FloatSetting.builder().id("speed_intensity").displayName("Intensity")
            .description("Speed intensity.").defaultValue(1f).minValue(0f).maxValue(8f).step(0.1f).build();

    private final BooleanSetting airStop = BooleanSetting.builder().id("speed_airstop").displayName("Quick Stop")
            .description("limits speed").defaultValue(true).build();

    private final BooleanSetting slowCheck = BooleanSetting.builder().id("speed_slowcheck").displayName("Slow Check")
            .description("limits speed").defaultValue(true).build();

    private final BooleanSetting inAir = BooleanSetting.builder().id("speed_inair").displayName("Falling Only")
            .description("limits speed").defaultValue(false).build();

    private final BooleanSetting autojump = BooleanSetting.builder().id("speed_jump").displayName("Auto Jump")
            .description("jumps for you").defaultValue(true).build();

    private final BooleanSetting autolowhop = BooleanSetting.builder().id("speed_lowhop").displayName("Auto LowHop")
            .description("lowhops for you").defaultValue(false).build();

    public Speed() {
        super("Speed");
        this.setCategory(Category.of("Movement"));
        this.setDescription("Makes the player able to change directions mid-air");

        strafeSettings = SettingGroup.Builder.builder()
                .id("speed_strafeoptions")
                .displayName("Strafe")
                .description("Strafe settings")
                .build();

        this.addSetting(intensity);
        this.addSetting(sneaking);
        this.addSetting(strafe);

        strafeSettings.addSetting(mode);
        strafeSettings.addSetting(slowCheck);
        strafeSettings.addSetting(autojump);
        strafeSettings.addSetting(autolowhop);
        strafeSettings.addSetting(airStop);
        strafeSettings.addSetting(inAir);

        this.addSetting(strafeSettings);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(PlayerMoveEventListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(PlayerMoveEventListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (strafe.getValue()) {
            if ((MC.player.isOnGround() && !autolowhop.getValue() && !autojump.getValue())
                    || MC.player.isSneaking()
                    || MC.player.isGliding()
                    || MC.player.isInLava()
                    || MC.player.isTouchingWater()
                    || MC.player.getAbilities().flying
                    || !MovementUtil.isMoving()) {
                return;
            }

            if (MC.player.isOnGround() && autolowhop.getValue()) {
                MC.player.addVelocity(MC.player.getVelocity().x, 0.40123128, MC.player.getVelocity().z);

            } else if (MC.player.isOnGround() && autojump.getValue()) {
                MC.player.jump();
            }
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onMove(PlayerMoveEvent event) {
        if ((MC.player.isSneaking() && !sneaking.getValue())|| MC.player.isGliding() || MC.player.isInLava() || MC.player.isTouchingWater() || MC.player.getAbilities().flying)
            return;

        if (strafe.getValue()) {
            if (!MovementUtil.isMoving()) {
                if (airStop.getValue()) {
                    MovementUtil.setMotionX(0);
                    MovementUtil.setMotionZ(0);
                }
                return;
            }

            switch (mode.getValue()) {
                case Mode.Legit -> {
                    if (inAir.getValue() && MC.player.isOnGround()) {
                        return;
                    }

                    double[] dir = directionSpeed(getBaseMoveSpeed());
                    event.setX(dir[0]);
                    event.setZ(dir[1]);
                }

                case Mode.Rage -> {
                    if (inAir.getValue() && MC.player.isOnGround()) {
                        return;
                    }

                    double[] dir = directionSpeed(intensity.getValue() * getBaseMoveSpeed());
                    event.setX(dir[0]);
                    event.setZ(dir[1]);
                }
            }
        }
        else {
            if (MC.player.isOnGround()) {
                double[] dir = directionSpeed(intensity.getValue() * getBaseMoveSpeed());
                event.setX(dir[0]);
                event.setZ(dir[1]);
            }
        }
    }

    public double getBaseMoveSpeed() {
        double n = 0.2873;
        if (MC.player.hasStatusEffect(StatusEffects.SPEED) && (!this.slowCheck.getValue() || !MC.player.hasStatusEffect(StatusEffects.SLOWNESS))) {
            n *= 1.0 + 0.2 * (double) (Objects.requireNonNull(MC.player.getStatusEffect(StatusEffects.SPEED)).getAmplifier() + 1);
        }
        return n;
    }

    public double getX() {
        return getMotionX();
    }

    public double getY() {
        return getMotionY();
    }

    public double getZ() {
        return getMotionZ();
    }
}
