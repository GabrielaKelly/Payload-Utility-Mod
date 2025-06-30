package net.payload.module.modules.combat;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.module.Category;
import net.payload.settings.PageGroup;
import net.payload.settings.types.EnumSetting;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.FindItemResult;
import net.payload.utils.player.InvUtils;

import net.payload.Payload;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.payload.utils.player.combat.EntityUtil;

public class AutoTotem extends Module implements TotemPopListener, Render3DListener, GameLeftListener, TickListener, SendMovementPacketListener {

    public enum Mode {
        Passive, PVP
    }

    private final EnumSetting<AutoTotem.Mode> mode = EnumSetting.<AutoTotem.Mode>builder()
            .id("autototem_mode")
            .displayName("Mode")
            .description("Decides whether or not you always want a totem")
            .defaultValue(Mode.Passive)
            .build();

    public FloatSetting healthTrigger = FloatSetting.builder()
            .id("autototem_health")
            .displayName("Totem Health")
            .description("The health at which the totem will be placed into your hand.")
            .defaultValue(36.0f)
            .minValue(1.0f)
            .maxValue(36.0f)
            .step(1.0f)
            .build();

    public FloatSetting crystalRadiusTrigger = FloatSetting.builder()
            .id("autototem_crystal_radius")
            .displayName("Crystal Radius")
            .description("The radius at which a placed end crystal will trigger autototem.")
            .defaultValue(6.0f)
            .minValue(1.0f)
            .maxValue(10.0f)
            .step(1.0f)
            .build();

    public BooleanSetting mainHand = BooleanSetting.builder()
            .id("autototem_mainhand")
            .displayName("Mainhand")
            .description("Places totem in main hand instead of off-hand")
            .defaultValue(false)
            .build();

    public BooleanSetting antiPop = BooleanSetting.builder()
            .id("autototem_antipop")
            .displayName("AntiPop")
            .description("Uses multiple listeners to prevent popping")
            .defaultValue(true)
            .build();

    public FloatSetting mainHandSlot = FloatSetting.builder()
            .id("autototem_mainhand_slot")
            .displayName("Mainhand Slot")
            .defaultValue(8.0f)
            .minValue(0.0f)
            .maxValue(8.0f)
            .step(1.0f)
            .build();

    public BooleanSetting popText = BooleanSetting.builder()
            .id("autototem_poptext")
            .displayName("Pop Text")
            .description("Sends a client message for every totem popped")
            .defaultValue(true)
            .build();

    public BooleanSetting gapple = BooleanSetting.builder()
            .id("autototem_gapples")
            .displayName("Sword GappleSwap")
            .description("Uses gapples when your safe")
            .defaultValue(true)
            .build();

    public BooleanSetting crystal = BooleanSetting.builder()
            .id("autototem_poptext")
            .displayName("Safety Crystals")
            .description("Uses endcrystals when your safe")
            .defaultValue(false)
            .build();

    public AutoTotem() {
        super("AutoTotem");

        this.setName("AutoTotem");
        this.setCategory(Category.of("Combat"));
        this.setDescription("Automatically holds a Totem");

        PageGroup settingsPages = PageGroup.Builder.builder()
                .id("autototem_pages")
                .description("AutoTotem settings pages")
                .build();

        PageGroup.Page pvp = new PageGroup.Page("PVP");
        PageGroup.Page passive = new PageGroup.Page("Passive");

        this.addSetting(mode);
        this.addSetting(healthTrigger);
        this.addSetting(popText);

        pvp.addSetting(gapple);
        pvp.addSetting(crystal);
        pvp.addSetting(antiPop);

        passive.addSetting(crystalRadiusTrigger);
        passive.addSetting(mainHand);
        passive.addSetting(mainHandSlot);

        settingsPages.addPage(passive);
        settingsPages.addPage(pvp);

        this.addSetting(settingsPages);
    }

    int totems = 0;
    private final CacheTimer timer = new CacheTimer();

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TotemPopListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.AddListener(TotemPopListener.class, this);
        Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
    }

    @Override
    public void onToggle() {
        // Additional functionality for toggling the module can be added here
    }


    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
        if (mode.getValue() == Mode.PVP) {
            updatePVP();
        }
    }

    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Post event) {

    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (mode.getValue() == Mode.PVP) {
            updatePVP();
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    private void passiveSwitchToTotem() {
        if (nullCheck()) return;

        MinecraftClient MC = MinecraftClient.getInstance();
        PlayerInventory inventory = MC.player.getInventory();
        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);

        if (!mainHand.getValue() && (MC.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING)) {
            InvUtils.move().from(result.slot()).toOffhand();
        }

        else if (mainHand.getValue()) {
            if ((InvUtils.getStackInSlot(Math.round(mainHandSlot.get())).getItem() != Items.TOTEM_OF_UNDYING)) {
                InvUtils.move().from(result.slot()).toHotbar(Math.round(mainHandSlot.get()));
            }

            if (InvUtils.getStackInSlot(inventory.selectedSlot).getItem() != Items.TOTEM_OF_UNDYING) {
                InvUtils.swap((Math.round(mainHandSlot.get())), false);
            }
        }
    }

    @Override
    public void onTotemPop(TotemPopEvent event) {
        if (nullCheck()) return;

        if (mode.getValue() == Mode.PVP) {
            updatePVP();
        }

        Entity entity = event.getEntity();
        String playerName = entity.getName().getString();

        if (entity instanceof PlayerEntity) {
            int pops = Payload.getInstance().combatManager.getPop(playerName);

            if (entity == MC.player) {
                FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
                if (popText.getValue() && result.found()) {
                    sendTotemMessage(String.format("You have popped %d times, %d totems remain",
                            pops, (result.count() - 1)));
                }
            } else {
                if (popText.getValue()) {
                    sendTotemMessage(String.format("%s popped %d %s",
                            playerName, pops, pops == 1 ? "time" : "times"));
                }
            }
        }
    }

    @Override
    public void onGameLeft(GameLeftEvent event) {
        Payload.getInstance().combatManager.resetPops();
    }

    @Override
    public void onRender(Render3DEvent event) {
        if (nullCheck()) return;

        switch (mode.getValue()) {
            case Mode.PVP: {
                updatePVP();
                break;
            }
            case Mode.Passive: {
                for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                    if (entity instanceof EndCrystalEntity) {
                        if (MC.player.distanceTo(entity) < crystalRadiusTrigger.getValue()) {
                            passiveSwitchToTotem();
                            break;
                        }
                    }
                }

                if (MC.player.getHealth() <= healthTrigger.getValue()) {
                    passiveSwitchToTotem();
                    break;
                }
            }
        }
    }

    private void updatePVP() {
        if (nullCheck()) return;
        totems = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
        if (MC.currentScreen != null && !(MC.currentScreen instanceof ChatScreen) && !(MC.currentScreen instanceof InventoryScreen) && !(MC.currentScreen instanceof GameMenuScreen)) {
            return;
        }
        if (!timer.passedMs(200)) {
            return;
        }
        if (gapple.getValue() && isGoodMainhandItem() && MC.options.useKey.isPressed()) {
            if (MC.player.getOffHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE && MC.player.getOffHandStack().getItem() != Items.GOLDEN_APPLE) {
                int itemSlot = findItemInventorySlot(Items.ENCHANTED_GOLDEN_APPLE);
                if (itemSlot == -1) {
                    itemSlot = findItemInventorySlot(Items.GOLDEN_APPLE);
                }
                if (itemSlot != -1) {
                    MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, MC.player);
                    MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, MC.player);
                    MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, MC.player);
                    EntityUtil.syncInventory();
                    timer.reset();
                }
            }
            return;
        }
        if (MC.player.getHealth() + MC.player.getAbsorptionAmount() > healthTrigger.getValue()) {
            if (crystal.getValue() && MC.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
                int itemSlot = findItemInventorySlot(Items.END_CRYSTAL);
                if (itemSlot != -1) {
                    MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, MC.player);
                    MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, MC.player);
                    MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, MC.player);
                    EntityUtil.syncInventory();
                    timer.reset();
                }
            }
            return;
        }
        if (MC.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING || MC.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }
        int itemSlot = findItemInventorySlot(Items.TOTEM_OF_UNDYING);
        if (itemSlot != -1) {
                MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, MC.player);
                MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, MC.player);
                MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, MC.player);
                EntityUtil.syncInventory();
            timer.reset();
        }
    }

    public static int findItemInventorySlot(Item item) {
        for (int i = 44; i >= 0; --i) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public boolean isGoodMainhandItem() {
        return MC.player.getMainHandStack().getItem() instanceof SwordItem ||
                MC.player.getMainHandStack().getItem() instanceof TridentItem ||
                MC.player.getMainHandStack().getItem() instanceof PickaxeItem ||
                MC.player.getMainHandStack().getItem() instanceof ShovelItem ||
                MC.player.getMainHandStack().getItem() instanceof AxeItem ||
                MC.player.getMainHandStack().getItem() instanceof HoeItem;
    }
}