package net.payload.utils.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.payload.Payload;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.events.SendPacketEvent;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.event.listeners.SendPacketListener;
import net.payload.module.modules.client.AntiCheat;
import net.payload.utils.block.OtherBlockUtils;

import java.util.HashMap;
import java.util.Map;

import static net.payload.PayloadClient.MC;

public class InventoryUtil implements SendPacketListener, ReceivePacketListener {

    public InventoryUtil() {
        Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
    }

    static int lastSlot = -1;
    static int lastSelect = -1;
    public static void inventorySwap(int slot, int selectedSlot) {
        if (slot == lastSlot) {
            switchToSlot(lastSelect);
            lastSlot = -1;
            lastSelect = -1;
            return;
        }
        if (slot - 36 == selectedSlot) return;
        if (AntiCheat.INSTANCE.invSwapBypass.getValue()) {
            if (slot - 36 >= 0) {
                lastSlot = slot;
                lastSelect = selectedSlot;
                switchToSlot(slot - 36);
                return;
            }
            Int2ObjectMap<ItemStack> modifiedStacks = new Int2ObjectOpenHashMap<>();
            MC.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(
                    MC.player.currentScreenHandler.syncId,
                    slot,
                    0,
                    MC.player.currentScreenHandler.getRevision(),
                    SlotActionType.PICKUP,
                    MC.player.currentScreenHandler.getSlot(slot).getStack().copy(),
                    modifiedStacks
            ));
            //MC.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(slot));
            return;
        }
        MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, slot, selectedSlot, SlotActionType.SWAP, MC.player);
    }
    public static void switchToSlot(int slot) {
        MC.player.getInventory().selectedSlot = slot;
        MC.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }
    public static boolean holdingItem(Class clazz) {
        boolean result;
        ItemStack stack = MC.player.getMainHandStack();
        result = isInstanceOf(stack, clazz);
        if (!result) {
            result = isInstanceOf(stack, clazz);
        }
        return result;
    }

    public static boolean isInstanceOf(ItemStack stack, Class clazz) {
        if (stack == null) {
            return false;
        }
        Item item = stack.getItem();
        if (clazz.isInstance(item)) {
            return true;
        }
        if (item instanceof BlockItem) {
            Block block = Block.getBlockFromItem(item);
            return clazz.isInstance(block);
        }
        return false;
    }

    public static ItemStack getStackInSlot(int i) {
        return MC.player.getInventory().getStack(i);
    }
    public static int findItem(Item input) {
        for (int i = 0; i < 9; ++i) {
            Item item = getStackInSlot(i).getItem();
            if (Item.getRawId(item) != Item.getRawId(input)) continue;
            return i;
        }
        return -1;
    }

    public static int getIteMCount(Class clazz) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() instanceof BlockItem && clazz.isInstance(((BlockItem) entry.getValue().getItem()).getBlock())) {
                count = count + entry.getValue().getCount();
            }
        }
        return count;
    }
    public static int getIteMCount(Item item) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() != item) continue;
            count = count + entry.getValue().getCount();
        }
        return count;
    }

    public static int findClass(Class clazz) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = getStackInSlot(i);
            if (stack == ItemStack.EMPTY) continue;
            if (clazz.isInstance(stack.getItem())) {
                return i;
            }
            if (!(stack.getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem) stack.getItem()).getBlock()))
                continue;
            return i;
        }
        return -1;
    }

    public static int findClassInventorySlot(Class clazz) {
        for (int i = 0; i < 45; ++i) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY) continue;
            if (clazz.isInstance(stack.getItem())) {
                return i < 9 ? i + 36 : i;
            }
            if (!(stack.getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem) stack.getItem()).getBlock()))
                continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public static int findBlock(Block blockIn) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof BlockItem) || ((BlockItem) stack.getItem()).getBlock() != blockIn)
                continue;
            return i;
        }
        return -1;
    }

    public static int findUnBlock() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = getStackInSlot(i);
            if (stack.getItem() instanceof BlockItem)
                continue;
            return i;
        }
        return -1;
    }

    public static int findBlock() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = getStackInSlot(i);
            if (stack.getItem() instanceof BlockItem && !OtherBlockUtils.shiftBlocks.contains(Block.getBlockFromItem(stack.getItem())) && ((BlockItem) stack.getItem()).getBlock() != Blocks.COBWEB)
                return i;
        }
        return -1;
    }
    public static int findBlockInventorySlot(Block block) {
        return findItemInventorySlot(block.asItem());
    }
    public static int findItemInventorySlot(Item item) {
        for (int i = 0; i < 45; ++i) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<>();

        for (int current = 0; current <= 44; ++current) {
            fullInventorySlots.put(current, MC.player.getInventory().getStack(current));
        }

        return fullInventorySlots;
    }

    //Shore
    private static int currentSlot;

    public static void syncToClient() {
        if (isDesynced()) {
            setSlotForced(MC.player.getInventory().selectedSlot);
        }
    }

    public static void setSlot(int barSlot) {
        if (currentSlot != barSlot &&
                PlayerInventory.isValidHotbarIndex(barSlot)) {
            setSlotForced(barSlot);
        }
    }

    public static void setSlotForced(int barSlot) {
        MC.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(barSlot));
    }

    public static boolean isDesynced() {
        return MC.player.getInventory().selectedSlot != currentSlot;
    }

    public static int getItemCount(Item item) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() != item) continue;
            count = count + entry.getValue().getCount();
        }
        return count;
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        Packet<?> packet = event.GetPacket();
        if (packet instanceof UpdateSelectedSlotC2SPacket slotPacket) {
            int packetSlot = slotPacket.getSelectedSlot();
            if (!PlayerInventory.isValidHotbarIndex(packetSlot) ||
                    this.currentSlot == packetSlot) {
                event.cancel();
                return;
            }
            this.currentSlot = packetSlot;
        }
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
            Packet<?> packet = readPacketEvent.getPacket();
            if (packet instanceof UpdateSelectedSlotS2CPacket slotPacket) {
                this.currentSlot = slotPacket.slot();
            }
        }
}

