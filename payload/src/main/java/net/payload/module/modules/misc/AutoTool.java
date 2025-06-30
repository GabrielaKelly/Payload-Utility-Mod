package net.payload.module.modules.misc;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.combat.AutoTotem;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;

import java.util.ArrayList;
import java.util.List;

public class AutoTool extends Module implements TickListener {

    public enum Mode {
        Silk, Fortune, Neutral
    }

    private final EnumSetting<AutoTool.Mode> mode = EnumSetting.<AutoTool.Mode>builder()
            .id("autotool_mode")
            .displayName("Echest Filter")
            .description("Decides when to mine echests")
            .defaultValue(AutoTool.Mode.Silk)
            .build();


    private BooleanSetting swapBack = BooleanSetting.builder()
            .id("autotool_swapBack")
            .displayName("Swap Back")
            .description("Swap back to your previous tool after block breaking.")
            .defaultValue(true)
            .build();

    private BooleanSetting saveItem = BooleanSetting.builder()
            .id("autotool_saveItem")
            .displayName("Save Item")
            .description("Skip tools that are low on durability.")
            .defaultValue(false)
            .build();

    private BooleanSetting silent = BooleanSetting.builder()
            .id("autotool_silent")
            .displayName("Silent")
            .description("Switch tools silently without visibly changing the selected slot.")
            .defaultValue(false)
            .build();

    public static int itemIndex;
    private boolean swap;
    private long swapDelay;
    private final List<Integer> lastItem = new ArrayList<>();

    public AutoTool() {
        super("AutoTool");
        this.setCategory(Category.of("Misc"));
        this.setDescription("Automatically switches to the ideal tool for breaking blocks");
        this.addSetting(mode);
        this.addSetting(swapBack);
        this.addSetting(saveItem);
        this.addSetting(silent);
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
        if (nullCheck()) return;

        if (Payload.getInstance().moduleManager.packetMine.state.getValue()) {
            return;
        }

        if (!(MC.crosshairTarget instanceof BlockHitResult)) {
            return;
        }

        BlockHitResult result = (BlockHitResult) MC.crosshairTarget;
        BlockPos pos = result.getBlockPos();
        BlockState state = MC.world.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        int toolSlot = getTool(pos);
        // Only proceed if a valid tool is found and the attack key is pressed.

        if (toolSlot != -1 && MC.options.attackKey.isPressed()) {
            // Save the current slot so that we can swap back later.
            lastItem.add(MC.player.getInventory().selectedSlot);

            if (silent.getValue()) {
                MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(toolSlot));
            } else {
                MC.player.getInventory().selectedSlot = toolSlot;
            }
            itemIndex = toolSlot;
            swap = true;
            swapDelay = System.currentTimeMillis();
        } else if (swap && !lastItem.isEmpty() && System.currentTimeMillis() >= swapDelay + 300 && swapBack.getValue()) {
            // Swap back to the previous item after a delay.
            int previousSlot = lastItem.get(0);
            if (silent.getValue()) {
                MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            } else {
                MC.player.getInventory().selectedSlot = previousSlot;
            }
            itemIndex = previousSlot;
            lastItem.clear();
            swap = false;
        }
    }

    public int getOutsideTool(BlockPos pos) {
        int slot = this.getTool(pos);
        return slot != -1 ? slot : MC.player.getInventory().selectedSlot;
    }

    /**
     * Returns the best tool slot (0-8) for breaking the block at the given position.
     * If no tool is found, returns -1.
     */
    private int getTool(final BlockPos pos) {
        int index = -1;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack != ItemStack.EMPTY) {
                if (!(MC.player.getInventory().getStack(i).getMaxDamage() - MC.player.getInventory().getStack(i).getDamage() > 10) && saveItem.getValue())
                    continue;

                final float digSpeed = EnchantmentHelper.getLevel(MC.world.getRegistryManager().getOrThrow(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY.getValue()).orElse(null), stack);
                final float destroySpeed = stack.getMiningSpeedMultiplier(MC.world.getBlockState(pos));

                if (MC.world.getBlockState(pos).getBlock() instanceof AirBlock) return -1;
                if (MC.world.getBlockState(pos).getBlock() instanceof EnderChestBlock && (mode.getValue() == Mode.Silk)) {
                    if (EnchantmentHelper.getLevel(MC.world.getRegistryManager().getOrThrow(Enchantments.SILK_TOUCH.getRegistryRef()).getEntry(Enchantments.SILK_TOUCH.getValue()).orElse(null), stack) > 0 && digSpeed + destroySpeed > CurrentFastest) {
                        CurrentFastest = digSpeed + destroySpeed;
                        index = i;
                    }
                }
                else if (MC.world.getBlockState(pos).getBlock() instanceof EnderChestBlock && (mode.getValue() == Mode.Fortune)) {
                    if (EnchantmentHelper.getLevel(MC.world.getRegistryManager().getOrThrow(Enchantments.FORTUNE.getRegistryRef()).getEntry(Enchantments.FORTUNE.getValue()).orElse(null), stack) > 0 && digSpeed + destroySpeed > CurrentFastest) {
                        CurrentFastest = digSpeed + destroySpeed;
                        index = i;
                    }
                }
                else if (digSpeed + destroySpeed > CurrentFastest) {
                    CurrentFastest = digSpeed + destroySpeed;
                    index = i;
                }
            }
        }
        return index;
    }

    @Override
    public void onTick(TickEvent.Post event) {
        // You can add post-tick logic here if needed.
    }
}
