package net.payload.module.modules.misc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.gui.colors.Color;
import net.payload.mixin.interfaces.IPlayerMoveC2SPacket;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.client.AntiCheat;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.*;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.combat.CombatUtil;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.player.combat.SwingSide;
import net.payload.utils.render.Render3D;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketMine extends Module implements TickListener, SendPacketListener, StartBreakingBlockListener, Render3DListener, LookAtListener, KeyDownListener {

    private final SettingGroup generalSettings;
    private final SettingGroup checkSettings;
    private final SettingGroup placeSettings;
    private final SettingGroup rotationSettings;
    private final SettingGroup renderSettings;

    public static final List<Block> godBlocks = Arrays.asList(Blocks.COMMAND_BLOCK, Blocks.LAVA_CAULDRON,
            Blocks.LAVA, Blocks.WATER_CAULDRON, Blocks.WATER, Blocks.BEDROCK, Blocks.BARRIER,
            Blocks.END_PORTAL, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME);

    private final FloatSetting delay = FloatSetting.builder()
            .id("packetmine_delay")
            .displayName("Delay")
            .description("Mining operation delay in milliseconds")
            .defaultValue(50f)
            .minValue(0f)
            .maxValue(500f)
            .step(1f)
            .build();

    private final FloatSetting damage = FloatSetting.builder()
            .id("packetmine_damage")
            .displayName("Damage")
            .description("Mining damage per operation")
            .defaultValue(0.7f)
            .minValue(0.0f)
            .maxValue(2.0f)
            .step(0.01f)
            .build();

    public final FloatSetting range = FloatSetting.builder()
            .id("packetmine_range")
            .displayName("Range")
            .description("Maximum mining reach distance")
            .defaultValue(6f)
            .minValue(3.0f)
            .maxValue(10.0f)
            .step(0.1f)
            .build();

    private final FloatSetting maxBreak = FloatSetting.builder()
            .id("packetmine_max_break")
            .displayName("MaxBreak")
            .description("Maximum blocks to break simultaneously")
            .defaultValue(3f)
            .minValue(0f)
            .maxValue(20f)
            .step(1f)
            .build();

    private final BooleanSetting grim = BooleanSetting.builder()
            .id("packetmine_grim")
            .displayName("Grim")
            .description("Enable Grim compatibility mode")
            .defaultValue(false)
            .build();

    private final BooleanSetting instant = BooleanSetting.builder()
            .id("packetmine_instant")
            .displayName("Instant")
            .description("Break blocks instantly")
            .defaultValue(false)
            .build();

    private final BooleanSetting wait = BooleanSetting.builder()
            .id("packetmine_wait")
            .displayName("Wait")
            .description("Wait for block breaking completion")
            .defaultValue(true)
            .build();

    private final BooleanSetting mineAir = BooleanSetting.builder()
            .id("packetmine_mine_air")
            .displayName("MineAir")
            .description("Continue mining animation in air")
            .defaultValue(true)
            .build();

    private enum SwapMode {
        Inventory, Silent
    }

    private final EnumSetting<SwapMode> autoSwap = EnumSetting.<SwapMode>builder()
            .id("packetmine_autoswap")
            .displayName("Auto Swap")
            .description("Automatically switches to your best tool")
            .defaultValue(SwapMode.Silent)
            .build();

    private final BooleanSetting doubleBreak = BooleanSetting.builder()
            .id("packetmine_double_break")
            .displayName("DoubleBreak")
            .description("Break two blocks at once")
            .defaultValue(true)
            .build();

    private final BooleanSetting stopPacket = BooleanSetting.builder()
            .id("packetmine_stop_packet")
            .displayName("StopPacket")
            .description("Stop redundant packets")
            .defaultValue(true)
            .build();

    private final BooleanSetting setAir = BooleanSetting.builder()
            .id("packetmine_set_air")
            .displayName("SetAir")
            .description("Set broken blocks to air")
            .defaultValue(false)
            .build();

    private final BooleanSetting swing = BooleanSetting.builder()
            .id("packetmine_swing")
            .displayName("Swing")
            .description("Perform swing animation")
            .defaultValue(true)
            .build();

    private final BooleanSetting endSwing = BooleanSetting.builder()
            .id("packetmine_end_swing")
            .displayName("EndSwing")
            .description("Perform swing animation at the end")
            .defaultValue(false)
            .build();

    public final EnumSetting<SwingSide> swingMode = EnumSetting.<SwingSide>builder()
            .id("packetmine_swing_mode")
            .displayName("SwingMode")
            .description("Mode of swing animation")
            .defaultValue(SwingSide.All)
            .build();

    // Check Settings
    private final BooleanSetting switchReset = BooleanSetting.builder()
            .id("packetmine_switch_reset")
            .displayName("SwitchReset")
            .description("Reset on switch")
            .defaultValue(false)
            .build();

    public final BooleanSetting preferWeb = BooleanSetting.builder()
            .id("packetmine_prefer_web")
            .displayName("PreferWeb")
            .description("Prefer breaking webs")
            .defaultValue(true)
            .build();

    public final BooleanSetting preferHead = BooleanSetting.builder()
            .id("packetmine_prefer_head")
            .displayName("PreferHead")
            .description("Prefer breaking head blocks")
            .defaultValue(true)
            .build();

    public final BooleanSetting farCancel = BooleanSetting.builder()
            .id("packetmine_far_cancel")
            .displayName("FarCancel")
            .description("Cancel when too far")
            .defaultValue(false)
            .build();

    private final BooleanSetting onlyGround = BooleanSetting.builder()
            .id("packetmine_only_ground")
            .displayName("OnlyGround")
            .description("Only mine when on ground")
            .defaultValue(true)
            .build();

    private final BooleanSetting checkGround = BooleanSetting.builder()
            .id("packetmine_check_ground")
            .displayName("CheckGround")
            .description("Check if on ground")
            .defaultValue(true)
            .build();

    private final BooleanSetting smart = BooleanSetting.builder()
            .id("packetmine_smart")
            .displayName("Smart")
            .description("Smart ground checking")
            .defaultValue(true)
            .build();

    private final BooleanSetting usingPause = BooleanSetting.builder()
            .id("packetmine_using_pause")
            .displayName("UsingPause")
            .description("Pause while using items")
            .defaultValue(false)
            .build();

    private final BooleanSetting bypassGround = BooleanSetting.builder()
            .id("packetmine_bypass_ground")
            .displayName("BypassGround")
            .description("Bypass ground checks")
            .defaultValue(true)
            .build();

    private final FloatSetting bypassTime = FloatSetting.builder()
            .id("packetmine_bypass_time")
            .displayName("BypassTime")
            .description("Time for ground bypass")
            .defaultValue(400f)
            .minValue(0f)
            .maxValue(2000f)
            .step(1f)
            .build();

    private final BooleanSetting rotate = BooleanSetting.builder()
            .id("packetmine_start_rotate")
            .displayName("StartRotate")
            .description("Rotate at the start of mining")
            .defaultValue(true)
            .build();

    private final BooleanSetting endRotate = BooleanSetting.builder()
            .id("packetmine_end_rotate")
            .displayName("EndRotate")
            .description("Rotate at the end of mining")
            .defaultValue(true)
            .build();

    private final FloatSetting syncTime = FloatSetting.builder()
            .id("packetmine_sync_time")
            .displayName("Sync")
            .description("Rotation synchronization time")
            .defaultValue(300f)
            .minValue(0f)
            .maxValue(1000f)
            .step(1f)
            .build();

    private final BooleanSetting yawStep = BooleanSetting.builder()
            .id("packetmine_yaw_step")
            .displayName("YawStep")
            .description("Enable yaw stepping for smoother rotations")
            .defaultValue(false)
            .build();

    private final FloatSetting steps = FloatSetting.builder()
            .id("packetmine_steps")
            .displayName("Steps")
            .description("Step size for yaw rotation")
            .defaultValue(0.05f)
            .minValue(0f)
            .maxValue(1f)
            .step(0.01f)
            .build();

    private final BooleanSetting checkFov = BooleanSetting.builder()
            .id("packetmine_check_fov")
            .displayName("OnlyLooking")
            .description("Only mine blocks within FOV")
            .defaultValue(true)
            .build();

    private final FloatSetting fov = FloatSetting.builder()
            .id("packetmine_fov")
            .displayName("Fov")
            .description("Field of view range for mining")
            .defaultValue(30f)
            .minValue(0f)
            .maxValue(50f)
            .step(1f)
            .build();

    private final FloatSetting priority = FloatSetting.builder()
            .id("packetmine_priority")
            .displayName("Priority")
            .description("Rotation priority level")
            .defaultValue(10f)
            .minValue(0f)
            .maxValue(100f)
            .step(1f)
            .build();

    public final BooleanSetting crystal = BooleanSetting.builder()
            .id("packetmine_crystal")
            .displayName("PlaceCrystal")
            .description("Enable crystal placement functionality")
            .defaultValue(false)
            .build();

    private final BooleanSetting onlyHeadBomber = BooleanSetting.builder()
            .id("packetmine_only_cev")
            .displayName("OnlyCev")
            .description("Only perform crystal end vessel attacks")
            .defaultValue(true)
            .build();

    private final BooleanSetting waitPlace = BooleanSetting.builder()
            .id("packetmine_wait_place")
            .displayName("WaitPlace")
            .description("Wait before placing crystals")
            .defaultValue(true)
            .build();

    private final BooleanSetting spamPlace = BooleanSetting.builder()
            .id("packetmine_spam_place")
            .displayName("SpamPlace")
            .description("Continuously place crystals")
            .defaultValue(false)
            .build();

    private final BooleanSetting afterBreak = BooleanSetting.builder()
            .id("packetmine_after_break")
            .displayName("AfterBreak")
            .description("Place crystal after block break")
            .defaultValue(true)
            .build();

    private final BooleanSetting checkDamage = BooleanSetting.builder()
            .id("packetmine_detect_progress")
            .displayName("DetectProgress")
            .description("Check mining progress before placing")
            .defaultValue(true)
            .build();

    private final FloatSetting crystalDamage = FloatSetting.builder()
            .id("packetmine_progress")
            .displayName("Progress")
            .description("Required mining progress before crystal placement")
            .defaultValue(0.9f)
            .minValue(0.0f)
            .maxValue(1.0f)
            .step(0.01f)
            .build();

    public final KeybindSetting obsidian = KeybindSetting.builder()
            .id("packetmine_obsidian")
            .displayName("Obsidian")
            .description("Keybind for obsidian placement")
            .defaultValue(InputUtil.fromKeyCode(GLFW.GLFW_KEY_UNKNOWN, 0))
            .build();

    private final KeybindSetting enderChest = KeybindSetting.builder()
            .id("packetmine_enderchest")
            .displayName("EnderChest")
            .description("Keybind for ender chest placement")
            .defaultValue(InputUtil.fromKeyCode(GLFW.GLFW_KEY_UNKNOWN, 0))
            .build();

    private final FloatSetting placeDelay = FloatSetting.builder()
            .id("packetmine_place_delay")
            .displayName("PlaceDelay")
            .description("Delay between block placements")
            .defaultValue(100f)
            .minValue(0f)
            .maxValue(1000f)
            .step(1f)
            .build();

    private final BooleanSetting renderProgress = BooleanSetting.builder()
            .id("packetmine_renderprog")
            .displayName("Render Progress")
            .description("Render mining progress")
            .defaultValue(true)
            .build();

    private final BooleanSetting renderTextProgress = BooleanSetting.builder()
            .id("packetmine_rendertextprog")
            .displayName("Render Text")
            .description("Render mining progress text")
            .defaultValue(true)
            .build();

    private final BooleanSetting renderOutside = BooleanSetting.builder()
            .id("packetmine_rendertextprog")
            .displayName("Render Block")
            .description("Render mining block")
            .defaultValue(false)
            .build();

    private ColorSetting outsideColor = ColorSetting.builder()
            .id("packetmine_outsidecolor")
            .displayName("Block Color")
            .defaultValue(new Color(0, 0, 150, 50))
            .build();

    public FloatSetting lineThickness = FloatSetting.builder()
            .id("packetmine_thickness")
            .displayName("Line Thickness")
            .defaultValue(0f)
            .minValue(0f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    public PacketMine() {
        super("PacketMine");

        this.setCategory(Category.of("Misc"));
        this.setDescription("Sends mining packets");

        INSTANCE = this;

        generalSettings = SettingGroup.Builder.builder()
                .id("packetmine_General")
                .displayName("General")
                .description("General packet mine settings")
                .build();

        checkSettings = SettingGroup.Builder.builder()
                .id("packetmine_Check")
                .displayName("Check")
                .description("Check settings for packet mine")
                .build();

        placeSettings = SettingGroup.Builder.builder()
                .id("packetmine_Place")
                .displayName("CPVP Place")
                .description("Places crystals, for breaking others surround")
                .build();

        rotationSettings = SettingGroup.Builder.builder()
                .id("packetmine_Rotations")
                .displayName("Rotations")
                .description("Rotates you")
                .build();

        renderSettings = SettingGroup.Builder.builder()
                .id("packetmine_Render")
                .displayName("Render")
                .description("Rendering")
                .build();



        // Add settings to general group
        generalSettings.addSetting(autoSwap);
        generalSettings.addSetting(delay);
        generalSettings.addSetting(damage);
        generalSettings.addSetting(range);
        generalSettings.addSetting(maxBreak);
        generalSettings.addSetting(grim);
        generalSettings.addSetting(instant);
        generalSettings.addSetting(wait);
        generalSettings.addSetting(mineAir);
        generalSettings.addSetting(doubleBreak);
        generalSettings.addSetting(stopPacket);
        generalSettings.addSetting(setAir);
        generalSettings.addSetting(swing);
        generalSettings.addSetting(endSwing);
        generalSettings.addSetting(swingMode);

        checkSettings.addSetting(switchReset);
        checkSettings.addSetting(preferWeb);
        checkSettings.addSetting(preferHead);
        checkSettings.addSetting(farCancel);
        checkSettings.addSetting(onlyGround);
        checkSettings.addSetting(checkGround);
        checkSettings.addSetting(smart);
        checkSettings.addSetting(usingPause);
        checkSettings.addSetting(bypassGround);
        checkSettings.addSetting(bypassTime);

        rotationSettings.addSetting(rotate);
        rotationSettings.addSetting(endRotate);
        rotationSettings.addSetting(syncTime);
        rotationSettings.addSetting(yawStep);
        rotationSettings.addSetting(steps);
        rotationSettings.addSetting(checkFov);
        rotationSettings.addSetting(fov);
        rotationSettings.addSetting(priority);

        placeSettings.addSetting(crystal);
        placeSettings.addSetting(onlyHeadBomber);
        placeSettings.addSetting(waitPlace);
        placeSettings.addSetting(spamPlace);
        placeSettings.addSetting(afterBreak);
        placeSettings.addSetting(checkDamage);
        placeSettings.addSetting(crystalDamage);
        placeSettings.addSetting(placeDelay);
        placeSettings.addSetting(obsidian);
        placeSettings.addSetting(enderChest);

        renderSettings.addSetting(renderOutside);
        renderSettings.addSetting(renderProgress);
        renderSettings.addSetting(renderTextProgress);
        renderSettings.addSetting(outsideColor);
        renderSettings.addSetting(lineThickness);

        // Add groups to module
        this.addSetting(generalSettings);
        this.addSetting(checkSettings);
        this.addSetting(rotationSettings);
        this.addSetting(placeSettings);
        this.addSetting(renderSettings);

        // Register settings with SettingManager
        SettingManager.registerSetting(generalSettings);
        SettingManager.registerSetting(autoSwap);
        SettingManager.registerSetting(delay);
        SettingManager.registerSetting(damage);
        SettingManager.registerSetting(range);
        SettingManager.registerSetting(maxBreak);
        SettingManager.registerSetting(grim);
        SettingManager.registerSetting(instant);
        SettingManager.registerSetting(wait);
        SettingManager.registerSetting(mineAir);
        SettingManager.registerSetting(doubleBreak);
        SettingManager.registerSetting(stopPacket);
        SettingManager.registerSetting(setAir);
        SettingManager.registerSetting(generalSettings);
        SettingManager.registerSetting(checkSettings);
        SettingManager.registerSetting(swing);
        SettingManager.registerSetting(endSwing);
        SettingManager.registerSetting(swingMode);
        SettingManager.registerSetting(switchReset);
        SettingManager.registerSetting(preferWeb);
        SettingManager.registerSetting(preferHead);
        SettingManager.registerSetting(farCancel);
        SettingManager.registerSetting(onlyGround);
        SettingManager.registerSetting(checkGround);
        SettingManager.registerSetting(smart);
        SettingManager.registerSetting(usingPause);
        SettingManager.registerSetting(bypassGround);
        SettingManager.registerSetting(bypassTime);
        SettingManager.registerSetting(rotationSettings);
        SettingManager.registerSetting(rotate);
        SettingManager.registerSetting(endRotate);
        SettingManager.registerSetting(syncTime);
        SettingManager.registerSetting(yawStep);
        SettingManager.registerSetting(steps);
        SettingManager.registerSetting(checkFov);
        SettingManager.registerSetting(fov);
        SettingManager.registerSetting(priority);
        SettingManager.registerSetting(placeSettings);
        SettingManager.registerSetting(crystal);
        SettingManager.registerSetting(onlyHeadBomber);
        SettingManager.registerSetting(waitPlace);
        SettingManager.registerSetting(spamPlace);
        SettingManager.registerSetting(afterBreak);
        SettingManager.registerSetting(checkDamage);
        SettingManager.registerSetting(crystalDamage);
        SettingManager.registerSetting(obsidian);
        SettingManager.registerSetting(enderChest);
        SettingManager.registerSetting(placeDelay);
        SettingManager.registerSetting(renderTextProgress);
        SettingManager.registerSetting(renderProgress);
        SettingManager.registerSetting(renderOutside);
        SettingManager.registerSetting(outsideColor);
        SettingManager.registerSetting(lineThickness);
    }

    int lastSlot = -1;
    public Vec3d directionVec = null;
    public static PacketMine INSTANCE;
    public static BlockPos breakPos;
    public static BlockPos secondPos;
    public static double progress = 0;
    private final CacheTimer mineTimer = new CacheTimer();
    private boolean startPacket = false;
    private int breakNumber = 0;
    private final CacheTimer secondTimer = new CacheTimer();
    private final CacheTimer delayTimer = new CacheTimer();
    private final CacheTimer placeTimer = new CacheTimer();
    public static boolean sendGroundPacket = false;
    public boolean isobbypressed = false;
    public boolean isenderpressed = false;

    public static BlockPos getBreakPos() {
        if (INSTANCE.state.getValue()) {
            return breakPos;
        }
        return null;
    }

    private int findCrystal() {
        if (autoSwap.getValue() == SwapMode.Inventory) {
            return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
        } else {
            return InventoryUtil.findItem(Items.END_CRYSTAL);
        }
    }
    private int findBlock(Block block) {
        if (autoSwap.getValue() == SwapMode.Inventory) {
            return InventoryUtil.findBlockInventorySlot(block);
        } else {
            return InventoryUtil.findBlock(block);
        }
    }

    private final CacheTimer sync = new CacheTimer();

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(StartBreakingBlockListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);

        startPacket = false;
        breakPos = null;
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(StartBreakingBlockListener.class, this);
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.AddListener(LookAtListener.class, this);
        Payload.getInstance().eventManager.AddListener(KeyDownListener.class, this);
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        if (event.GetKey() == obsidian.getValue().getCode()) {
            isobbypressed = true;
        }
        else isobbypressed = false;

        if (event.GetKey() == enderChest.getValue().getCode()) {
            isenderpressed = true;
        }
        else isenderpressed = false;
    }

    @Override
    public void onLook(LookAtEvent event) {
        if (rotate.getValue() && yawStep.getValue() && directionVec != null && !sync.passed(syncTime.getValue())) {
            event.setTarget(directionVec, steps.getValue(), priority.getValue());
        }
    }

    private void doSwap(int slot, int inv) {
        if (autoSwap.getValue() == SwapMode.Silent) {
            InventoryUtil.switchToSlot(slot);
        } else {
            InventoryUtil.inventorySwap(inv, MC.player.getInventory().selectedSlot);
        }
    }

    static DecimalFormat df = new DecimalFormat("0.0");

    private boolean placeCrystal() {
        int crystal = findCrystal();
        if (crystal != -1) {
            int oldSlot = MC.player.getInventory().selectedSlot;
            doSwap(crystal, crystal);
            OtherBlockUtils.placeCrystal(breakPos.up(), rotate.getValue());
            doSwap(oldSlot, crystal);
            placeTimer.reset();
            return !waitPlace.getValue();
        }
        return true;
    }

    boolean done = false;
    boolean skip = false;
    @Override
    public void onTick(TickEvent.Pre event) {
        if (skip) {
            skip = false;
            return;
        }
        if (MC.player.isDead()) {
            secondPos = null;
        }
        if (secondPos != null) {
            double time = getBreakTime(secondPos, MC.player.getInventory().selectedSlot, 1.1);
            if (secondTimer.passed(time)) {
                secondPos = null;
            } else if (stopPacket.getValue()) {
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, secondPos, OtherBlockUtils.getClickSide(secondPos), id));
            }
        }
        if (secondPos != null && isAir(secondPos)) {
            secondPos = null;
        }
        if (MC.player.isCreative()) {
            startPacket = false;
            breakNumber = 0;
            breakPos = null;
            return;
        }
        if (breakPos == null) {
            breakNumber = 0;
            startPacket = false;
            return;
        }
        if (isAir(breakPos)) {
            breakNumber = 0;
        }
        if (breakNumber > maxBreak.getValue() - 1 && maxBreak.getValue() > 0 || !wait.getValue() && isAir(breakPos) && !instant.getValue()) {
            if (breakPos.equals(secondPos)) {
                secondPos = null;
            }
            startPacket = false;
            breakNumber = 0;
            breakPos = null;
            return;
        }
        if (godBlocks.contains(MC.world.getBlockState(breakPos).getBlock())) {
            breakPos = null;
            startPacket = false;
            return;
        }
        if (usingPause.getValue() && MC.player.isUsingItem()) {
            return;
        }
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(breakPos.toCenterPos())) > range.getValue()) {
            if (farCancel.getValue()) {
                startPacket = false;
                breakNumber = 0;
                breakPos = null;
            }
            return;
        }
        //Fish if (breakPos.equals(AutoAnchor.INSTANCE.currentPos)) return;

        if (autoSwap.getValue() == SwapMode.Inventory && MC.currentScreen != null && !(MC.currentScreen instanceof ChatScreen) && !(MC.currentScreen instanceof InventoryScreen) && !(Payload.getInstance().guiManager.isClickGuiOpen())) {
            return;
        }

        int slot = getTool(breakPos);
        if (slot == -1) {
            slot = MC.player.getInventory().selectedSlot;
        }
        if (isAir(breakPos)) {
            if (shouldCrystal()) {
                for (Direction facing : Direction.values()) {
                    CombatUtil.attackCrystal(breakPos.offset(facing), rotate.getValue(), true);
                }
            }
            if (placeTimer.passed(placeDelay.getValue())) {
                if (OtherBlockUtils.canPlace(breakPos) && MC.currentScreen == null) {
                    if (isenderpressed) {
                        int eChest = findBlock(Blocks.ENDER_CHEST);
                        if (eChest != -1) {
                            int oldSlot = MC.player.getInventory().selectedSlot;
                            doSwap(eChest, eChest);
                            OtherBlockUtils.placeBlock(breakPos, rotate.getValue(), true);
                            doSwap(oldSlot, eChest);
                            placeTimer.reset();
                        }
                    } else if (isobbypressed) {

                        int obsidian = findBlock(Blocks.OBSIDIAN);
                        if (obsidian != -1) {

                            boolean hasCrystal = false;
                            if (shouldCrystal()) {
                                for (Entity entity : OtherBlockUtils.getEntities(new Box(breakPos.up()))) {
                                    if (entity instanceof EndCrystalEntity) {
                                        hasCrystal = true;
                                        break;
                                    }
                                }
                            }

                            if (!hasCrystal || spamPlace.getValue()) {
                                int oldSlot = MC.player.getInventory().selectedSlot;
                                doSwap(obsidian, obsidian);
                                OtherBlockUtils.placeBlock(breakPos, rotate.getValue(), true);
                                doSwap(oldSlot, obsidian);
                                placeTimer.reset();
                            }
                        }
                    }
                }
            }
            breakNumber = 0;
        } else if (canPlaceCrystal(breakPos.up(), true)) {
            if (shouldCrystal()) {
                if (placeTimer.passed(placeDelay.getValue())) {
                    if (checkDamage.getValue()) {
                        if (mineTimer.getElapsedTime() / getBreakTime(breakPos, slot) >= crystalDamage.getValue()) {
                            if (!placeCrystal()) return;
                        }
                    } else {
                        if (!placeCrystal()) return;
                    }
                } else if (startPacket) {
                    return;
                }
            }
        }
        if (waitPlace.getValue()) {
            for (Direction i : Direction.values()) {

                if (breakPos.offset(i).equals(Payload.getInstance().moduleManager.crystalaura.crystalPos)) {
                    if (Payload.getInstance().moduleManager.crystalaura.canPlaceCrystal(Payload.getInstance().moduleManager.crystalaura.crystalPos, false, false)) {
                        return;
                    }
                    break;
                }
            }
        }
        if (!delayTimer.passed(delay.getValue())) return;
        if (startPacket) {
            if (isAir(breakPos)) {
                return;
            }
            if (onlyGround.getValue() && !MC.player.isOnGround()) return;
            done = mineTimer.passed((long) getBreakTime(breakPos, slot));
            if (done) {
                if (endRotate.getValue()) {
                    Vec3i vec3i = OtherBlockUtils.getClickSide(breakPos).getVector();
                    if (!faceVector(breakPos.toCenterPos().add(new Vec3d(vec3i.getX() * 0.5, vec3i.getY() * 0.5, vec3i.getZ() * 0.5)))) {
                        return;
                    }
                }
                int old = MC.player.getInventory().selectedSlot;
                boolean shouldSwitch;
                if (autoSwap.getValue() == SwapMode.Silent) {
                    shouldSwitch = slot != old;
                } else {
                    if (slot < 9) {
                        slot = slot + 36;
                    }
                    shouldSwitch = old + 36 != slot;
                }
                if (shouldSwitch) {
                    if (autoSwap.getValue() == SwapMode.Silent) {
                        InventoryUtil.switchToSlot(slot);
                    } else {
                        if (autoSwap.getValue() == SwapMode.Inventory) {
                            InventoryUtil.inventorySwap(slot, old);
                        } else {
                            MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, slot, old, SlotActionType.SWAP, MC.player);
                        }
                    }
                }
                if (endSwing.getValue()) EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, OtherBlockUtils.getClickSide(breakPos), id));
                if (shouldSwitch) {
                    if (autoSwap.getValue() == SwapMode.Silent) {
                        InventoryUtil.switchToSlot(old);
                    } else {
                        if (autoSwap.getValue() == SwapMode.Inventory) {
                            InventoryUtil.inventorySwap(slot, old);
                        } else {
                            MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, slot, old, SlotActionType.SWAP, MC.player);
                        }
                        EntityUtil.syncInventory();
                    }
                }
                breakNumber++;
                delayTimer.reset();
                if (afterBreak.getValue() && shouldCrystal()) {
                    for (Direction facing : Direction.values()) {
                        CombatUtil.attackCrystal(breakPos.offset(facing), rotate.getValue(), true);
                    }
                }
                if (setAir.getValue()) {
                    MC.world.setBlockState(breakPos, Blocks.AIR.getDefaultState());
                }
                skip = true;
            }
        } else {
            if (!mineAir.getValue() && isAir(breakPos)) {
                return;
            }
            Direction side = OtherBlockUtils.getClickSide(breakPos);
            if (rotate.getValue()) {
                Vec3i vec3i = side.getVector();
                if (!faceVector(breakPos.toCenterPos().add(new Vec3d(vec3i.getX() * 0.5, vec3i.getY() * 0.5, vec3i.getZ() * 0.5)))) {
                    return;
                }
            }
            mineTimer.reset();
            done = false;
            if (swing.getValue()) {
                EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
            }
            if (doubleBreak.getValue()) {
                if (secondPos == null || isAir(secondPos)) {
                    double breakTime = (getBreakTime(breakPos, slot, 1));
                    secondTimer.reset();
                    secondPos = breakPos;
                }
                if (grim.getValue()) {
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, breakPos, side, id));
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, side, id));
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, side, id));
                } else {
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, side, id));
                }
            }
            sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
            if (grim.getValue()) {
                if (!doubleBreak.getValue()) {
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, side, id));
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, breakPos, side, id));
                } else {
                    sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, breakPos, side, id));
                }
            }
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (nullCheck() || MC.player.isCreative()) {
            return;
        }
        if (event.GetPacket() instanceof PlayerMoveC2SPacket) {
            if (bypassGround.getValue() && !MC.player.isGliding() && breakPos != null && !isAir(breakPos) && bypassTime.getValue() > 0 && MathHelper.sqrt((float) breakPos.toCenterPos().squaredDistanceTo(EntityUtil.getEyesPos())) <= range.get() + 2) {
                int slot = getTool(breakPos);
                if (slot == -1) {
                    slot = MC.player.getInventory().selectedSlot;
                }
                double breakTime = (getBreakTime(breakPos, slot) - bypassTime.getValue());
                if (breakTime <= 0 || mineTimer.passed((long) breakTime)) {
                    sendGroundPacket = true;
                    ((IPlayerMoveC2SPacket) event.GetPacket()).setOnGround(true);
                }
            } else {
                sendGroundPacket = false;
            }
            return;
        }
        if (event.GetPacket() instanceof UpdateSelectedSlotC2SPacket packet) {
            if (packet.getSelectedSlot() != lastSlot) {
                lastSlot = packet.getSelectedSlot();
                if (switchReset.getValue()) {
                    startPacket = false;
                    mineTimer.reset();
                    done = false;
                }
            }
            return;
        }
        if (!(event.GetPacket() instanceof PlayerActionC2SPacket)) {
            return;
        }
        if (((PlayerActionC2SPacket) event.GetPacket()).getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            if (breakPos == null || !((PlayerActionC2SPacket) event.GetPacket()).getPos().equals(breakPos)) {
                //if (cancelPacket.getValue()) event.cancel();
                return;
            }
            startPacket = true;
        } else if (((PlayerActionC2SPacket) event.GetPacket()).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
            if (breakPos == null || !((PlayerActionC2SPacket) event.GetPacket()).getPos().equals(breakPos)) {
                //if (cancelPacket.getValue()) event.cancel();
                return;
            }
            if (!instant.getValue()) {
                startPacket = false;
            }
        }
    }

    @Override
    public void onBreak(StartBreakingBlockEvent event) {
        if (nullCheck()) {
            return;
        }
        if (MC.player.isCreative()) {
            return;
        }
        event.cancel();

        BlockPos pos = (event.getPos());
        if (pos.equals(breakPos)) {
            return;
        }
        if (godBlocks.contains(MC.world.getBlockState(pos).getBlock())) {
            return;
        }
        if (breakPos != null && preferWeb.getValue() && OtherBlockUtils.getBlock(breakPos) == Blocks.COBWEB) {
            return;
        }
        if (breakPos != null && preferHead.getValue() && EntityUtil.getPlayerPos(true).up().equals(breakPos)) {
            return;
        }
        if (OtherBlockUtils.getClickSideStrict(pos) == null) {
            return;
        }
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > range.getValue()) {
            return;
        }
        breakPos = pos;
        breakNumber = 0;
        startPacket = false;
        mineTimer.reset();
        done = false;
        skip = true;
        Direction side = OtherBlockUtils.getClickSide(breakPos);
        if (rotate.getValue()) {
            Vec3i vec3i = side.getVector();
            if (!faceVector(breakPos.toCenterPos().add(new Vec3d(vec3i.getX() * 0.5, vec3i.getY() * 0.5, vec3i.getZ() * 0.5)))) {
                return;
            }
        }
        mineTimer.reset();
        done = false;
        if (swing.getValue()) {
            EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
        }
        if (doubleBreak.getValue()) {
            if (secondPos == null || isAir(secondPos)) {
                int slot = getTool(breakPos);
                if (slot == -1) {
                    slot = MC.player.getInventory().selectedSlot;
                }
                double breakTime = (getBreakTime(breakPos, slot, 1));
                secondTimer.reset();
                secondPos = breakPos;
            }
            if (grim.getValue()) {
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, breakPos, side, id));
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, side, id));
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, side, id));
            } else {
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, side, id));
            }
        }
        sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
        if (grim.getValue()) {
            if (!doubleBreak.getValue()) {
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, side, id));
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, side, id));
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, breakPos, side, id));
            } else {
                sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, breakPos, side, id));
            }
        }
    }

    @Override
    public void onRender(Render3DEvent event) {
        if (nullCheck()) return;

        if (!MC.player.isCreative()) {

            if (secondPos != null && renderOutside.get()) {

                Box box2 = new Box(secondPos);

                Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), box2, outsideColor.getValue(), lineThickness.getValue());
            }

            if (secondPos != null && renderProgress.get()) {
                if (isAir(secondPos)) {
                    secondPos = null;
                    return;
                }

                Box box2 = new Box(secondPos);

                    int slot = getTool(secondPos);
                    if (slot == -1) {
                        slot = MC.player.getInventory().selectedSlot;
                    }

                    double breakTime = getBreakTime(secondPos, slot);
                    progress = (double) mineTimer.getElapsedTime() / breakTime;

                    Vec3d center = box2.getCenter();
                    float scale = MathHelper.clamp((float) this.progress, 0.0F, 1.0F);

                    double dx = (box2.maxX - box2.minX) / 2.0;
                    double dy = (box2.maxY - box2.minY) / 2.0;
                    double dz = (box2.maxZ - box2.minZ) / 2.0;

                    if (scale > 1.0F) {
                        scale = 1.0F;
                    }

                    Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), new Box(center, center).expand(dx * scale, dy * scale, dz * scale), new Color(this.progress > 0.95F ? 1610678016 : 1627324416), lineThickness.getValue());


                //Render3D.draw3DBox(event.GetMatrix(), new Box(secondPos), new Color(this.progress > 0.95F ? 1610678016 : 1627324416), 0f);
            }

            if (breakPos != null && renderOutside.get()) {

                Box box = new Box(breakPos);
                Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), box, outsideColor.getValue(), lineThickness.getValue());

            }

            if (breakPos != null) {
                        Box box = new Box(breakPos);

                        int slot = getTool(breakPos);

                        if (slot == -1) {
                            slot = MC.player.getInventory().selectedSlot;
                        }

                        double breakTime = getBreakTime(breakPos, slot);
                        progress = (double) mineTimer.getElapsedTime() / breakTime;

                        Vec3d center = box.getCenter();
                        float scale = MathHelper.clamp((float) this.progress, 0.0F, 1.0F);

                        double dx = (box.maxX - box.minX) / 2.0;
                        double dy = (box.maxY - box.minY) / 2.0;
                        double dz = (box.maxZ - box.minZ) / 2.0;

                        if (scale > 1.0F) {
                            scale = 1.0F;
                        }

                        if (renderProgress.get()) {
                            Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), new Box(center, center).expand(dx * scale, dy * scale, dz * scale), new Color(this.progress > 0.95F ? 1610678016 : 1627324416), lineThickness.getValue());
                        }

                        if (renderTextProgress.get()) {
                            if (isAir(breakPos)) {
                                Render3D.drawText3D("Waiting", breakPos.toCenterPos().add(0, 1, 0), -1);
                            } else {
                                if ((int) mineTimer.getElapsedTime() < breakTime) {
                                   Render3D.drawText3D(df.format(progress * 100) + "%", breakPos.toCenterPos().add(0, 1, 0), -1);
                                } else {
                                    Render3D.drawText3D("100.0%", breakPos.toCenterPos().add(0, 1, 0), -1);
                                }
                            }
                        }
            } else {
                progress = 0;
            }
        } else {
            progress = 0;
        }
/*
        if (this.mining != null && this.state != null && !MC.player.isSpectator() && this.mode.getValue() == SpeedmineMode.PACKET) {

            VoxelShape outlineShape = this.state.getOutlineShape(MC.world, this.mining);
            if (!outlineShape.isEmpty()) {
                Box render1 = outlineShape.getBoundingBox();
                Box render = new Box(
                        this.mining.getX() + render1.minX,
                        this.mining.getY() + render1.minY,
                        this.mining.getZ() + render1.minZ,
                        this.mining.getX() + render1.maxX,
                        this.mining.getY() + render1.maxY,
                        this.mining.getZ() + render1.maxZ
                );

                Vec3d center = render.getCenter();
                float scale = MathHelper.clamp((float) this.progress, 0.0F, 1.0F);
                if (scale > 1.0F) {
                    scale = 1.0F;
                }

                double dx = (render1.maxX - render1.minX) / 2.0;
                double dy = (render1.maxY - render1.minY) / 2.0;
                double dz = (render1.maxZ - render1.minZ) / 2.0;


                if (renderouter.getValue()) {
                    Render3D.draw3DBox(event.GetMatrix(), render, new Color(this.damage > 0.95F ? 1610678016 : 1627324416), lineThickness.getValue());
                }

                if (renderinner.getValue()) {
                    Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);

                    Render3D.draw3DBox(event.GetMatrix(), scaled, new Color(this.damage > 0.95F ? 1610678016 : 1627324416), lineThickness.getValue());
                }
            }
        }

 */

    }

    public static boolean canPlaceCrystal(BlockPos pos, boolean ignoreItem) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return (OtherBlockUtils.getBlock(obsPos) == Blocks.BEDROCK || OtherBlockUtils.getBlock(obsPos) == Blocks.OBSIDIAN)
                && OtherBlockUtils.getClickSideStrict(obsPos) != null
                && noEntity(boost, ignoreItem)
                && noEntity(boost.up(), ignoreItem)
                && (!AntiCheat.INSTANCE.lowVersion.getValue() || MC.world.isAir(boost.up()));
    }

    public static boolean noEntity(BlockPos pos, boolean ignoreItem) {
        for (Entity entity : OtherBlockUtils.getEntities(new Box(pos))) {
            if (entity instanceof ItemEntity && ignoreItem || entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.obsMode.getValue()) continue;
            return false;
        }
        return true;
    }
    public void mine(BlockPos pos) {
        if (nullCheck()) {
            return;
        }
        if (MC.player.isCreative()) {
            MC.interactionManager.attackBlock(pos, OtherBlockUtils.getClickSide(pos));
            return;
        }
        if (!this.state.getValue()) {
            MC.interactionManager.attackBlock(pos, OtherBlockUtils.getClickSide(pos));
            return;
        }
        if (pos.equals(breakPos)) {
            return;
        }
        if (godBlocks.contains(MC.world.getBlockState(pos).getBlock())) {
            return;
        }
        if (breakPos != null && preferWeb.getValue() && OtherBlockUtils.getBlock(breakPos) == Blocks.COBWEB) {
            return;
        }
        if (breakPos != null && preferHead.getValue() && EntityUtil.getPlayerPos(true).up().equals(breakPos)) {
            return;
        }
        if (OtherBlockUtils.getClickSideStrict(pos) == null) {
            return;
        }
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > range.getValue()) {
            return;
        }
        breakPos = pos;
        breakNumber = 0;
        startPacket = false;
        mineTimer.reset();
        done = false;
        skip = true;
    }
    private boolean shouldCrystal() {
        return crystal.getValue() && (!onlyHeadBomber.getValue() || isobbypressed); // || AutoCev.INSTANCE.isOn();
    }

    private boolean faceVector(Vec3d directionVec) {
        if (!yawStep.getValue()) {
            Payload.getInstance().rotationManager.lookAt(directionVec);
            return true;
        } else {
            this.sync.reset();
            this.directionVec = directionVec;
            if (Payload.getInstance().rotationManager.inFov(directionVec, fov.get())) {
                return true;
            }
        }
        return !checkFov.getValue();
    }

    private int getTool(BlockPos pos) {
        if (autoSwap.getValue() == SwapMode.Silent) {
            int index = -1;
            float CurrentFastest = 1.0f;
            for (int i = 0; i < 9; ++i) {
                final ItemStack stack = MC.player.getInventory().getStack(i);
                if (stack != ItemStack.EMPTY) {
                    final float digSpeed = EnchantmentHelper.getLevel(MC.world.getRegistryManager().getOrThrow(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY.getValue()).orElse(null), stack);
                    final float destroySpeed = stack.getMiningSpeedMultiplier(MC.world.getBlockState(pos));
                    if (digSpeed + destroySpeed > CurrentFastest) {
                        CurrentFastest = digSpeed + destroySpeed;
                        index = i;
                    }
                }
            }
            return index;
        } else {
            AtomicInteger slot = new AtomicInteger();
            slot.set(-1);
            float CurrentFastest = 1.0f;
            for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
                if (!(entry.getValue().getItem() instanceof AirBlockItem)) {
                    final float digSpeed = EnchantmentHelper.getLevel(MC.world.getRegistryManager().getOrThrow(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY.getValue()).orElse(null), entry.getValue());
                    final float destroySpeed = entry.getValue().getMiningSpeedMultiplier(MC.world.getBlockState(pos));
                    if (digSpeed + destroySpeed > CurrentFastest) {
                        CurrentFastest = digSpeed + destroySpeed;
                        slot.set(entry.getKey());
                    }
                }
            }
            return slot.get();
        }
    }

    public final double getBreakTime(BlockPos pos, int slot) {
        return getBreakTime(pos, slot, damage.getValue());
    }

    public final double getBreakTime(BlockPos pos, int slot, double damage) {
        if (slot == -1) {
            slot = MC.player.getInventory().selectedSlot;

            if (slot == -1) {
                slot = 1;
            }
        }
        return (1 / getBlockStrength(pos, MC.player.getInventory().getStack(slot)) / 20 * 1000 * damage);
    }

    public float getBlockStrength(BlockPos position, ItemStack itemStack) {
        BlockState state = MC.world.getBlockState(position);
        float hardness = state.getHardness(MC.world, position);
        if (hardness < 0) {
            return 0;
        }
        return getDigSpeed(state, itemStack) / hardness / 30F;
    }

    public float getDigSpeed(BlockState state, ItemStack itemStack) {
        float digSpeed = getDestroySpeed(state, itemStack);
        if (digSpeed > 1) {
            int efficiencyModifier = EnchantmentHelper.getLevel(MC.world.getRegistryManager().getOrThrow(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY.getValue()).orElse(null), itemStack);
            if (efficiencyModifier > 0 && !itemStack.isEmpty()) {
                digSpeed += (float) (StrictMath.pow(efficiencyModifier, 2) + 1);
            }
        }
        if (MC.player.hasStatusEffect(StatusEffects.HASTE)) {
            digSpeed *= 1 + (MC.player.getStatusEffect(StatusEffects.HASTE).getAmplifier() + 1) * 0.2F;
        }
        if (MC.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float fatigueScale;
            switch (MC.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> fatigueScale = 0.3F;
                case 1 -> fatigueScale = 0.09F;
                case 2 -> fatigueScale = 0.0027F;
                default -> fatigueScale = 8.1E-4F;
            }
            digSpeed *= fatigueScale;
        }
        if (MC.player.isSubmergedInWater()) { //Fixme && !EnchantmentHelper.hasAquaAffinity(MC.player)) {
            digSpeed /= 5;
        }
        boolean inWeb = Payload.getInstance().playerManager.isInWeb(MC.player) && MC.world.getBlockState(breakPos).getBlock() == Blocks.COBWEB;
        if ((!MC.player.isOnGround() || inWeb) && INSTANCE.checkGround.getValue() && (!smart.getValue() || MC.player.isGliding() || inWeb)) {
            digSpeed /= 5;
        }
        return (digSpeed < 0 ? 0 : digSpeed);
    }

    public float getDestroySpeed(BlockState state, ItemStack itemStack) {
        float destroySpeed = 1;
        if (itemStack != null && !itemStack.isEmpty()) {
            destroySpeed *= itemStack.getMiningSpeedMultiplier(state);
        }
        return destroySpeed;
    }

    private boolean isAir(BlockPos breakPos) {
        return MC.world.isAir(breakPos) || OtherBlockUtils.getBlock(breakPos) == Blocks.FIRE && OtherBlockUtils.hasCrystal(breakPos);
    }
}