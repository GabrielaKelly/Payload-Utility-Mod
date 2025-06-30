package net.payload.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.payload.interfaces.RightClickHandler;


import static net.payload.PayloadClient.MC;

public class PlayerUtils {

    private PlayerUtils() {
    }

    public static void rightClick() {
        if (MC.player != null) {
            ((RightClickHandler) MC).payload$rightClick();
        }
    }

    public static boolean isMoving() {
        return MC.player.forwardSpeed != 0 || MC.player.sidewaysSpeed != 0;
    }

    public static boolean isSprinting() {
        return MC.player.isSprinting() && (MC.player.forwardSpeed != 0 || MC.player.sidewaysSpeed != 0);
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(squaredDistance(x1, y1, z1, x2, y2, z2));
    }

    public static double distanceTo(Entity entity) {
        return distanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double distanceTo(BlockPos blockPos) {
        return distanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double distanceTo(Vec3d vec3d) {
        return distanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static double distanceTo(double x, double y, double z) {
        return Math.sqrt(squaredDistanceTo(x, y, z));
    }

    public static double squaredDistanceTo(Entity entity) {
        return squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double squaredDistanceTo(BlockPos blockPos) {
        return squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double squaredDistanceTo(double x, double y, double z) {
        return squaredDistance(MC.player.getX(), MC.player.getY(), MC.player.getZ(), x, y, z);
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double f = x1 - x2;
        double g = y1 - y2;
        double h = z1 - z2;
        return org.joml.Math.fma(f, f, org.joml.Math.fma(g, g, h * h));
    }

    public static boolean isWithin(Entity entity, double r) {
        return squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ()) <= r * r;
    }

    public static boolean isWithin(Vec3d vec3d, double r) {
        return squaredDistanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ()) <= r * r;
    }

    public static boolean isWithin(BlockPos blockPos, double r) {
        return squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= r * r;
    }

    public static boolean isWithin(double x, double y, double z, double r) {
        return squaredDistanceTo(x, y, z) <= r * r;
    }

    public static double distanceToCamera(double x, double y, double z) {
        return Math.sqrt(squaredDistanceToCamera(x, y, z));
    }

    public static double distanceToCamera(Entity entity) {
        return distanceToCamera(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
    }

    public static double squaredDistanceToCamera(double x, double y, double z) {
        Vec3d cameraPos = MC.gameRenderer.getCamera().getPos();
        return squaredDistance(cameraPos.x, cameraPos.y, cameraPos.z, x, y, z);
    }

    public static double squaredDistanceToCamera(Entity entity) {
        return squaredDistanceToCamera(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
    }

    public static boolean isWithinCamera(Entity entity, double r) {
        return squaredDistanceToCamera(entity.getX(), entity.getY(), entity.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(Vec3d vec3d, double r) {
        return squaredDistanceToCamera(vec3d.getX(), vec3d.getY(), vec3d.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(BlockPos blockPos, double r) {
        return squaredDistanceToCamera(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(double x, double y, double z, double r) {
        return squaredDistanceToCamera(x, y, z) <= r * r;
    }

    public static boolean isWithinReach(Entity entity) {
        return isWithinReach(entity.getX(), entity.getY(), entity.getZ());
    }

    public static boolean isWithinReach(Vec3d vec3d) {
        return isWithinReach(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static boolean isWithinReach(BlockPos blockPos) {
        return isWithinReach(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static boolean isWithinReach(double x, double y, double z) {
        return squaredDistance(MC.player.getX(), MC.player.getEyeY(), MC.player.getZ(), x, y, z) <= MC.player.getBlockInteractionRange() * MC.player.getBlockInteractionRange();
    }

    public static GameMode getGameMode() {
        if (MC.player == null) return null;
        PlayerListEntry playerListEntry = MC.getNetworkHandler().getPlayerListEntry(MC.player.getUuid());
        if (playerListEntry == null) return null;
        return playerListEntry.getGameMode();
    }

    public static float getTotalHealth() {
        return MC.player.getHealth() + MC.player.getAbsorptionAmount();
    }

    public static boolean isAlive() {
        return MC.player.isAlive() && !MC.player.isDead();
    }

    public static int getPing() {
        if (MC.getNetworkHandler() == null) return 0;

        PlayerListEntry playerListEntry = MC.getNetworkHandler().getPlayerListEntry(MC.player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }

    public static boolean silentSwapEquipChestplate() {
        // Check if the player is already wearing a chestplate or elytra
        if (!MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.NETHERITE_CHESTPLATE)
                && !MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {

            // Look for a chestplate in the hotbar
            FindItemResult hotbarChestplateSlot = InvUtils.findInHotbar(Items.ELYTRA);
            if (!hotbarChestplateSlot.found()) {
                hotbarChestplateSlot = InvUtils.findInHotbar(Items.NETHERITE_CHESTPLATE);
            }

            // If a chestplate is found in the hotbar, equip it silently
            if (hotbarChestplateSlot.found()) {
                MC.interactionManager.clickSlot(
                        MC.player.currentScreenHandler.syncId,
                        6, // Chestplate slot
                        hotbarChestplateSlot.slot(),
                        SlotActionType.SWAP,
                        MC.player
                );
                return true;
            } else {
                // Look for a chestplate in the entire inventory
                FindItemResult inventorySlot = InvUtils.find(Items.ELYTRA);
                if (!inventorySlot.found()) {
                    inventorySlot = InvUtils.find(Items.NETHERITE_CHESTPLATE);
                }

                // If no chestplate is found, return false
                if (!inventorySlot.found()) {
                    return false;
                } else {
                    // Find a hotbar slot to swap with
                    FindItemResult hotbarSlot = InvUtils.findInHotbar((itemStack) -> {
                        return itemStack.getItem() != Items.AIR; // Ensure the slot is not empty
                    });

                    // Perform the swap to equip the chestplate
                    MC.interactionManager.clickSlot(
                            MC.player.currentScreenHandler.syncId,
                            inventorySlot.slot(),
                            hotbarSlot.found() ? hotbarSlot.slot() : 0,
                            SlotActionType.SWAP,
                            MC.player
                    );
                    MC.interactionManager.clickSlot(
                            MC.player.currentScreenHandler.syncId,
                            6, // Chestplate slot
                            hotbarSlot.found() ? hotbarSlot.slot() : 0,
                            SlotActionType.SWAP,
                            MC.player
                    );
                    MC.interactionManager.clickSlot(
                            MC.player.currentScreenHandler.syncId,
                            inventorySlot.slot(),
                            hotbarSlot.found() ? hotbarSlot.slot() : 0,
                            SlotActionType.SWAP,
                            MC.player
                    );
                    return true;
                }
            }
        } else {
            // If the player is already wearing a chestplate or elytra, return false
            return false;
        }
    }

    public static boolean silentSwapEquipElytra() {
        // Check if the player is already wearing an elytra
        if (MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            return false;
        } else {
            // Look for an elytra in the hotbar
            FindItemResult inventorySlot = InvUtils.findInHotbar(Items.ELYTRA);
            if (inventorySlot.found()) {
                // Equip the elytra silently
                MC.interactionManager.clickSlot(
                        MC.player.currentScreenHandler.syncId,
                        6, // Chestplate slot
                        inventorySlot.slot(),
                        SlotActionType.SWAP,
                        MC.player
                );
                return true;
            } else {
                // Look for an elytra in the entire inventory
                inventorySlot = InvUtils.find(Items.ELYTRA);
                if (!inventorySlot.found()) {
                    return false;
                } else {
                    // Find a hotbar slot to swap with
                    FindItemResult hotbarSlot = InvUtils.findInHotbar((itemStack) -> {
                        return itemStack.getItem() != Items.AIR; // Ensure the slot is not empty
                    });

                    // Perform the swap to equip the elytra
                    MC.interactionManager.clickSlot(
                            MC.player.currentScreenHandler.syncId,
                            inventorySlot.slot(),
                            hotbarSlot.found() ? hotbarSlot.slot() : 0,
                            SlotActionType.SWAP,
                            MC.player
                    );
                    MC.interactionManager.clickSlot(
                            MC.player.currentScreenHandler.syncId,
                            6, // Chestplate slot
                            hotbarSlot.found() ? hotbarSlot.slot() : 0,
                            SlotActionType.SWAP,
                            MC.player
                    );
                    MC.interactionManager.clickSlot(
                            MC.player.currentScreenHandler.syncId,
                            inventorySlot.slot(),
                            hotbarSlot.found() ? hotbarSlot.slot() : 0,
                            SlotActionType.SWAP,
                            MC.player
                    );
                    return true;
                }
            }
        }
    }
}
