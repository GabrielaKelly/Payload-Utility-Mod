
package net.payload.module.modules.combat;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;

import java.util.ArrayList;

public class HotbarRefill extends Module implements TickListener {

    private FloatSetting percent = FloatSetting.builder().id("hotbar_percent").displayName("Percent")
            .defaultValue(25f).minValue(0f).maxValue(80f)
            .step(5f).build();

    public HotbarRefill() {
        super("HotbarRefill");

        this.setCategory(Category.of("Combat"));
        this.setDescription("Refills items in your hotbar");
        this.addSetting(percent);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (MC.player == null || MC.currentScreen != null) {
            return;
        }
            // Check each hotbar slot (0-8)
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = MC.player.getInventory().getStack(i);
                // Check if stack is not empty and is stackable
                if (!stack.isEmpty() && stack.isStackable()) {
                    double stackPercent = (float) stack.getCount() / stack.getMaxCount() * 100.0F;
                    // Replenish if stack count is 1 or below configured percentage
                    if (stack.getCount() == 1 ||
                            stackPercent <= Math.max(percent.getValue(), 5.0F)) {
                        replenishStack(stack, i);
                    }
                }
            }
    }

    public boolean doSkip(Item item) {
            if (item == Items.MAP || item == Items.FILLED_MAP) {
                return true;
            }

            if (item == Items.WHITE_BANNER ||
                    item == Items.ORANGE_BANNER ||
                    item == Items.MAGENTA_BANNER ||
                    item == Items.LIGHT_BLUE_BANNER ||
                    item == Items.YELLOW_BANNER ||
                    item == Items.LIME_BANNER ||
                    item == Items.PINK_BANNER ||
                    item == Items.GRAY_BANNER ||
                    item == Items.LIGHT_GRAY_BANNER ||
                    item == Items.CYAN_BANNER ||
                    item == Items.PURPLE_BANNER ||
                    item == Items.BLUE_BANNER ||
                    item == Items.BROWN_BANNER ||
                    item == Items.GREEN_BANNER ||
                    item == Items.RED_BANNER ||
                    item == Items.BLACK_BANNER) {
                return true;
            }

            if (item == Items.FLOWER_BANNER_PATTERN ||
                    item == Items.CREEPER_BANNER_PATTERN ||
                    item == Items.SKULL_BANNER_PATTERN ||
                    item == Items.MOJANG_BANNER_PATTERN ||
                    item == Items.GLOBE_BANNER_PATTERN ||
                    item == Items.PIGLIN_BANNER_PATTERN ||
                    item == Items.FLOW_BANNER_PATTERN ||
                    item == Items.GUSTER_BANNER_PATTERN ||
                    item == Items.FIELD_MASONED_BANNER_PATTERN ||
                    item == Items.BORDURE_INDENTED_BANNER_PATTERN) {
                return true;
            }

            return false;
        }

    private void replenishStack(ItemStack item, int hotbarSlot) {
        int total = item.getCount();

        // Search through main inventory (slots 9-35)
        for(int i = 9; i < 36; ++i) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem().equals(item.getItem())) {
                Item stackItem = stack.getItem();

                if (doSkip(stackItem)) return;

                // Special handling for BlockItems
                if (stackItem instanceof BlockItem) {
                    BlockItem blockItem = (BlockItem)stackItem;
                    Item itemItem = item.getItem();
                    if (!(itemItem instanceof BlockItem)) {
                        continue;
                    }
                    BlockItem targetBlockItem = (BlockItem)itemItem;
                    // Skip if blocks don't match
                    if (blockItem.getBlock() != targetBlockItem.getBlock()) {
                        continue;
                    }
                }

                // If items match and total is less than max stack size
                if (stack.getItem() == item.getItem() && total < stack.getMaxCount()) {
                    // Perform inventory operations
                    pickupSlot(i);
                    pickupSlot(hotbarSlot + 36);
                    // Return any remaining items
                    if (!MC.player.currentScreenHandler.getCursorStack().isEmpty()) {
                        pickupSlot(i);
                    }
                    total += stack.getCount();
                }
            }
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    public void pickupSlot(int slot) {
        click(slot, 0, SlotActionType.PICKUP);
    }

    private void click(int slot, int button, SlotActionType actionType) {
        ScreenHandler screenHandler = MC.player.currentScreenHandler;
        ArrayList<ItemStack> beforeSlots = captureInventoryState(screenHandler);

        // Perform the click action
        screenHandler.onSlotClick(slot, button, actionType, MC.player);

        // Track changes and send packet
        Int2ObjectOpenHashMap<ItemStack> changedSlots = trackInventoryChanges(
                screenHandler, beforeSlots);

        sendClickPacket(screenHandler, slot, button, actionType, changedSlots);
    }

    private ArrayList<ItemStack> captureInventoryState(ScreenHandler handler) {
        ArrayList<ItemStack> slots = Lists.newArrayListWithCapacity(handler.slots.size());
        for (Slot slot : handler.slots) {
            slots.add(slot.getStack().copy());
        }
        return slots;
    }

    private Int2ObjectOpenHashMap<ItemStack> trackInventoryChanges(
            ScreenHandler handler, ArrayList<ItemStack> beforeSlots) {
        Int2ObjectOpenHashMap<ItemStack> changes = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < beforeSlots.size(); ++i) {
            ItemStack before = beforeSlots.get(i);
            ItemStack after = handler.slots.get(i).getStack();
            if (!ItemStack.areEqual(before, after)) {
                changes.put(i, after.copy());
            }
        }
        return changes;
    }

    private void sendClickPacket(ScreenHandler handler, int slot, int button,
                                 SlotActionType actionType, Int2ObjectOpenHashMap<ItemStack> changedSlots) {
        MC.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                handler.syncId,
                handler.getRevision(),
                slot,
                button,
                actionType,
                handler.getCursorStack().copy(),
                changedSlots
        ));
    }
}