package net.payload.utils.player.combat;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.module.modules.client.AntiCheat;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.math.CacheTimer;

import java.util.ArrayList;
import java.util.List;

import static net.payload.PayloadClient.MC;

public class CombatUtil {
    public static boolean terrainIgnore = false;
    public static BlockPos modifyPos;
    public static BlockState modifyBlockState = Blocks.AIR.getDefaultState();
    public static final CacheTimer breakTimer = new CacheTimer();
    
    static AntiCheat ac = Payload.getInstance().moduleManager.antiCheat;
    
    public static List<PlayerEntity> getEnemies(double range) {
        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity player : MC.world.getPlayers()) {
            if (!isValid(player, range)) continue;
            list.add(player);
        }
        return list;
    }

    public static void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
        for (EndCrystalEntity entity : OtherBlockUtils.getEndCrystals(new Box(pos))) {
            attackCrystal(entity, rotate, eatingPause);
            break;
        }
    }

    public static void attackCrystal(Box box, boolean rotate, boolean eatingPause) {
        for (EndCrystalEntity entity : OtherBlockUtils.getEndCrystals(box)) {
            attackCrystal(entity, rotate, eatingPause);
            break;
        }
    }

    public static void attackCrystal(Entity crystal, boolean rotate, boolean usingPause) {
        if (!CombatUtil.breakTimer.passed((long) (ac.attackDelay.getValue() * 1000))) return;
        if (usingPause && MC.player.isUsingItem())
            return;
        if (crystal != null) {
            CombatUtil.breakTimer.reset();
            if (rotate && ac.attackRotate.getValue()) Payload.getInstance().rotationManager.lookAt(new Vec3d(crystal.getX(), crystal.getY() + 0.25, crystal.getZ()));
            MC.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, MC.player.isSneaking()));
            MC.player.resetLastAttackedTicks();
            EntityUtil.swingHand(Hand.MAIN_HAND, ac.swingMode.getValue());
            if (rotate && ac.snapBack.getValue()) {
                Payload.getInstance().rotationManager.snapBack();
            }
        }
    }
    public static boolean isValid(Entity entity, double range) {
        boolean invalid = entity == null || !entity.isAlive() || entity.equals(MC.player) || entity instanceof PlayerEntity player &&
                Payload.getInstance().friendsList.contains(player) || MC.player.getPos().distanceTo(entity.getPos()) > range;

        return !invalid;
    }

    public static PlayerEntity getClosestEnemy(double distance) {
        PlayerEntity closest = null;

        for (PlayerEntity player : getEnemies(distance)) {
            if (closest == null) {
                closest = player;
                continue;
            }

            if (!(MC.player.squaredDistanceTo(player.getPos()) < MC.player.squaredDistanceTo(closest))) continue;

            closest = player;
        }
        return closest;
    }
    public static Vec3d getEntityPosVec(PlayerEntity entity, int ticks) {
        if (ticks <= 0) {
            return entity.getPos();
        }
        return entity.getPos().add(getMotionVec(entity, ticks, true));
    }

    public static Vec3d getMotionVec(Entity entity, float ticks, boolean collision) {
        double dX = entity.getX() - entity.prevX;
        double dZ = entity.getZ() - entity.prevZ;
        double entityMotionPosX = 0;
        double entityMotionPosZ = 0;
        if (collision) {
            for (double i = 1; i <= ticks; i = i + 0.5) {
                if (!MC.world.canCollide(entity, entity.getBoundingBox().offset(new Vec3d(dX * i, 0, dZ * i)))) {
                    entityMotionPosX = dX * i;
                    entityMotionPosZ = dZ * i;
                } else {
                    break;
                }
            }
        } else {
            entityMotionPosX = dX * ticks;
            entityMotionPosZ = dZ * ticks;
        }

        return new Vec3d(entityMotionPosX, 0, entityMotionPosZ);
    }
}
