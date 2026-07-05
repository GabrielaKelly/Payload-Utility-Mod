/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.payload.utils.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.payload.Payload;
import net.payload.utils.player.FindItemResult;
import net.payload.utils.player.InvUtils;
import net.payload.utils.player.PlayerUtils;
import net.payload.utils.player.SlotUtils;

import static net.payload.PayloadClient.MC;


@SuppressWarnings("ConstantConditions")
public class BlockUtils {
    public static boolean breaking;
    private static boolean breakingThisTick;

    private BlockUtils() {
    }

    // Placing

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority) {
        return place(blockPos, findItemResult, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority) {
        return place(blockPos, findItemResult, rotate, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean checkEntities) {
        return place(blockPos, findItemResult, rotate, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority, boolean checkEntities) {
        return place(blockPos, findItemResult, true, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities) {
        return place(blockPos, findItemResult, rotate, rotationPriority, swingHand, checkEntities, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (findItemResult.isOffhand()) {
            return place(blockPos, Hand.OFF_HAND, MC.player.getInventory().selectedSlot, rotate, rotationPriority, swingHand, checkEntities, swapBack);
        } else if (findItemResult.isHotbar()) {
            return place(blockPos, Hand.MAIN_HAND, findItemResult.slot(), rotate, rotationPriority, swingHand, checkEntities, swapBack);
        }
        return false;
    }

    public static boolean place(BlockPos blockPos, Hand hand, int slot, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (slot < 0 || slot > 8) return false;

        Block toPlace = Blocks.OBSIDIAN;
        ItemStack i = hand == Hand.MAIN_HAND ? MC.player.getInventory().getStack(slot) : MC.player.getInventory().getStack(SlotUtils.OFFHAND);
        if (i.getItem() instanceof BlockItem blockItem) toPlace = blockItem.getBlock();
        if (!canPlaceBlock(blockPos, checkEntities, toPlace)) return false;

        Vec3d hitPos = Vec3d.ofCenter(blockPos);

        BlockPos neighbour;
        Direction side = getPlaceSide(blockPos);

        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);

        if (rotate) {
            float[] rotations = getRotations(hitPos);
            {
                InvUtils.swap(slot, swapBack);

                interact(bhr, hand, swingHand);

                if (swapBack) InvUtils.swapBack();
            }
        } else {
            InvUtils.swap(slot, swapBack);

            interact(bhr, hand, swingHand);

            if (swapBack) InvUtils.swapBack();
        }


        return true;
    }

    public static float[] getRotations(Vec3d targetPos) {
        Vec3d eyesPos = MC.player.getEyePos();  // Get player eye position
        double diffX = targetPos.x - eyesPos.x;
        double diffY = targetPos.y - eyesPos.y;
        double diffZ = targetPos.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90F;
        float pitch = (float) -(Math.atan2(diffY, diffXZ) * 180.0 / Math.PI);

        return new float[]{yaw, pitch};
    }

    public static void interact(BlockHitResult blockHitResult, Hand hand, boolean swing) {
        boolean wasSneaking = MC.player.isSneaking();
        MC.player.setSneaking(false);

        ActionResult result = MC.interactionManager.interactBlock(MC.player, hand, blockHitResult);

        if (result.isAccepted()) {
            if (swing) MC.player.swingHand(hand);
            else MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }

        MC.player.setSneaking(wasSneaking);
    }

    public static boolean canPlaceBlock(BlockPos blockPos, boolean checkEntities, Block block) {
        if (blockPos == null) return false;

        // Check y level
        if (!World.isValid(blockPos)) return false;

        // Check if current block is replaceable
        if (!MC.world.getBlockState(blockPos).isReplaceable()) return false;

        // Check if intersects entities
        return !checkEntities || MC.world.canPlace(block.getDefaultState(), blockPos, ShapeContext.absent());
    }

    public static boolean canPlace(BlockPos blockPos, boolean checkEntities) {
        return canPlaceBlock(blockPos, checkEntities, Blocks.OBSIDIAN);
    }

    public static boolean canPlace(BlockPos blockPos) {
        return canPlace(blockPos, true);
    }

    private static BlockPos lastSupportBlock = null;

    public static Direction getPlaceSide(BlockPos blockPos) {
        Vec3d lookVec = blockPos.toCenterPos().subtract(MC.player.getEyePos());
        double bestRelevancy = -Double.MAX_VALUE;
        Direction bestSide = null;

        // Standard placement check
        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = MC.world.getBlockState(neighbor);

            if (state.isAir() || isClickable(state.getBlock())) continue;
            if (!state.getFluidState().isEmpty()) continue;

            double relevancy = side.getAxis().choose(lookVec.getX(), lookVec.getY(), lookVec.getZ()) * side.getDirection().offset();
            if (relevancy > bestRelevancy) {
                bestRelevancy = relevancy;
                bestSide = side;
            }
        }

        if (bestSide != null) {
            lastSupportBlock = null;
        }

        return bestSide;
    }

    public static Direction getClosestPlaceSide(BlockPos blockPos) {
        return getClosestPlaceSide(blockPos, MC.player.getEyePos());
    }

    public static Direction getClosestPlaceSide(BlockPos blockPos, Vec3d pos) {
        Direction closestSide = null;
        double closestDistance = Double.MAX_VALUE;

        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = MC.world.getBlockState(neighbor);

            // Check if neighbour isn't empty
            if (state.isAir() || isClickable(state.getBlock())) continue;

            // Check if neighbour is a fluid
            if (!state.getFluidState().isEmpty()) continue;

            double distance = pos.squaredDistanceTo(neighbor.getX(), neighbor.getY(), neighbor.getZ());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSide = side;
            }
        }

        return closestSide;
    }

    public static boolean canBreak(BlockPos blockPos, BlockState state) {
        if (!MC.player.isCreative() && state.getHardness(MC.world, blockPos) < 0) return false;
        return state.getOutlineShape(MC.world, blockPos) != VoxelShapes.empty();
    }

    public static boolean canBreak(BlockPos blockPos) {
        return canBreak(blockPos, MC.world.getBlockState(blockPos));
    }

    public static boolean canInstaBreak(BlockPos blockPos, float breakSpeed) {
        return MC.player.isCreative() || calcBlockBreakingDelta2(blockPos, breakSpeed) >= 1;
    }

    public static boolean canInstaBreak(BlockPos blockPos) {
        BlockState state = MC.world.getBlockState(blockPos);
        return canInstaBreak(blockPos, MC.player.getBlockBreakingSpeed(state));
    }

    public static float calcBlockBreakingDelta2(BlockPos blockPos, float breakSpeed) {
        BlockState state = MC.world.getBlockState(blockPos);
        float f = state.getHardness(MC.world, blockPos);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = MC.player.canHarvest(state) ? 30 : 100;
            return breakSpeed / f / (float) i;
        }
    }

    // Other

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock
            || block instanceof AnvilBlock
            || block instanceof LoomBlock
            || block instanceof CartographyTableBlock
            || block instanceof GrindstoneBlock
            || block instanceof StonecutterBlock
            || block instanceof ButtonBlock
            || block instanceof AbstractPressurePlateBlock
            || block instanceof BlockWithEntity
            || block instanceof BedBlock
            || block instanceof FenceGateBlock
            || block instanceof DoorBlock
            || block instanceof NoteBlock
            || block instanceof TrapdoorBlock;
    }

    public static MobSpawn isValidMobSpawn(BlockPos blockPos, boolean newMobSpawnLightLevel) {
        return isValidMobSpawn(blockPos, MC.world.getBlockState(blockPos), newMobSpawnLightLevel ? 0 : 7);
    }

    public static MobSpawn isValidMobSpawn(BlockPos blockPos, BlockState blockState, int spawnLightLimit) {
        if (!(blockState.getBlock() instanceof AirBlock)) return MobSpawn.Never;

        BlockPos down = blockPos.down();
        BlockState downState = MC.world.getBlockState(down);
        if (downState.getBlock() == Blocks.BEDROCK) return MobSpawn.Never;

        if (!topSurface(downState)) {
            if (downState.getCollisionShape(MC.world, down) != VoxelShapes.fullCube())
                return MobSpawn.Never;
            if (downState.isTransparent()) return MobSpawn.Never;
        }

        if (MC.world.getLightLevel(LightType.BLOCK, blockPos) > spawnLightLimit) return MobSpawn.Never;
        else if (MC.world.getLightLevel(LightType.SKY, blockPos) > spawnLightLimit) return  MobSpawn.Potential;

        return MobSpawn.Always;
    }

    public static boolean topSurface(BlockState blockState) {
        if (blockState.getBlock() instanceof SlabBlock && blockState.get(SlabBlock.TYPE) == SlabType.TOP) return true;
        else return blockState.getBlock() instanceof StairsBlock && blockState.get(StairsBlock.HALF) == BlockHalf.TOP;
    }


    public enum MobSpawn {
        Never,
        Potential,
        Always
    }


}
