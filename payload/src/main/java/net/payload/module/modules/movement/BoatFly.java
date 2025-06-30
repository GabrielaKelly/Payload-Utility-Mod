package net.payload.module.modules.movement;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;

public class BoatFly extends Module implements TickListener {
    private final SettingGroup boatflyspeed;
    private final FloatSetting forwardSpeed;
    private final FloatSetting backwardSpeed;
    private final FloatSetting upwardSpeed;
    private final FloatSetting glideSpeed;
    private final BooleanSetting antiKick;
    private final BooleanSetting hover;

    public BoatFly() {
        super("BoatFly");
        this.setName("BoatFly");
        this.setCategory(Category.of("Movement"));
        this.setDescription("Changes your movement speed in boats");
        boatflyspeed = SettingGroup.Builder.builder()
                .id("speed(s)")
                .description("Speed-related settings for BoatFly")
                .build();

        forwardSpeed = FloatSetting.builder()
                .id("forward_speed")
                .displayName("Forward Speed")
                .defaultValue(1.0f)
                .minValue(0.05f)
                .maxValue(5.0f)
                .step(0.05f)
                .build();

        backwardSpeed = FloatSetting.builder()
                .id("backward_speed")
                .displayName("Backward Speed")
                .defaultValue(0.5f)
                .minValue(0.05f)
                .maxValue(5.0f)
                .step(0.05f)
                .build();

        upwardSpeed = FloatSetting.builder()
                .id("upward_speed")
                .displayName("Upward Speed")
                .defaultValue(0.3f)
                .minValue(0.0f)
                .maxValue(5.0f)
                .step(0.05f)
                .build();

        glideSpeed = FloatSetting.builder()
                .id("glide_speed")
                .displayName("Glide Speed")
                .defaultValue(0.3f)
                .minValue(0.0f)
                .maxValue(5.0f)
                .step(0.05f)
                .build();

        antiKick = BooleanSetting.builder()
                .id("boat_antikick")
                .displayName("AntiKick")
                .defaultValue(false)
                .build();

        hover = BooleanSetting.builder()
                .id("hover")
                .displayName("Hover")
                .defaultValue(false)
                .build();

        boatflyspeed.addSetting(forwardSpeed);
        boatflyspeed.addSetting(backwardSpeed);
        boatflyspeed.addSetting(upwardSpeed);
        boatflyspeed.addSetting(glideSpeed);

        this.addSetting(boatflyspeed);
        this.addSetting(antiKick);
        this.addSetting(hover);

        SettingManager.registerSetting(forwardSpeed);
        SettingManager.registerSetting(backwardSpeed);
        SettingManager.registerSetting(upwardSpeed);
        SettingManager.registerSetting(glideSpeed);
        SettingManager.registerSetting(antiKick);
        SettingManager.registerSetting(hover);
    }

    @Override
    public void onToggle() {
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
    public void onTick(TickEvent.Pre event) {
        ClientPlayerEntity player = MC.player;

        if (player.getVehicle() instanceof BoatEntity && this.state.getValue()) {
            BoatEntity boat = (BoatEntity) player.getVehicle();
            Vec3d velocity = boat.getVelocity();

            double motionX = velocity.x;
            double motionZ = velocity.z;
            double motionY = velocity.y;
            float yawRad = boat.getYaw() * MathHelper.RADIANS_PER_DEGREE;

            if (MC.options.forwardKey.isPressed()) {
                float speed = forwardSpeed.getValue();
                motionX = MathHelper.sin(-yawRad) * speed;
                motionZ = MathHelper.cos(yawRad) * speed;
            } else if (MC.options.backKey.isPressed()) {
                float speed = backwardSpeed.getValue();
                motionX = MathHelper.sin(-yawRad) * -speed;
                motionZ = MathHelper.cos(yawRad) * -speed;
            }

            if (MC.options.jumpKey.isPressed()) {
                motionY = upwardSpeed.getValue();
            }

            if (hover.getValue() && !MC.options.jumpKey.isPressed()) {
                motionY = 0.02;
            }

            if (!hover.getValue() && !MC.options.jumpKey.isPressed()) {
                motionY += (0.01 * glideSpeed.getValue());
            }

            if (antiKick.getValue()) {
                boat.setVelocity(boat.getVelocity().add(0, -0.03, 0));
            }

            boat.setVelocity(new Vec3d(motionX, motionY, motionZ));
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {
    }
}