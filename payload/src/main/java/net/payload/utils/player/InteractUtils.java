package net.payload.utils.player;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

import static net.payload.PayloadClient.MC;

public class InteractUtils {

    public static Set<Direction> getPlaceDirectionsGrim(Vec3d eyePos, BlockPos blockPos) {
        return getPlaceDirectionsGrim(
                eyePos.x, eyePos.y, eyePos.z,
                blockPos
        );
    }

    public static Direction getPlaceDirectionGrim(BlockPos blockPos) {
        // Get valid placement directions based on player's eye position
        Set<Direction> directions = getPlaceDirectionsGrim(
                MC.player.getEyePos(),
                blockPos
        );

        // Return any valid direction or default to UP
        return directions.stream()
                .findAny()
                .orElse(Direction.UP);
    }

    public static Set<Direction> getPlaceDirectionsGrim(double x, double y, double z, BlockPos pos) {
        Set<Direction> dirs = new HashSet<>(6);
        Box combined = new Box(pos);

        // Create eye position box with specific offsets
        Box eyePositions = new Box(
                x, y + 0.4, z,
                x, y + 1.62, z
        ).expand(2.0E-4);

        // Check each direction based on box intersections
        if (eyePositions.minZ <= combined.minZ) {
            dirs.add(Direction.NORTH);
        }
        if (eyePositions.maxZ >= combined.maxZ) {
            dirs.add(Direction.SOUTH);
        }
        if (eyePositions.maxX >= combined.maxX) {
            dirs.add(Direction.EAST);
        }
        if (eyePositions.minX <= combined.minX) {
            dirs.add(Direction.WEST);
        }
        if (eyePositions.maxY >= combined.maxY) {
            dirs.add(Direction.UP);
        }
        if (eyePositions.minY <= combined.minY) {
            dirs.add(Direction.DOWN);
        }

        return dirs;
    }
}
