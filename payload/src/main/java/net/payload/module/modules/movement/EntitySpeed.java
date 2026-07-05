package net.payload.module.modules.movement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.LivingEntityMoveEvent;
import net.payload.event.listeners.LivingEntityMoveEventListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;

public class EntitySpeed extends Module implements LivingEntityMoveEventListener {

    private FloatSetting forwardSpeed = FloatSetting.builder()
            .id("entityspeed_forward_speed")
            .displayName("Forward Speed")
            .defaultValue(1.0f)
            .minValue(0.05f)
            .maxValue(5.0f)
            .step(0.05f)
            .build();

    private FloatSetting backwardSpeed = FloatSetting.builder()
            .id("entityspeed_backward_speed")
            .displayName("Backward Speed")
            .defaultValue(0.5f)
            .minValue(0.05f)
            .maxValue(5.0f)
            .step(0.05f)
            .build();

    private FloatSetting upwardSpeed = FloatSetting.builder()
            .id("entityspeed_upward_speed")
            .displayName("Upward Speed")
            .defaultValue(0f)
            .minValue(0.0f)
            .maxValue(5.0f)
            .step(0.05f)
            .build();

    private FloatSetting glideSpeed = FloatSetting.builder()
            .id("entityspeed_glide_speed")
            .displayName("Glide Speed")
            .defaultValue(0f)
            .minValue(0.0f)
            .maxValue(15.0f)
            .step(1f)
            .build();

    private final BooleanSetting hover = BooleanSetting.builder()
            .id("entityspeed_hover")
            .displayName("Hover")
            .defaultValue(false)
            .build();

    private final BooleanSetting onlyOnGround = BooleanSetting.builder().id("entityspeed_onground").displayName("Only On Ground")
            .description("limits speed in air").defaultValue(true).build();

    private final BooleanSetting inWater = BooleanSetting.builder().id("entityspeed_inwater").displayName("In Water")
            .description("Use speed when in water").defaultValue(false).build();

    boolean warning = false;


    public EntitySpeed() {
        super("EntitySpeed");
        this.setDescription("Changes your movement speed when riding animals");
        this.setCategory(Category.of("Movement"));

        this.addSetting(onlyOnGround);
        this.addSetting(inWater);
        this.addSetting(forwardSpeed);
        this.addSetting(backwardSpeed);
        this.addSetting(upwardSpeed);
        this.addSetting(glideSpeed);
        this.addSetting(hover);
    }


    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(LivingEntityMoveEventListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(LivingEntityMoveEventListener.class, this);
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onEntityMove(LivingEntityMoveEvent event) {
            if (nullCheck()) return;

            if (upwardSpeed.getValue() == 0) {
                warning = true;
            }

            if (upwardSpeed.getValue() > 0 && warning) {
                sendChatMessage("Warning: flying entities will build up fall damage!");
                warning = false;
            }

            if (event.entity.getControllingPassenger() != MC.player) return;

            // Check for onlyOnGround and inWater
            LivingEntity entity = event.entity;
            if (onlyOnGround.getValue() && !entity.isOnGround()) return;
            if (!inWater.getValue() && entity.isTouchingWater()) return;

            Vec3d velocity = entity.getVelocity();

            double motionX = velocity.x;
            double motionZ = velocity.z;
            double motionY = velocity.y;
            float yawRad = entity.getYaw() * MathHelper.RADIANS_PER_DEGREE;


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

            entity.setVelocity( new Vec3d(motionX, motionY, motionZ));

        }
    }
