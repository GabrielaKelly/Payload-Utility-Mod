package net.payload.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import net.payload.mixin.interfaces.IElytraVec3d;

import static net.payload.PayloadClient.MC;

public class MovementUtil {

    public static double getMotionX() {
        return MC.player.getVelocity().x;
    }
    public static double getMotionY() {
        return MC.player.getVelocity().y;
    }
    public static double getMotionZ() {
        return MC.player.getVelocity().z;
    }
    public static void setMotionX(double x) {
        ((IElytraVec3d) MC.player.getVelocity()).setEX(x);
    }
    public static void setMotionY(double y) {
        ((IElytraVec3d) MC.player.getVelocity()).setEY(y);
    }
    public static void setMotionZ(double z) {
        ((IElytraVec3d) MC.player.getVelocity()).setEZ(z);
    }
    
    
    public static boolean isMoving() {
        return MC.player.input.movementForward != 0.0 || MC.player.input.movementSideways != 0.0;
    }
    public static boolean isJumping() {
        return MC.player.input.playerInput.jump();
    }
    public static double getDistance2D() {
        double xDist = MC.player.getX() - MC.player.prevX;
        double zDist = MC.player.getZ() - MC.player.prevZ;
        return Math.sqrt(xDist * xDist + zDist * zDist);
    }

    public static double getJumpSpeed() {
        double defaultSpeed = 0.0;

        if (MC.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            //noinspection ConstantConditions
            int amplifier = MC.player.getActiveStatusEffects().get(StatusEffects.JUMP_BOOST).getAmplifier();
            defaultSpeed += (amplifier + 1) * 0.1;
        }

        return defaultSpeed;
    }
    public static float getMoveForward() {
        return MC.player.input.movementForward;
    }

    public static float getMoveStrafe() {
        return MC.player.input.movementSideways;
    }

    private static final double diagonal = 1 / Math.sqrt(2);
    private static final Vec3d horizontalVelocity = new Vec3d(0, 0, 0);
    public static Vec3d getHorizontalVelocity(double bps) {
        float yaw = MC.player.getYaw();

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double velX = 0;
        double velZ = 0;

        boolean a = false;
        if (MC.player.input.playerInput.forward()) {
            velX += forward.x / 20 * bps;
            velZ += forward.z / 20 * bps;
            a = true;
        }
        if (MC.player.input.playerInput.backward()) {
            velX -= forward.x / 20 * bps;
            velZ -= forward.z / 20 * bps;
            a = true;
        }

        boolean b = false;
        if (MC.player.input.playerInput.right()) {
            velX += right.x / 20 * bps;
            velZ += right.z / 20 * bps;
            b = true;
        }
        if (MC.player.input.playerInput.left()) {
            velX -= right.x / 20 * bps;
            velZ -= right.z / 20 * bps;
            b = true;
        }

        if (a && b) {
            velX *= diagonal;
            velZ *= diagonal;
        }

        /*
        ((IVec3d) horizontalVelocity).setAX(velX);
        ((IVec3d) horizontalVelocity).setAZ(velZ);

         */
        return horizontalVelocity;
    }
    public static double[] directionSpeedKey(double speed) {
        float forward = (MC.options.forwardKey.isPressed() ? 1 : 0) + (MC.options.backKey.isPressed() ? -1 : 0);
        float side = (MC.options.leftKey.isPressed() ? 1 : 0) + (MC.options.rightKey.isPressed() ? -1 : 0);
        float yaw = MC.player.prevYaw + (MC.player.getYaw() - MC.player.prevYaw) * MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }
    public static double[] directionSpeed(double speed) {
        float forward = MC.player.input.movementForward;
        float side = MC.player.input.movementSideways;
        float yaw = MC.player.prevYaw + (MC.player.getYaw() - MC.player.prevYaw) * MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static double getSpeed(boolean slowness) {
        double defaultSpeed = 0.2873;
        return getSpeed(slowness, defaultSpeed);
    }

    public static double getSpeed(boolean slowness, double defaultSpeed) {
        if (MC.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = MC.player.getActiveStatusEffects().get(StatusEffects.SPEED)
                    .getAmplifier();

            defaultSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }

        if (slowness && MC.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            int amplifier = MC.player.getActiveStatusEffects().get(StatusEffects.SLOWNESS)
                    .getAmplifier();

            defaultSpeed /= 1.0 + 0.2 * (amplifier + 1);
        }

        if (MC.player.isSneaking()) defaultSpeed /= 5;
        return defaultSpeed;
    }
}
