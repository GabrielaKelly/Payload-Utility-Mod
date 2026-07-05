package net.payload.module.modules.client;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.Placement;
import net.payload.utils.player.combat.SwingSide;

public class AntiCheat extends Module {

    // Static instance
    public static AntiCheat INSTANCE;

    public final EnumSetting<SwingSide> swingMode = EnumSetting.<SwingSide>builder()
            .id("anticheat_swingmode")
            .displayName("SwingMode")
            .defaultValue(SwingSide.All)
            .build();

    public final EnumSetting<Placement> placement = EnumSetting.<Placement>builder()
            .id("anticheat_placement")
            .displayName("PlaceMode")
            .defaultValue(Placement.Vanilla)
            .build();

    // General Page Settings
    public final BooleanSetting blockCheck = BooleanSetting.builder()
            .id("anticheat_blockcheck")
            .displayName("BlockCheck")
            .defaultValue(true)
            .build();

    public final BooleanSetting lowVersion = BooleanSetting.builder()
            .id("anticheat_1.12")
            .displayName("1.12")
            .defaultValue(false)
            .build();

    public final BooleanSetting multiPlace = BooleanSetting.builder()
            .id("anticheat_multiplace")
            .displayName("MultiPlace")
            .defaultValue(false)
            .build();

    public final BooleanSetting packetPlace = BooleanSetting.builder()
            .id("anticheat_packetplace")
            .displayName("PacketPlace")
            .defaultValue(false)
            .build();

    public final BooleanSetting attackRotate = BooleanSetting.builder()
            .id("anticheat_attack_rotate")
            .displayName("AttackRotation")
            .defaultValue(false)
            .build();

    public final BooleanSetting invSwapBypass = BooleanSetting.builder()
            .id("anticheat_pickswap")
            .displayName("PickSwap")
            .defaultValue(false)
            .build();

    public final FloatSetting boxSize = FloatSetting.builder()
            .id("anticheat_hitbox_size")
            .displayName("HitBoxSize")
            .defaultValue(0.6f)
            .minValue(0f)
            .maxValue(1f)
            .step(0.01f)
            .build();

    public final FloatSetting attackDelay = FloatSetting.builder()
            .id("anticheat_break_delay")
            .displayName("BreakDelay")
            .description("Break delay in seconds")
            .defaultValue(0.2f)
            .minValue(0f)
            .maxValue(1f)
            .step(0.01f)
            .build();

    // Rotation Page Settings

    public final BooleanSetting grimRotation = BooleanSetting.builder()
            .id("anticheat_grim_rotation")
            .displayName("GrimRotation")
            .defaultValue(false)
            .build();

    public final BooleanSetting noSpamRotation = BooleanSetting.builder()
            .id("anticheat_noSpamRotation")
            .displayName("noSpamRotation")
            .defaultValue(true)
            .build();

    public final BooleanSetting snapBack = BooleanSetting.builder()
            .id("anticheat_snapback")
            .displayName("SnapBack")
            .defaultValue(false)
            .build();

    public final BooleanSetting look = BooleanSetting.builder()
            .id("anticheat_look")
            .displayName("Look")
            .defaultValue(true)
            .build();

    public final FloatSetting fov = FloatSetting.builder()
            .id("anticheat_fov")
            .displayName("Look FOV")
            .defaultValue(10f)
            .minValue(0f)
            .maxValue(180f)
            .step(1f)
            .build();

    public final FloatSetting rotateTime = FloatSetting.builder()
            .id("anticheat_rotate_time")
            .displayName("RotateTime")
            .defaultValue(0.5f)
            .minValue(0f)
            .maxValue(1f)
            .step(0.01f)
            .build();

    public final BooleanSetting forceSync = BooleanSetting.builder()
            .id("anticheat_forceSync")
            .displayName("ServerSideSync")
            .defaultValue(false)
            .build();

    // Misc Page Settings
    public final BooleanSetting obsMode = BooleanSetting.builder()
            .id("anticheat_obs_server")
            .displayName("OBSServer")
            .defaultValue(false)
            .build();

    public final BooleanSetting inventorySync = BooleanSetting.builder()
            .id("anticheat_inventory_sync")
            .displayName("InventorySync")
            .defaultValue(false)
            .build();

    public AntiCheat() {
        super("AntiCheat");
        this.setCategory(Category.of("Client"));
        this.setDescription("Anti-Cheat configuration and bypass settings");
        INSTANCE = this;

        // Add settings
        this.addSetting(swingMode);

        // General settings
        this.addSetting(multiPlace);
        this.addSetting(packetPlace);
        this.addSetting(attackRotate);
        this.addSetting(invSwapBypass);
        this.addSetting(boxSize);
        this.addSetting(attackDelay);
        this.addSetting(lowVersion);

        // Rotation settings
        this.addSetting(grimRotation);
        this.addSetting(snapBack);
        this.addSetting(noSpamRotation);
        this.addSetting(rotateTime);
        this.addSetting(forceSync);
        this.addSetting(look);

        // Misc settings
        this.addSetting(obsMode);
        this.addSetting(inventorySync);
    }

    public static double getOffset() {
        if (INSTANCE != null) {
            return INSTANCE.boxSize.getValue() / 2;
        }
        return 0.3;
    }

    @Override
    public void onDisable() {
        // Implementation
        keepEnabled();
    }

    @Override
    public void onEnable() {
        // Implementation
    }

    @Override
    public void onToggle() {
        // Implementation
    }
}