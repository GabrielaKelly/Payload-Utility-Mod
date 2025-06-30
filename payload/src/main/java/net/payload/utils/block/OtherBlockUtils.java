package net.payload.utils.block;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;
import net.payload.Payload;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.player.combat.SwingSide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.payload.PayloadClient.MC;

public class OtherBlockUtils {
    public static final List<Block> shiftBlocks = Arrays.asList(
            Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE,
            Blocks.BIRCH_TRAPDOOR, Blocks.BAMBOO_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.CHERRY_TRAPDOOR,
            Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
            Blocks.ACACIA_TRAPDOOR, Blocks.ENCHANTING_TABLE, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX
    );
    public static boolean canPlace(BlockPos pos) {
        return canPlace(pos, 1000);
    }

    public static boolean canPlace(BlockPos pos, double distance) {
        if (getPlaceSide(pos, distance) == null) return false;
        if (!canReplace(pos)) return false;
        return !hasEntity(pos, false);
    }

    public static boolean canPlace(BlockPos pos, double distance, boolean ignoreCrystal) {
        if (getPlaceSide(pos, distance) == null) return false;
        if (!canReplace(pos)) return false;
        return !hasEntity(pos, ignoreCrystal);
    }

    public static boolean clientCanPlace(BlockPos pos) {
        return clientCanPlace(pos, false);
    }
    public static boolean clientCanPlace(BlockPos pos, boolean ignoreCrystal) {
        if (!canReplace(pos)) return false;
        return !hasEntity(pos, ignoreCrystal);
    }

    public static List<Entity> getEntities(Box box) {
        List<Entity> list = new ArrayList<>();
        for (Entity entity : MC.world.getEntities()) {
            if (entity == null) continue;
            if (entity.getBoundingBox().intersects(box)) {
                list.add(entity);
            }
        }
        return list;
    }

    public static List<EndCrystalEntity> getEndCrystals(Box box) {
        List<EndCrystalEntity> list = new ArrayList<>();
        for (Entity entity : MC.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal) {
                if (crystal.getBoundingBox().intersects(box)) {
                    list.add(crystal);
                }
            }
        }
        return list;
    }
    public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || ignoreCrystal && entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity && Payload.getInstance().moduleManager.antiCheat.obsMode.getValue())
                continue;
            return true;
        }
        return false;
    }

    public static boolean hasCrystal(BlockPos pos) {
        for (Entity entity : getEndCrystals(new Box(pos))) {
            if (!entity.isAlive() || !(entity instanceof EndCrystalEntity))
                continue;
            return true;
        }
        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreCrystal && entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity && Payload.getInstance().moduleManager.antiCheat.obsMode.getValue())
                continue;
            return true;
        }
        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || ignoreCrystal && entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity && Payload.getInstance().moduleManager.antiCheat.obsMode.getValue())
                continue;
            return true;
        }
        return false;
    }

    public static Direction getBestNeighboring(BlockPos pos, Direction facing) {
        for (Direction i : Direction.values()) {
            if (facing != null && pos.offset(i).equals(pos.offset(facing, -1)) || i == Direction.DOWN) continue;
            if (getPlaceSide(pos, false, true) != null) return i;
        }
        Direction bestFacing = null;
        double distance = 0;
        for (Direction i : Direction.values()) {
            if (facing != null && pos.offset(i).equals(pos.offset(facing, -1)) || i == Direction.DOWN) continue;
            if (getPlaceSide(pos) != null) {
                if (bestFacing == null || MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()) < distance) {
                    bestFacing = i;
                    distance = MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos());
                }
            }
        }
        return bestFacing;
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
                && getClickSideStrict(obsPos) != null
                && (MC.world.isAir(boost))
                && !hasEntityBlockCrystal(boost, false)
                && !hasEntityBlockCrystal(boost.up(), false);
               // && (!ClientSetting.INSTANCE.lowVersion.getValue() || MC.world.isAir(boost.up()));
    }
    public static void placeCrystal(BlockPos pos, boolean rotate) {
        boolean offhand = MC.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5,facing.getVector().getY() * 0.5,facing.getVector().getZ() * 0.5);
        if (rotate) {
            Payload.getInstance().rotationManager.lookAt(vec);
        }
        clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
    }
    public static final CopyOnWriteArrayList<BlockPos> placedPos = new CopyOnWriteArrayList<>();

    public static void placeBlock(BlockPos pos, boolean rotate) {
        placeBlock(pos, rotate, Payload.getInstance().moduleManager.antiCheat.packetPlace.getValue());
    }

    public static void placeBlock(BlockPos pos, boolean rotate, boolean packet) {
        if (airPlace()) {
            placedPos.add(pos);
            clickBlock(pos, Direction.DOWN, rotate, Hand.MAIN_HAND, packet);
            return;
        }
        Direction side = getPlaceSide(pos);
        if (side == null) return;
        placedPos.add(pos);
        clickBlock(pos.offset(side), side.getOpposite(), rotate, Hand.MAIN_HAND, packet);
    }

    public static void airPlaceBlock(BlockPos pos, boolean rotate) {
        placedPos.add(pos);
        clickBlock(pos, Direction.DOWN, rotate, Hand.MAIN_HAND, Payload.getInstance().moduleManager.antiCheat.packetPlace.getValue());
        return;
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate) {
        clickBlock(pos, side, rotate, Hand.MAIN_HAND);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand) {
        clickBlock(pos, side, rotate, hand, Payload.getInstance().moduleManager.antiCheat.packetPlace.getValue());
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, boolean packet) {
        clickBlock(pos, side, rotate, Hand.MAIN_HAND, packet);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, boolean packet) {
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (rotate) {
            Payload.getInstance().rotationManager.lookAt(directionVec);
        }
        EntityUtil.swingHand(hand, Payload.getInstance().moduleManager.antiCheat.swingMode.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        if (packet) {
            net.payload.module.Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        } else {
            MC.interactionManager.interactBlock(MC.player, hand, result);
        }
        if (rotate && Payload.getInstance().moduleManager.antiCheat.snapBack.getValue()) {
            Payload.getInstance().rotationManager.snapBack();
        }
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, SwingSide swingSide) {
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (rotate) {
            Payload.getInstance().rotationManager.lookAt(directionVec);
        }
        EntityUtil.swingHand(hand, swingSide);
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        net.payload.module.Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        if (rotate && Payload.getInstance().moduleManager.antiCheat.snapBack.getValue()) {
            Payload.getInstance().rotationManager.snapBack();
        }
    }

    public static Direction getPlaceSide(BlockPos pos) {
        return getPlaceSide(pos, Payload.getInstance().moduleManager.antiCheat.placement.getValue() == Placement.Strict, Payload.getInstance().moduleManager.antiCheat.placement.getValue() == Placement.Legit);
    }

    public static Direction getPlaceSide(BlockPos pos, boolean strict, boolean legit) {
        if (pos == null) return null;
        double dis = 114514;
        Direction side = null;
        for (Direction i : Direction.values()) {
            if (canClick(pos.offset(i)) && !canReplace(pos.offset(i))) {
                if (legit) {
                    if (!EntityUtil.canSee(pos.offset(i), i.getOpposite())) continue;
                }
                if (strict) {
                    if (!isStrictDirection(pos.offset(i), i.getOpposite())) continue;
                }
                double vecDis = MC.player.getEyePos().squaredDistanceTo(pos.toCenterPos().add(i.getVector().getX() * 0.5, i.getVector().getY() * 0.5, i.getVector().getZ() * 0.5));
                if (side == null || vecDis < dis) {
                    side = i;
                    dis = vecDis;
                }
            }
        }
        if (airPlace()) return Direction.DOWN;
        return side;
    }

    public static double distanceToXZ(final double x, final double z, double x2, double z2) {
        final double dx = x2 - x;
        final double dz = z2 - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distanceToXZ(final double x, final double z) {
        return distanceToXZ(x, z, MC.player.getX(), MC.player.getZ());
    }
    public static Direction getPlaceSide(BlockPos pos, double distance) {
        if (airPlace()) return Direction.DOWN;
        double dis = 114514;
        Direction side = null;
        for (Direction i : Direction.values()) {
            if (canClick(pos.offset(i)) && !canReplace(pos.offset(i))) {
                if (Payload.getInstance().moduleManager.antiCheat.placement.getValue() == Placement.Legit) {
                    if (!EntityUtil.canSee(pos.offset(i), i.getOpposite())) continue;
                } else if (Payload.getInstance().moduleManager.antiCheat.placement.getValue() == Placement.Strict) {
                    if (!isStrictDirection(pos.offset(i), i.getOpposite())) continue;
                }
                double vecDis = MC.player.getEyePos().squaredDistanceTo(pos.toCenterPos().add(i.getVector().getX() * 0.5, i.getVector().getY() * 0.5, i.getVector().getZ() * 0.5));
                if (MathHelper.sqrt((float) vecDis) > distance) {
                    continue;
                }
                if (side == null || vecDis < dis) {
                    side = i;
                    dis = vecDis;
                }
            }
        }
        return side;
    }

    public static Direction getClickSide(BlockPos pos) {
        Direction side = null;
        double range = 100;
        for (Direction i : Direction.values()) {
            if (!EntityUtil.canSee(pos, i)) continue;
            if (MathHelper.sqrt((float) MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range) continue;
            side = i;
            range = MathHelper.sqrt((float) MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
        }
        if (side != null)
            return side;
        side = Direction.UP;
        for (Direction i : Direction.values()) {
            if (Payload.getInstance().moduleManager.antiCheat.placement.getValue() == Placement.Strict) {
                if (!isStrictDirection(pos, i)) continue;
                if (Payload.getInstance().moduleManager.antiCheat.blockCheck.getValue() && !MC.world.isAir(pos.offset(i))) continue;
            }
            if (MathHelper.sqrt((float) MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range) continue;
            side = i;
            range = MathHelper.sqrt((float) MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
        }
        return side;
    }

    public static Direction getClickSideStrict(BlockPos pos) {
        Direction side = null;
        double range = 100;
        for (Direction i : Direction.values()) {
            if (!EntityUtil.canSee(pos, i)) continue;
            if (MathHelper.sqrt((float) MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range) continue;
            side = i;
            range = MathHelper.sqrt((float) MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
        }
        if (side != null)
            return side;
        side = null;
        for (Direction i : Direction.values()) {
            if (Payload.getInstance().moduleManager.antiCheat.placement.getValue() == Placement.Strict) {
                if (!isStrictDirection(pos, i)) continue;
                if (Payload.getInstance().moduleManager.antiCheat.blockCheck.getValue() && !MC.world.isAir(pos.offset(i))) continue;
            }
            if (MathHelper.sqrt((float) MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range) continue;
            side = i;
            range = MathHelper.sqrt((float) MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
        }
        return side;
    }

    public static boolean isStrictDirection(BlockPos pos, Direction side) {
        if (MC.player.getBlockY() - pos.getY() >= 0 && side == Direction.DOWN) return false;
        /*
        if (!Payload.getInstance().moduleManager.antiCheat.oldNCP.getValue()) {
            if (side == Direction.UP && pos.getY() + 1 > MC.player.getEyePos().getY()) {
                return false;
            }
        } else {
            if (side == Direction.UP && pos.getY() > MC.player.getEyePos().getY()) {
                return false;
            }
        }

         */

        if (Payload.getInstance().moduleManager.antiCheat.blockCheck.getValue() && (getBlock(pos.offset(side)) == Blocks.OBSIDIAN || getBlock(pos.offset(side)) == Blocks.BEDROCK || getBlock(pos.offset(side)) == Blocks.RESPAWN_ANCHOR)) return false;
        Vec3d eyePos = EntityUtil.getEyesPos();
        Vec3d blockCenter = pos.toCenterPos();
        ArrayList<Direction> validAxis = new ArrayList<>();
        validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, false));
        validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true));
        validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, false));
        return validAxis.contains(side);
    }

    public static ArrayList<Direction> checkAxis(double diff, Direction negativeSide, Direction positiveSide, boolean bothIfInRange) {
        ArrayList<Direction> valid = new ArrayList<>();
        if (diff < -0.5) {
            valid.add(negativeSide);
        }
        if (diff > 0.5) {
            valid.add(positiveSide);
        }
        if (bothIfInRange) {
            if (!valid.contains(negativeSide)) valid.add(negativeSide);
            if (!valid.contains(positiveSide)) valid.add(positiveSide);
        }
        return valid;
    }

    public static ArrayList<BlockEntity> getTileEntities(){
        return getLoadedChunks().flatMap(chunk -> chunk.getBlockEntities().values().stream()).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Stream<WorldChunk> getLoadedChunks(){
        int radius = Math.max(2, MC.options.getClampedViewDistance()) + 3;
        int diameter = radius * 2 + 1;

        ChunkPos center = MC.player.getChunkPos();
        ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
        ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);

        return Stream.iterate(min, pos -> {
                    int x = pos.x;
                    int z = pos.z;
                    x++;

                    if(x > max.x)
                    {
                        x = min.x;
                        z++;
                    }

                    return new ChunkPos(x, z);

                }).limit((long) diameter *diameter)
                .filter(c -> MC.world.isChunkLoaded(c.x, c.z))
                .map(c -> MC.world.getChunk(c.x, c.z)).filter(Objects::nonNull);
    }

    public static ArrayList<BlockPos> getSphere(float range) {
       return getSphere(range, MC.player.getEyePos());
    }
    public static ArrayList<BlockPos> getSphere(float range, Vec3d pos) {
        ArrayList<BlockPos> list = new ArrayList<>();
        for (double x = pos.getX() - range; x < pos.getX() + range; ++x) {
            for (double z = pos.getZ() - range; z < pos.getZ() + range; ++z) {
                for (double y = pos.getY() - range; y < pos.getY() + range; ++y) {
                    BlockPos curPos = new BlockPosX(x, y, z);
                    if (curPos.toCenterPos().distanceTo(pos) > range) continue;
                    if (!list.contains(curPos)) {
                        list.add(curPos);
                    }
                }
            }
        }
        return list;
    }

    public static Block getBlock(BlockPos pos) {
        return MC.world.getBlockState(pos).getBlock();
    }

    public static boolean canReplace(BlockPos pos) {
        if (pos.getY() >= 320) return false;
        if (Payload.getInstance().moduleManager.antiCheat.multiPlace.getValue() && placedPos.contains(pos)) {
            return true;
        }
        if (MC.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
           // if (AutoWeb.ignore && AutoCrystal.INSTANCE.replace.getValue()) return true;
            if (Payload.getInstance().moduleManager.crystalaura.replace.getValue()) return true;
        }
        return MC.world.getBlockState(pos).isReplaceable();
    }

    public static boolean canClick(BlockPos pos) {
        if (Payload.getInstance().moduleManager.antiCheat.multiPlace.getValue() && placedPos.contains(pos)) {
            return true;
        }

        /*
        if (MC.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
            if (AutoWeb.ignore) {
                return AutoCrystal.INSTANCE.airPlace.getValue();
            }
        }

         */
        return MC.world.getBlockState(pos).isSolid() && (!(shiftBlocks.contains(getBlock(pos)) || getBlock(pos) instanceof BedBlock) || MC.player.isSneaking());
    }

    public static boolean airPlace() {
        return Payload.getInstance().moduleManager.antiCheat.placement.getValue() == Placement.AirPlace;
    }
}
