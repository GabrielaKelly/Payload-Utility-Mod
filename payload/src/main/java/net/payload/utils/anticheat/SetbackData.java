package net.payload.utils.anticheat;

import net.minecraft.util.math.Vec3d;

public record SetbackData(Vec3d position, long timeMS, int teleportID) {

    public SetbackData(Vec3d position, long timeMS, int teleportID) {
        this.position = position;
        this.timeMS = timeMS;
        this.teleportID = teleportID;
    }


    public long timeSince() {
        return System.currentTimeMillis() - timeMS;
    }


    public Vec3d position() {
        return position;
    }


    public long timeMS() {
        return timeMS;
    }


    public int teleportID() {
        return teleportID;
    }
}