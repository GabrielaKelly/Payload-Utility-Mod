package net.payload.utils.rotation;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RotationLitematica {
    private final float yaw;
    private final float pitch;

    public RotationLitematica(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        if (Float.isInfinite(yaw) || Float.isNaN(yaw) || Float.isInfinite(pitch) || Float.isNaN(pitch)) {
            throw new IllegalStateException(yaw + " " + pitch);
        }
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public RotationLitematica add(RotationLitematica other) {
        return new RotationLitematica(
                this.yaw + other.yaw,
                this.pitch + other.pitch
        );
    }

    public RotationLitematica subtract(RotationLitematica other) {
        return new RotationLitematica(
                this.yaw - other.yaw,
                this.pitch - other.pitch
        );
    }

    public RotationLitematica clamp() {
        return new RotationLitematica(
                this.yaw,
                clampPitch(this.pitch)
        );
    }

    public RotationLitematica normalize() {
        return new RotationLitematica(
                normalizeYaw(this.yaw),
                this.pitch
        );
    }

    public boolean yawIsReallyClose(RotationLitematica other) {
        float yawDiff = Math.abs(normalizeYaw(yaw) - normalizeYaw(other.yaw)); // you cant fool me
        return (yawDiff < 0.01 || yawDiff > 359.99);
    }

    public static float clampPitch(float pitch) {
        return Math.max(-90, Math.min(90, pitch));
    }

    public static float normalizeYaw(float yaw) {
        float newYaw = yaw % 360F;
        if (newYaw < -180F) {
            newYaw += 360F;
        }
        if (newYaw > 180F) {
            newYaw -= 360F;
        }
        return newYaw;
    }

    @Override
    public String toString() {
        return "Yaw: " + yaw + ", Pitch: " + pitch;
    }

    public class RotationStuff {
        public static final double DEG_TO_RAD = Math.PI / 180.0;
        public static final float DEG_TO_RAD_F = (float) DEG_TO_RAD;
        public static final double RAD_TO_DEG = 180.0 / Math.PI;
        public static final float RAD_TO_DEG_F = (float) RAD_TO_DEG;

        public static RotationLitematica calcRotationFromVec3d(Vec3d orig, Vec3d dest, RotationLitematica current) {
            return wrapAnglesToRelative(current, calcRotationFromVec3d(orig, dest));
        }

        private static RotationLitematica calcRotationFromVec3d(Vec3d orig, Vec3d dest) {
            double[] delta = {orig.x - dest.x, orig.y - dest.y, orig.z - dest.z};
            double yaw = MathHelper.atan2(delta[0], -delta[2]);
            double dist = Math.sqrt(delta[0] * delta[0] + delta[2] * delta[2]);
            double pitch = MathHelper.atan2(delta[1], dist);
            return new RotationLitematica(
                    (float) (yaw * RAD_TO_DEG),
                    (float) (pitch * RAD_TO_DEG)
            );
        }

        public static RotationLitematica wrapAnglesToRelative(RotationLitematica current, RotationLitematica target) {
            if (current.yawIsReallyClose(target)) {
                return new RotationLitematica(current.getYaw(), target.getPitch());
            }
            return target.subtract(current).normalize().add(current);
        }

        public static Vec3d calcLookDirectionFromRotation(RotationLitematica rotation) {
            float flatZ = MathHelper.cos((-rotation.getYaw() * DEG_TO_RAD_F) - (float) Math.PI);
            float flatX = MathHelper.sin((-rotation.getYaw() * DEG_TO_RAD_F) - (float) Math.PI);
            float pitchBase = -MathHelper.cos(-rotation.getPitch() * DEG_TO_RAD_F);
            float pitchHeight = MathHelper.sin(-rotation.getPitch() * DEG_TO_RAD_F);
            return new Vec3d(flatX * pitchBase, pitchHeight, flatZ * pitchBase);
        }


        public static HitResult rayTraceTowards(ClientPlayerEntity entity, RotationLitematica rotation, double blockReachDistance) {
            Vec3d start = entity.getCameraPosVec(1.0F);


            Vec3d direction = calcLookDirectionFromRotation(rotation);
            Vec3d end = start.add(
                    direction.x * blockReachDistance,
                    direction.y * blockReachDistance,
                    direction.z * blockReachDistance
            );
            return entity.getWorld().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));
        }
    }
}
