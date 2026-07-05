package net.payload.utils.player.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.payload.module.modules.client.AntiCheat;
import net.payload.utils.FindItemResult;
import net.payload.utils.block.BlockPosX;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.entity.InventoryUtil;

import static net.payload.PayloadClient.MC;
import static net.payload.module.Module.find;
import static net.payload.module.Module.findInHotbar;

public class EntityUtil {

    public static boolean isHoldingWeapon(PlayerEntity player) {
        return player.getMainHandStack().getItem() instanceof SwordItem || player.getMainHandStack().getItem() instanceof AxeItem || player.getMainHandStack().getItem() instanceof TridentItem || player.getMainHandStack().getItem() instanceof MaceItem;
    }

    public static int getHotbarWeaponSlot() {
        FindItemResult sword = findInHotbar(itemStack -> itemStack.getItem() instanceof SwordItem);
        FindItemResult axe = findInHotbar(itemStack -> itemStack.getItem() instanceof AxeItem);
        FindItemResult trident = findInHotbar(itemStack -> itemStack.getItem() instanceof TridentItem);
        FindItemResult mace = findInHotbar(itemStack -> itemStack.getItem() instanceof MaceItem);

        if (sword.found()) {
            return sword.slot();
        }
        else if (trident.found()) {
            return trident.slot();
        }
        else if (axe.found()) {
            return axe.slot();
        }
        else if (mace.found()) {
            return mace.slot();
        }

        return -1;
    }

    public static int getInventoryWeaponSlot() {

        FindItemResult sword = find(itemStack -> itemStack.getItem() instanceof SwordItem);
        FindItemResult axe = find(itemStack -> itemStack.getItem() instanceof AxeItem);
        FindItemResult trident = find(itemStack -> itemStack.getItem() instanceof TridentItem);
        FindItemResult mace = find(itemStack -> itemStack.getItem() instanceof MaceItem);

        if (sword.found()) {
            return sword.slot();
        }
        else if (trident.found()) {
            return trident.slot();
        }
        else if (axe.found()) {
            return axe.slot();
        }
        else if (mace.found()) {
            return mace.slot();
        }

        return -1;
    }

    public static boolean isInsideBlock() {
        if (OtherBlockUtils.getBlock(EntityUtil.getPlayerPos(true)) == Blocks.ENDER_CHEST) return true;
        return MC.world.canCollide(MC.player, MC.player.getBoundingBox());
    }
    public static int getDamagePercent(ItemStack stack) {
        if (stack.getDamage() == stack.getMaxDamage()) return 100;
        return (int) ((stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0f);
    }
    public static boolean isArmorLow(PlayerEntity player, int durability) {
        for (ItemStack piece : player.getArmorItems()) {
            if (piece == null || piece.isEmpty()) {
                return true;
            }

            if (getDamagePercent(piece) >= durability) continue;
            return true;
        }
        return false;
    }

    public static float getHealth(Entity entity) {
        if (entity.isLiving()) {
            LivingEntity livingBase = (LivingEntity) entity;
            return livingBase.getHealth() + livingBase.getAbsorptionAmount();
        }
        return 0.0f;
    }

    public static BlockPos getEntityPos(Entity entity) {
        return new BlockPosX(entity.getPos());
    }

    public static BlockPos getPlayerPos(boolean fix) {
        return new BlockPosX(MC.player.getPos(), fix);
    }

    public static BlockPos getEntityPos(Entity entity, boolean fix) {
        return new BlockPosX(entity.getPos(), fix);
    }

    public static Vec3d getEyesPos() {
        return MC.player.getEyePos();
    }

    public static boolean canSee(BlockPos pos, Direction side) {
        Vec3d testVec = pos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);
        HitResult result = MC.world.raycast(new RaycastContext(getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, MC.player));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    public static void swingHand(Hand hand, SwingSide side) {
        switch (side) {
            case All -> MC.player.swingHand(hand);
            case Client -> MC.player.swingHand(hand, false);
            case Server -> MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }
    }

    public static void syncInventory() {
        if (AntiCheat.INSTANCE.inventorySync.getValue()) MC.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(MC.player.currentScreenHandler.syncId));
    }
}
