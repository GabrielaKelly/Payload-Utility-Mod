package net.payload.module.modules.movement;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.SendMovementPacketEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.SendMovementPacketListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.FindItemResult;
import net.payload.utils.player.InvUtils;

public class ElytraBoost extends Module implements TickListener, SendMovementPacketListener {

    // Settings - Vanilla
    private final BooleanSetting instantElytraBoost = BooleanSetting.builder()
            .id("elytraboost_instant")
            .displayName("Easy Start")
            .description("no weird double-jump needed!")
            .defaultValue(true)
            .build();

    private final BooleanSetting hover = BooleanSetting.builder()
            .id("elytraboost_hover")
            .displayName("Hover")
            .description("Keeps you roughly at the same Y value")
            .defaultValue(false)
            .build();

    private final BooleanSetting speedCtrl = BooleanSetting.builder()
            .id("elytraboost_speedctrl")
            .displayName("Speed control")
            .description("Control your velocity with the Forward and Back keys")
            .defaultValue(true)
            .build();

    private final BooleanSetting speedCap = BooleanSetting.builder()
            .id("elytraboost_speedcap")
            .displayName("Speed Cap")
            .description("Cap speed")
            .defaultValue(false)
            .build();

    private final FloatSetting speedCapFloat = FloatSetting.builder()
            .id("elytraboost_speedcapfloat")
            .displayName("Speed Cap km/h")
            .description("Caps your speed")
            .defaultValue(110f)
            .minValue(0f)
            .maxValue(350f)
            .step(10f)
            .build();

    private final FloatSetting speedMultiplier = FloatSetting.builder()
            .id("elytraboost_speed_multiplier")
            .displayName("Speed Multiplier")
            .description("Speed multiplier when elytraboosting.")
            .defaultValue(1f)
            .minValue(0f)
            .maxValue(3f)
            .step(0.25f)
            .build();

    private final BooleanSetting autoFirework = BooleanSetting.builder()
            .id("elytraboost_autofirework")
            .displayName("Auto Firework")
            .description("Automatically uses fireworks")
            .defaultValue(false)
            .build();

    private final FloatSetting fireworkDelay = FloatSetting.builder()
            .id("elytraboost_firework_delay")
            .displayName("Firework Delay")
            .description("Time to wait before firework")
            .defaultValue(4f)
            .minValue(1f)
            .maxValue(20f)
            .step(1f)
            .build();

    private final BooleanSetting heightCtrl = BooleanSetting.builder()
            .id("elytraboost_heightctrl")
            .displayName("Height control")
            .description("Control your height with the Jump and Sneak keys")
            .defaultValue(true)
            .build();

    private final BooleanSetting yawlock = BooleanSetting.builder()
            .id("elytraboost_yawlock")
            .displayName("Smart Yaw")
            .description("Points you in the cardinal highway directions")
            .defaultValue(false)
            .build();

    private final BooleanSetting highway = BooleanSetting.builder()
            .id("elytraboost_highway")
            .displayName("Highway")
            .description("Points you in the angle for highways")
            .defaultValue(false)
            .build();

    private final FloatSetting highwayAngle = FloatSetting.builder()
            .id("elytraboost_highwayangle")
            .displayName("Highway Pitch")
            .description("Pitch angle in degrees.")
            .defaultValue(-2.055f)
            .minValue(-5f)
            .maxValue(1f)
            .step(0.01f)
            .build();

    private final BooleanSetting stopInWater = BooleanSetting.builder()
            .id("elytraboost_water")
            .displayName("Stop in water")
            .description("Stops elytraboosting in water")
            .defaultValue(true)
            .build();

    // State variables
    private double ticksLeft;
    private final CacheTimer instantFlyTimer = new CacheTimer();
    private boolean onGround = false;

    public ElytraBoost() {
        super("ElytraBoost");

        this.setName("ElytraBoost");
        this.setCategory(Category.of("Movement"));
        this.setDescription("Straight up changes elytra velocity");


        this.addSetting(instantElytraBoost);

        this.addSetting(speedCtrl);
        this.addSetting(heightCtrl);
        this.addSetting(stopInWater);
        this.addSetting(speedMultiplier);
        this.addSetting(yawlock);
        this.addSetting(hover);
        this.addSetting(highway);
        this.addSetting(speedCap);
        this.addSetting(speedCapFloat);
        this.addSetting(autoFirework);
        this.addSetting(fireworkDelay);
    }

    public double getSpeed() {
        return speedMultiplier.getValue();
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(SendMovementPacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(SendMovementPacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);

        if (nullCheck()) return;

        instantFlyTimer.reset();

        if (Payload.getInstance().moduleManager.elytraPacket.state.getValue()) {
            Payload.getInstance().moduleManager.elytraPacket.toggle();
        }

        if (MC.player.isGliding() && autoFirework.getValue()) {
            useFirework();
        }
    }

    @Override
    public void onToggle() {
        // No implementation needed
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        if (autoFirework.getValue() && MC.player.isGliding()) {
            useFirework();
        }

        Vec3d velocity = MC.player.getVelocity();

        // Apply smart yaw lock if enabled
        if (yawlock.getValue()) {
            MC.player.setYaw(getSmartYawDirection());
        }

        onGround = MC.player.isOnGround();

        if (onGround) {
            instantFlyTimer.reset();
        }

        if (hasElytra() && MC.options.jumpKey.isPressed() && !MC.player.isGliding() && !onGround && instantElytraBoost.getValue() && MC.player.getVelocity().getY() < 0D) {
            if (!instantFlyTimer.passed((100))) return;
            if (instantFlyTimer.passed(1000)) return;
            MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (hover.getValue() && MC.player.isGliding()) {
            double upwardForce = 0.02;

            if (MC.player.fallDistance > 0 || velocity.y < 0) {
                MC.player.setVelocity(velocity.x, upwardForce, velocity.z);
            } else {
                MC.player.setVelocity(velocity.x, 0, velocity.z);
            }

            if (highway.getValue()) {
                MC.player.setPitch(highwayAngle.getValue());
            }
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {
        // No implementation needed
    }

    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
        if (nullCheck()) return;

        if (!hasElytra()) return;

        if (MC.player.isGliding()) {
            // Handle water detection
            if (stopInWater.getValue() && MC.player.isTouchingWater()) {
                return;
            }

            controlSpeed();
            controlHeight();
        }
    }

    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Post event) {
        // No implementation needed
    }

    /**
     * Calculates a smart yaw direction that snaps to 45-degree increments
     */
    private float getSmartYawDirection() {
        return Math.round((MC.player.getYaw() + 1f) / 45f) * 45f;
    }

    /**
     * Controls the player's height while elytra flying
     */
    private void controlHeight() {
        if (!heightCtrl.getValue()) return;

        Vec3d velocity = MC.player.getVelocity();

        if (!hover.getValue()) {
            if (MC.options.jumpKey.isPressed()) {
                MC.player.setVelocity(0, velocity.y + 0.2, 0);
            } else if (MC.options.sneakKey.isPressed()) {
                MC.player.setVelocity(0, velocity.y - 0.2, 0);
            }
        }
    }

    /**
     * Uses a firework to boost elytra flight
     */
    private void useFirework() {
        if (ticksLeft <= 0) {
            ticksLeft = fireworkDelay.getValue() * 20;

            FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);

            if (!itemResult.found()) return;

            if (itemResult.isOffhand()) {
                MC.interactionManager.interactItem(MC.player, Hand.OFF_HAND);
                MC.player.swingHand(Hand.OFF_HAND);
            } else {
                InvUtils.swap(itemResult.slot(), true);
                MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
                MC.player.swingHand(Hand.MAIN_HAND);
                InvUtils.swapBack();
            }
        }
        ticksLeft--;
    }

    /**
     * Controls the player's speed in vanilla mode
     */
    private void controlSpeed() {
        if (!speedCtrl.getValue()) return;

        float yaw = (float) Math.toRadians(MC.player.getYaw());
        Vec3d forward = new Vec3d(-MathHelper.sin(yaw) * 0.05, 0, MathHelper.cos(yaw) * 0.05);
        Vec3d velocity = MC.player.getVelocity();

        PlayerEntity player = MC.player;
        if (player == null) return;

        double currentSpeed = calculateSpeed(player);

        if (MC.options.forwardKey.isPressed()) {
            if (speedCap.getValue() && currentSpeed > speedCapFloat.getValue()) {
                applySpeedCap(velocity, speedCapFloat.getValue());
            } else {
                MC.player.setVelocity(velocity.add(forward.multiply(speedMultiplier.getValue())));
            }
        } else if (MC.options.backKey.isPressed()) {
            MC.player.setVelocity(velocity.subtract(forward.multiply(speedMultiplier.getValue() * 2)));
        }
    }

    /**
     * Applies a speed cap to the player's velocity
     */
    private void applySpeedCap(Vec3d velocity, float speedCapValue) {
        double cappedSpeed = speedCapValue / (20 * 3.6);
        double currentSpeed = velocity.horizontalLength();

        if (currentSpeed > cappedSpeed) {
            MC.player.setVelocity(velocity.multiply(cappedSpeed / currentSpeed, 1.0, cappedSpeed / currentSpeed));
        }
    }

    /**
     * Calculates the player's current speed in km/h
     */
    private double calculateSpeed(PlayerEntity player) {
        double dx = player.getX() - player.prevX;
        double dz = player.getZ() - player.prevZ;
        double dy = player.getY() - player.prevY;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance * 20 * 3.6; // Convert to km/h
    }

    /**
     * Checks if the player has an elytra equipped
     */
    public boolean hasElytra() {
        if (nullCheck()) return false;

        ItemStack chest = MC.player.getEquippedStack(EquipmentSlot.CHEST);
        return chest.getItem() == Items.ELYTRA;
    }
}