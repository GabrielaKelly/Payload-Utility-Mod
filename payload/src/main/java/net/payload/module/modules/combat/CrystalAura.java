package net.payload.module.modules.combat;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.gui.colors.Color;
import net.payload.mixin.interfaces.IEntity;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.client.AntiCheat;
import net.payload.module.modules.misc.PacketMine;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.Interpolation;
import net.payload.utils.block.BlockPosX;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.math.ExplosionUtil;
import net.payload.utils.player.combat.CombatUtil;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.player.combat.SwingSide;
import net.payload.utils.render.Render3D;

import java.util.ArrayList;
import java.util.UUID;

import static net.payload.utils.block.OtherBlockUtils.getBlock;
import static net.payload.utils.block.OtherBlockUtils.hasCrystal;

public class CrystalAura extends Module implements TickListener, Render3DListener, LookAtListener, SendPacketListener, SendMovementPacketListener {

    private enum SwapMode {
        Off, Normal, Silent, Inventory
    }

    // Setting Group for General settings
    private final SettingGroup generalSettings = SettingGroup.Builder.builder()
            .id("autocrystal_general")
            .displayName("General")
            .description("General AutoCrystal settings")
            .build();

    // General Settings

    //Possibly future anchor aura.
    private final BooleanSetting preferAnchor = BooleanSetting.builder()
            .id("autocrystal_prefer_anchor")
            .displayName("Prefer Anchor")
            .description("Prioritizes end anchors over end crystals when available")
            .defaultValue(false)
            .build();

    private final BooleanSetting breakOnlyHasCrystal = BooleanSetting.builder()
            .id("autocrystal_only_hold")
            .displayName("Only Hold")
            .description("Only breaks crystals when holding a crystal or anchor")
            .defaultValue(true)
            .build();

    private final EnumSetting<SwingSide> swingMode = EnumSetting.<SwingSide>builder()
            .id("autocrystal_swing")
            .displayName("Swing")
            .description("Determines which hand to swing when interacting with crystals")
            .defaultValue(SwingSide.All)
            .build();

    private final BooleanSetting eatingPause = BooleanSetting.builder()
            .id("autocrystal_eating_pause")
            .displayName("Eating Pause")
            .description("Pauses crystal operations while eating")
            .defaultValue(true)
            .build();

    private final FloatSetting switchCooldown = FloatSetting.builder()
            .id("autocrystal_switch_pause")
            .displayName("Switch Pause")
            .description("Cooldown after switching items in milliseconds")
            .defaultValue(100f)
            .minValue(0f)
            .maxValue(1000f)
            .step(10f)
            .build();

    private final FloatSetting targetRange = FloatSetting.builder()
            .id("autocrystal_target_range")
            .displayName("Target Range")
            .description("Maximum distance to target players in meters")
            .defaultValue(12.0f)
            .minValue(0.0f)
            .maxValue(20.0f)
            .step(0.5f)
            .build();

    private final FloatSetting updateDelay = FloatSetting.builder()
            .id("autocrystal_update_delay")
            .displayName("Update Delay")
            .description("Delay between calculations in milliseconds")
            .defaultValue(50f)
            .minValue(0f)
            .maxValue(1000f)
            .step(10f)
            .build();

    private final FloatSetting wallRange = FloatSetting.builder()
            .id("autocrystal_wall_range")
            .displayName("Wall Range")
            .description("Maximum distance to target players through walls in meters")
            .defaultValue(6.0f)
            .minValue(0.0f)
            .maxValue(6.0f)
            .step(0.1f)
            .build();

    // Setting Group for Rotation settings
    private final SettingGroup rotationSettings = SettingGroup.Builder.builder()
            .id("autocrystal_rotation")
            .displayName("Rotation")
            .description("Settings for player rotation when using crystals")
            .build();

    // Rotation Settings
    private final BooleanSetting rotate = BooleanSetting.builder()
            .id("autocrystal_rotate")
            .displayName("Rotate")
            .description("Rotates player view when interacting with crystals")
            .defaultValue(true)
            .build();

    private final BooleanSetting onBreak = BooleanSetting.builder()
            .id("autocrystal_on_break")
            .displayName("On Break")
            .description("Only rotate when breaking crystals")
            .defaultValue(false)

            .build();

    private final FloatSetting yOffset = FloatSetting.builder()
            .id("autocrystal_y_offset")
            .displayName("Y Offset")
            .description("Vertical offset for rotation calculations")
            .defaultValue(0.05f)
            .minValue(0f)
            .maxValue(1f)
            .step(0.01f)

            .build();

    private final BooleanSetting yawStep = BooleanSetting.builder()
            .id("autocrystal_yaw_step")
            .displayName("Yaw Step")
            .description("Gradually rotate instead of snapping to position")
            .defaultValue(false)
            .build();

    private final FloatSetting steps = FloatSetting.builder()
            .id("autocrystal_steps")
            .displayName("Steps")
            .description("Step size for gradual rotation")
            .defaultValue(0.8f)
            .minValue(0f)
            .maxValue(1f)
            .step(0.1f)

            .build();

    private final BooleanSetting checkFov = BooleanSetting.builder()
            .id("autocrystal_only_looking")
            .displayName("Only Looking")
            .description("Only rotate to crystals within field of view")
            .defaultValue(false)

            .build();

    private final FloatSetting fov = FloatSetting.builder()
            .id("autocrystal_fov")
            .displayName("FOV")
            .description("Field of view angle in degrees")
            .defaultValue(30f)
            .minValue(0f)
            .maxValue(50f)
            .step(1f)

            .build();

    private final FloatSetting priority = FloatSetting.builder()
            .id("autocrystal_priority")
            .displayName("Priority")
            .description("Rotation priority level")
            .defaultValue(10f)
            .minValue(0f)
            .maxValue(100f)
            .step(1f)

            .build();

    // Setting Group for Interaction settings
    private final SettingGroup interactionSettings = SettingGroup.Builder.builder()
            .id("autocrystal_interaction")
            .displayName("Interaction")
            .description("Settings for crystal placement and breaking")
            .build();

    // Damage Settings
    private final FloatSetting autoMinDamage = FloatSetting.builder()
            .id("autocrystal_piston_min")
            .displayName("Piston Min")
            .description("Minimum damage required for auto piston placement in damage points")
            .defaultValue(5.0f)
            .minValue(0.0f)
            .maxValue(36.0f)
            .step(0.5f)
            .build();

    private final FloatSetting minDamage = FloatSetting.builder()
            .id("autocrystal_min")
            .displayName("Min")
            .description("Minimum damage required for crystal placement in damage points")
            .defaultValue(5.0f)
            .minValue(0.0f)
            .maxValue(36.0f)
            .step(0.5f)
            .build();

    private final FloatSetting maxSelf = FloatSetting.builder()
            .id("autocrystal_self")
            .displayName("Self")
            .description("Maximum self-damage allowed in damage points")
            .defaultValue(12.0f)
            .minValue(0.0f)
            .maxValue(36.0f)
            .step(0.5f)
            .build();

    private final FloatSetting range = FloatSetting.builder()
            .id("autocrystal_range")
            .displayName("Range")
            .description("Maximum placement range in meters")
            .defaultValue(5.0f)
            .minValue(0.0f)
            .maxValue(6.0f)
            .step(0.1f)
            .build();

    private final FloatSetting noSuicide = FloatSetting.builder()
            .id("autocrystal_no_suicide")
            .displayName("No Suicide")
            .description("Prevents actions that would reduce health below this threshold in health points")
            .defaultValue(3.0f)
            .minValue(0.0f)
            .maxValue(10.0f)
            .step(0.5f)
            .build();

    private final BooleanSetting smart = BooleanSetting.builder()
            .id("autocrystal_smart")
            .displayName("Smart")
            .description("Uses intelligent algorithms to optimize crystal placement")
            .defaultValue(true)
            .build();

    // Place Settings
    private final BooleanSetting place = BooleanSetting.builder()
            .id("autocrystal_place")
            .displayName("Place")
            .description("Enables crystal placement")
            .defaultValue(true)
            .build();

    private final FloatSetting placeDelay = FloatSetting.builder()
            .id("autocrystal_place_delay")
            .displayName("Place Delay")
            .description("Delay between crystal placements in milliseconds")
            .defaultValue(150f)
            .minValue(0f)
            .maxValue(1000f)
            .step(10f)

            .build();

    private final EnumSetting<SwapMode> autoSwap = EnumSetting.<SwapMode>builder()
            .id("autocrystal_auto_swap")
            .displayName("Auto Swap")
            .description("Automatically switches to crystals when needed")
            .defaultValue(SwapMode.Normal)
            .build();

    private final BooleanSetting afterBreak = BooleanSetting.builder()
            .id("autocrystal_after_break")
            .displayName("After Break")
            .description("Places crystals immediately after breaking")
            .defaultValue(true)

            .build();

    // Break Settings
    private final BooleanSetting breakSetting = BooleanSetting.builder()
            .id("autocrystal_break")
            .displayName("Break")
            .description("Enables crystal breaking")
            .defaultValue(true)
            .build();

    private final FloatSetting breakDelay = FloatSetting.builder()
            .id("autocrystal_break_delay")
            .displayName("Break Delay")
            .description("Delay between crystal breaks in milliseconds")
            .defaultValue(150f)
            .minValue(0f)
            .maxValue(1000f)
            .step(10f)

            .build();

    private final FloatSetting minAge = FloatSetting.builder()
            .id("autocrystal_min_age")
            .displayName("Min Age")
            .description("Minimum age of crystals before breaking in ticks")
            .defaultValue(0f)
            .minValue(0f)
            .maxValue(20f)
            .step(1f)

            .build();

    private final BooleanSetting breakRemove = BooleanSetting.builder()
            .id("autocrystal_remove")
            .displayName("Memory Remove")
            .description("Removes crystals from memory after breaking")
            .defaultValue(false)

            .build();

    private final BooleanSetting onlyTick = BooleanSetting.builder()
            .id("autocrystal_only_tick")
            .displayName("Only Tick")
            .description("Only performs operations on game ticks")
            .defaultValue(false)
            .build();

    // Setting Group for Calculation settings
    private final SettingGroup calculationSettings = SettingGroup.Builder.builder()
            .id("autocrystal_calculation")
            .displayName("Calculation")
            .description("Settings for damage calculation algorithms and performance")
            .build();

    // Calculation Settings
    private final BooleanSetting thread = BooleanSetting.builder()
            .id("autocrystal_thread")
            .displayName("Thread")
            .description("Performs calculations in a separate thread to improve performance")
            .defaultValue(false)
            .build();

    private final BooleanSetting doCrystal = BooleanSetting.builder()
            .id("autocrystal_thread_interact")
            .displayName("Thread Interact")
            .description("Performs crystal interactions in the calculation thread")
            .defaultValue(false)
            .build();

    private final BooleanSetting lite = BooleanSetting.builder()
            .id("autocrystal_less_cpu")
            .displayName("Less CPU")
            .description("Reduces CPU usage by optimizing calculations at the cost of some precision")
            .defaultValue(false)
            .build();

    private final FloatSetting predictTicks = FloatSetting.builder()
            .id("autocrystal_predict")
            .displayName("Predict")
            .description("Number of ticks to predict player movement for calculations")
            .defaultValue(4f)
            .minValue(0f)
            .maxValue(10f)
            .step(1f)
            .build();

    private final BooleanSetting terrainIgnore = BooleanSetting.builder()
            .id("autocrystal_terrain_ignore")
            .displayName("Terrain Ignore")
            .description("Ignores terrain when calculating crystal damage")
            .defaultValue(true)
            .build();

    // Setting Group for Miscellaneous settings
    private final SettingGroup miscSettings = SettingGroup.Builder.builder()
            .id("autocrystal_misc")
            .displayName("Miscellaneous")
            .description("Additional specialized settings for crystal combat")
            .build();

    // Mining Settings
    private final BooleanSetting ignoreMine = BooleanSetting.builder()
            .id("autocrystal_ignore_mine")
            .displayName("Ignore Mine")
            .description("Ignores blocks being mined when placing crystals")
            .defaultValue(true)
            .build();

    private final FloatSetting constantProgress = FloatSetting.builder()
            .id("autocrystal_progress")
            .displayName("Progress")
            .description("Minimum mining progress percentage to consider a block as being mined")
            .defaultValue(90.0f)
            .minValue(0.0f)
            .maxValue(100.0f)
            .step(1.0f)

            .build();

    // Anti-Surround Settings
    private final BooleanSetting antiSurround = BooleanSetting.builder()
            .id("autocrystal_anti_surround")
            .displayName("Anti Surround")
            .description("Attempts to break enemy surrounds with crystal placements")
            .defaultValue(false)
            .build();

    private final FloatSetting antiSurroundMax = FloatSetting.builder()
            .id("autocrystal_when_lower")
            .displayName("When Lower")
            .description("Only attempts anti-surround when potential damage is below this threshold in damage points")
            .defaultValue(5.0f)
            .minValue(0.0f)
            .maxValue(36.0f)
            .step(0.5f)

            .build();

    // Timeout Settings
    private final BooleanSetting slowPlace = BooleanSetting.builder()
            .id("autocrystal_timeout")
            .displayName("Timeout")
            .description("Implements a timeout for crystal placements to prevent wasteful spam")
            .defaultValue(true)
            .build();

    private final FloatSetting slowDelay = FloatSetting.builder()
            .id("autocrystal_timeout_delay")
            .displayName("Timeout Delay")
            .description("Duration of the timeout period in milliseconds")
            .defaultValue(200f)
            .minValue(0f)
            .maxValue(2000f)
            .step(50f)

            .build();

    private final FloatSetting slowMinDamage = FloatSetting.builder()
            .id("autocrystal_timeout_min")
            .displayName("Timeout Override HP")
            .description("Minimum damage required to bypass timeout in damage points")
            .defaultValue(1.5f)
            .minValue(0.0f)
            .maxValue(36.0f)
            .step(0.5f)

            .build();

    // Force Place Settings
    private final BooleanSetting forcePlace = BooleanSetting.builder()
            .id("autocrystal_force_place")
            .displayName("Force Place")
            .description("Forces crystal placement when target is vulnerable")
            .defaultValue(true)
            .build();

    private final FloatSetting forceMaxHealth = FloatSetting.builder()
            .id("autocrystal_lower_than")
            .displayName("Lower Than")
            .description("Forces placement when target health is below this threshold in health points")
            .defaultValue(7f)
            .minValue(0f)
            .maxValue(36f)
            .step(1f)

            .build();

    private final FloatSetting forceMin = FloatSetting.builder()
            .id("autocrystal_force_min")
            .displayName("Force Min HP")
            .description("Minimum damage required for forced placement in damage points")
            .defaultValue(1.5f)
            .minValue(0.0f)
            .maxValue(36.0f)
            .step(0.5f)

            .build();

    // Armor Breaker Settings
    private final BooleanSetting armorBreaker = BooleanSetting.builder()
            .id("autocrystal_armor_breaker")
            .displayName("Armor Breaker")
            .description("Targets enemies with low durability armor")
            .defaultValue(true)
            .build();

    private final FloatSetting maxDurable = FloatSetting.builder()
            .id("autocrystal_max_durable")
            .displayName("Max Durable")
            .description("Maximum armor durability percentage to target with armor breaker")
            .defaultValue(8f)
            .minValue(0f)
            .maxValue(100f)
            .step(1f)

            .build();

    private final FloatSetting armorBreakerDamage = FloatSetting.builder()
            .id("autocrystal_breaker_min")
            .displayName("Breaker Min")
            .description("Minimum damage required for armor breaking in damage points")
            .defaultValue(3.0f)
            .minValue(0.0f)
            .maxValue(36.0f)
            .step(0.5f)

            .build();

    // Timing Settings
    private final FloatSetting hurtTime = FloatSetting.builder()
            .id("autocrystal_hurt_time")
            .displayName("Hurt Time")
            .description("Maximum hurt time to consider when attacking entities in ticks")
            .defaultValue(10f)
            .minValue(0f)
            .maxValue(10f)
            .step(1f)
            .build();

    private final FloatSetting waitHurt = FloatSetting.builder()
            .id("autocrystal_wait_hurt")
            .displayName("Wait Hurt")
            .description("Time to wait after entity is hurt in ticks")
            .defaultValue(10f)
            .minValue(0f)
            .maxValue(10f)
            .step(1f)
            .build();

    private final FloatSetting syncTimeout = FloatSetting.builder()
            .id("autocrystal_wait_timeout")
            .displayName("Wait Timeout")
            .description("Maximum time to wait for server synchronization in milliseconds")
            .defaultValue(500f)
            .minValue(0f)
            .maxValue(2000f)
            .step(10f)
            .build();

    // Web Force Settings
    private final BooleanSetting forceWeb = BooleanSetting.builder()
            .id("autocrystal_force_web")
            .displayName("Force Web")
            .description("Forces crystal placement when target is in a web")
            .defaultValue(false)
            .build();

    public final BooleanSetting airPlace = BooleanSetting.builder()
            .id("autocrystal_air_place")
            .displayName("Web Air Place")
            .description("Allows placing crystals in air when target is in a web")
            .defaultValue(false)

            .build();

    public final BooleanSetting replace = BooleanSetting.builder()
            .id("autocrystal_replace")
            .displayName("Web Replace")
            .description("Replaces existing blocks with crystals when target is in a web")
            .defaultValue(false)
            .build();

    private final SettingGroup renderSettings = SettingGroup.Builder.builder()
            .id("autocrystal_render")
            .displayName("Render")
            .description("Rendering AutoCrystal settings")
            .build();

    private final BooleanSetting targetRender = BooleanSetting.builder()
            .id("autocrystal_targetrender")
            .displayName("TargetESP")
            .description("Renders target positions")
            .defaultValue(true)
            .build();

    private final ColorSetting targetColor = ColorSetting.builder()
            .id("autocrystal_targetcolor")
            .displayName("Target Color")
            .description("Color of the render")
            .defaultValue(new Color(0, 255, 0, 75))
            .build();

    private final BooleanSetting posRender = BooleanSetting.builder()
            .id("autocrystal_targetrender")
            .displayName("PlaceESP")
            .description("Renders target positions")
            .defaultValue(true)
            .build();

    private final ColorSetting posColor = ColorSetting.builder()
            .id("autocrystal_targetcolor")
            .displayName("Placement Color")
            .description("Color of the render")
            .defaultValue(new Color(255, 0, 0, 75))
            .build();

    private final FloatSetting lineWidth = FloatSetting.builder()
            .id("autocrystal_line_width")
            .displayName("Line Width")
            .description("Width of outline lines")
            .defaultValue(1.5f)
            .minValue(0.1f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    public CrystalAura() {
    	super("CrystalAura");

        this.setCategory(Category.of("Combat"));
        this.setDescription("Destroys and places endcrystals to kill opponents");

        //generalSettings.addSetting(preferAnchor);
        generalSettings.addSetting(breakOnlyHasCrystal);
        generalSettings.addSetting(swingMode);
        generalSettings.addSetting(eatingPause);
        generalSettings.addSetting(switchCooldown);
        generalSettings.addSetting(targetRange);
        generalSettings.addSetting(updateDelay);
        generalSettings.addSetting(wallRange);

        // Add group to module
        this.addSetting(generalSettings);

        // Register settings with SettingManager
        SettingManager.registerSetting(generalSettings);
      //  SettingManager.registerSetting(preferAnchor);
        SettingManager.registerSetting(breakOnlyHasCrystal);
        SettingManager.registerSetting(swingMode);
        SettingManager.registerSetting(eatingPause);
        SettingManager.registerSetting(switchCooldown);
        SettingManager.registerSetting(targetRange);
        SettingManager.registerSetting(updateDelay);
        SettingManager.registerSetting(wallRange);

        rotationSettings.addSetting(rotate);
        rotationSettings.addSetting(onBreak);
        rotationSettings.addSetting(yOffset);
        rotationSettings.addSetting(yawStep);
        rotationSettings.addSetting(steps);
        rotationSettings.addSetting(checkFov);
        rotationSettings.addSetting(fov);
        rotationSettings.addSetting(priority);

        // Add group to module
        this.addSetting(rotationSettings);

        // Register settings with SettingManager
        SettingManager.registerSetting(rotationSettings);
        SettingManager.registerSetting(rotate);
        SettingManager.registerSetting(onBreak);
        SettingManager.registerSetting(yOffset);
        SettingManager.registerSetting(yawStep);
        SettingManager.registerSetting(steps);
        SettingManager.registerSetting(checkFov);
        SettingManager.registerSetting(fov);
        SettingManager.registerSetting(priority);

        // Add settings to interaction group
        //interactionSettings.addSetting(autoMinDamage);
        interactionSettings.addSetting(minDamage);
        interactionSettings.addSetting(maxSelf);
        interactionSettings.addSetting(range);
        interactionSettings.addSetting(noSuicide);
        interactionSettings.addSetting(smart);
        interactionSettings.addSetting(place);
        interactionSettings.addSetting(placeDelay);
        interactionSettings.addSetting(autoSwap);
        interactionSettings.addSetting(afterBreak);
        interactionSettings.addSetting(breakSetting);
        interactionSettings.addSetting(breakDelay);
        interactionSettings.addSetting(minAge);
        interactionSettings.addSetting(breakRemove);
        interactionSettings.addSetting(onlyTick);

        // Add group to module
        this.addSetting(interactionSettings);

        // Register settings with SettingManager
        SettingManager.registerSetting(interactionSettings);
       // SettingManager.registerSetting(autoMinDamage);
        SettingManager.registerSetting(minDamage);
        SettingManager.registerSetting(maxSelf);
        SettingManager.registerSetting(range);
        SettingManager.registerSetting(noSuicide);
        SettingManager.registerSetting(smart);
        SettingManager.registerSetting(place);
        SettingManager.registerSetting(placeDelay);
        SettingManager.registerSetting(autoSwap);
        SettingManager.registerSetting(afterBreak);
        SettingManager.registerSetting(breakSetting);
        SettingManager.registerSetting(breakDelay);
        SettingManager.registerSetting(minAge);
        SettingManager.registerSetting(breakRemove);
        SettingManager.registerSetting(onlyTick);

        // Add settings to calculation group
       // calculationSettings.addSetting(thread);
        calculationSettings.addSetting(doCrystal);
        calculationSettings.addSetting(lite);
        calculationSettings.addSetting(predictTicks);
        calculationSettings.addSetting(terrainIgnore);

        // Add group to module
        this.addSetting(calculationSettings);

        // Register settings with SettingManager
        SettingManager.registerSetting(calculationSettings);
     //   SettingManager.registerSetting(thread);
        SettingManager.registerSetting(doCrystal);
        SettingManager.registerSetting(lite);
        SettingManager.registerSetting(predictTicks);
        SettingManager.registerSetting(terrainIgnore);

        // Add settings to miscellaneous group
        miscSettings.addSetting(ignoreMine);
        miscSettings.addSetting(constantProgress);
        miscSettings.addSetting(antiSurround);
        miscSettings.addSetting(antiSurroundMax);
        miscSettings.addSetting(slowPlace);
        miscSettings.addSetting(slowDelay);
        miscSettings.addSetting(slowMinDamage);
        miscSettings.addSetting(forcePlace);
        miscSettings.addSetting(forceMaxHealth);
        miscSettings.addSetting(forceMin);
        miscSettings.addSetting(armorBreaker);
        miscSettings.addSetting(maxDurable);
        miscSettings.addSetting(armorBreakerDamage);
        miscSettings.addSetting(hurtTime);
        miscSettings.addSetting(waitHurt);
        miscSettings.addSetting(syncTimeout);
       // miscSettings.addSetting(forceWeb);
       // miscSettings.addSetting(airPlace);
       // miscSettings.addSetting(replace);

        // Add group to module
        this.addSetting(miscSettings);

        SettingManager.registerSetting(targetRender);
        SettingManager.registerSetting(targetColor);
        SettingManager.registerSetting(posRender);
        SettingManager.registerSetting(posColor);
        SettingManager.registerSetting(lineWidth);

        renderSettings.addSetting(targetRender);
        renderSettings.addSetting(targetColor);
        renderSettings.addSetting(posRender);
        renderSettings.addSetting(posColor);
        renderSettings.addSetting(lineWidth);

        this.addSetting(renderSettings);

        // Register settings with SettingManager
        SettingManager.registerSetting(miscSettings);
        SettingManager.registerSetting(ignoreMine);
        SettingManager.registerSetting(constantProgress);
        SettingManager.registerSetting(antiSurround);
        SettingManager.registerSetting(antiSurroundMax);
        SettingManager.registerSetting(slowPlace);
        SettingManager.registerSetting(slowDelay);
        SettingManager.registerSetting(slowMinDamage);
        SettingManager.registerSetting(forcePlace);
        SettingManager.registerSetting(forceMaxHealth);
        SettingManager.registerSetting(forceMin);
        SettingManager.registerSetting(armorBreaker);
        SettingManager.registerSetting(maxDurable);
        SettingManager.registerSetting(armorBreakerDamage);
        SettingManager.registerSetting(hurtTime);
        SettingManager.registerSetting(waitHurt);
        SettingManager.registerSetting(syncTimeout);
    //    SettingManager.registerSetting(forceWeb);
    //    SettingManager.registerSetting(airPlace);
    //    SettingManager.registerSetting(replace);
    }

    //Todo: Commented out features for potential future modules are here don't delete

    public PlayerEntity displayTarget;
    public float breakDamage, tempDamage, lastDamage;
    public Vec3d directionVec = null;
    private BlockPos tempPos, breakPos, syncPos;
    private Vec3d placeVec3d;
    public static BlockPos crystalPos;
    public final CacheTimer lastBreakTimer = new CacheTimer();
    private final CacheTimer placeTimer = new CacheTimer(), noPosTimer = new CacheTimer(), switchTimer = new CacheTimer(), calcDelay = new CacheTimer();

    public static boolean canSee(Vec3d from, Vec3d to) {
        HitResult result = MC.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, MC.player));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(SendMovementPacketListener.class, this);

        crystalPos = null;
        tempPos = null;
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.AddListener(LookAtListener.class, this);
        Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(SendMovementPacketListener.class, this);

        crystalPos = null;
        tempPos = null;
        breakPos = null;
        displayTarget = null;
        syncTimer.reset();
        lastBreakTimer.reset();
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onTick(TickEvent.Pre event) {

        if (Payload.getInstance().moduleManager.autoeat.isEating()) {
            return;
        }

        if (!thread.getValue()) {
            updateCrystalPos();
        }

        doInteract();
    }
    
    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onLook(LookAtEvent event) {
        if (Payload.getInstance().moduleManager.autoeat.isEating()) {
            return;
        }

        if (rotate.getValue() && yawStep.getValue() && directionVec != null && !noPosTimer.passedMs(1000)) {
            event.setTarget(directionVec, steps.getValue(), priority.getValue());
        }
    }

    @Override
    public void onRender(Render3DEvent event) {
        if (nullCheck()) return;

        //if (!thread.getValue()) updateCrystalPos();

        if (Payload.getInstance().moduleManager.autoeat.isEating()) {
            return;
        }

        if (!onlyTick.getValue()) doInteract();

        BlockPos cpos = crystalPos != null ? syncPos : crystalPos;

        if (cpos != null) {
            placeVec3d = cpos.down().toCenterPos();
        }
        else {
            placeVec3d = null;
        }

        if (posRender.getValue() && placeVec3d != null) {
                Box cbox = new Box(placeVec3d, placeVec3d);
                cbox = cbox.expand(0.5);

                Render3D.draw3DBox(event.GetMatrix(), event.getCamera(),cbox, posColor.getValue(), lineWidth.getValue());
        }

        if (targetRender.getValue() && displayTarget != null && !noPosTimer.passed(500)) {
            doRender(event.getRenderTickCounter().getTickDelta(true), displayTarget, event);
        }
    }

    public void doRender(float partialTicks, Entity entity, Render3DEvent event) {

        if (targetRender.getValue()) {
            Render3D.draw3DBox(event.GetMatrix(), event.getCamera(),((IEntity) entity).getDimensions().getBoxAt(new Vec3d(Interpolation.interpolate(entity.lastRenderX, entity.getX(), partialTicks), Interpolation.interpolate(entity.lastRenderY, entity.getY(), partialTicks), Interpolation.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0, 0.1, 0)
                    , targetColor.getValue(), lineWidth.getValue());
        }

        //Box -> Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0, 0.1, 0), ColorUtil.fadeColor(targetColor.getValue(), hitColor.getValue(), animation.get(0, animationTime.getValueInt(), ease.getValue())), false, true);
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (event.isCancelled()) return;

        if (event.GetPacket() instanceof UpdateSelectedSlotC2SPacket) {
            switchTimer.reset();
        }
    }

    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
        if (Payload.getInstance().moduleManager.autoeat.isEating()) {
            return;
        }

        if (!thread.getValue()) updateCrystalPos();
        if (!onlyTick.getValue()) doInteract();
    }

    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Post event) {

    }

    private void doInteract() {
        if (shouldReturn()) {
            return;
        }
        if (breakPos != null) {
            doBreak(breakPos);
            breakPos = null;
        }
        if (crystalPos != null) {
            doCrystal(crystalPos);
        }
    }

    private void updateCrystalPos() {
        getCrystalPos();
        lastDamage = tempDamage;
        crystalPos = tempPos;
    }

    private boolean shouldReturn() {
        if (eatingPause.getValue() && MC.player.isUsingItem()) { // || Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            lastBreakTimer.reset();
            return true;
        }
        /*
        if (preferAnchor.getValue() && AutoAnchor.INSTANCE.currentPos != null) {
            lastBreakTimer.reset();
            return true;
        }

         */
        return false;
    }

    public long getLong(float hello) {
        double ok = (double) hello;
        long fog = (long) ok;

        return fog;
    }

    private void getCrystalPos() {
        if (nullCheck()) {
            lastBreakTimer.reset();
            tempPos = null;
            return;
        }
        if (!calcDelay.passedMs(getLong(updateDelay.getValue()))) return;
        if (breakOnlyHasCrystal.getValue() && !MC.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !MC.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            lastBreakTimer.reset();
            tempPos = null;
            return;
        }
        boolean shouldReturn = shouldReturn();
        calcDelay.reset();
        breakPos = null;
        breakDamage = 0;
        tempPos = null;
        tempDamage = 0f;
        ArrayList<PlayerAndPredict> list = new ArrayList<>();
        for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getValue())) {
            if (target.hurtTime <= hurtTime.getValue()) {
                list.add(new PlayerAndPredict(target));
            }
        }
        PlayerAndPredict self = new PlayerAndPredict(MC.player);
        if (list.isEmpty()) {
            lastBreakTimer.reset();
        } else {
            for (BlockPos pos : OtherBlockUtils.getSphere((float) range.getValue() + 1)) {
                if (behindWall(pos)) continue;
                if (MC.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > range.getValue()) {
                    continue;
                }
                if (!canTouch(pos.down())) continue;
                if (!canPlaceCrystal(pos, true, false)) continue;
                for (PlayerAndPredict pap : list) {
                    if (lite.getValue() && liteCheck(pos.toCenterPos().add(0, -0.5, 0), pap.predict.getPos())) {
                        continue;
                    }
                    float damage = calculateDamage(pos, pap.player, pap.predict);
                    if (tempPos == null || damage > tempDamage) {
                        float selfDamage = calculateDamage(pos, self.player, self.predict);
                        if (selfDamage > maxSelf.getValue()) continue;
                        if (noSuicide.getValue() > 0 && selfDamage > MC.player.getHealth() + MC.player.getAbsorptionAmount() - noSuicide.getValue())
                            continue;
                        if (damage < EntityUtil.getHealth(pap.player)) {
                            if (damage < getDamage(pap.player)) continue;
                            if (smart.getValue()) {
                                if (getDamage(pap.player) == forceMin.getValue()) {
                                    if (damage < selfDamage - 2.5) {
                                        continue;
                                    }
                                } else {
                                    if (damage < selfDamage) {
                                        continue;
                                    }
                                }
                            }
                        }
                        displayTarget = pap.player;
                        tempPos = pos;
                        tempDamage = damage;
                    }
                }
            }
            for (Entity entity : MC.world.getEntities()) {
                if (entity instanceof EndCrystalEntity crystal) {
                    if (!MC.player.canSee(crystal) && MC.player.getEyePos().distanceTo(crystal.getPos()) > wallRange.getValue())
                        continue;
                    if (MC.player.getEyePos().distanceTo(crystal.getPos()) > range.getValue()) {
                        continue;
                    }
                    for (PlayerAndPredict pap : list) {
                        float damage = calculateDamage(crystal.getPos(), pap.player, pap.predict);
                        if (breakPos == null || damage > breakDamage) {
                            float selfDamage = calculateDamage(crystal.getPos(), self.player, self.predict);
                            if (selfDamage > maxSelf.getValue()) continue;
                            if (noSuicide.getValue() > 0 && selfDamage > MC.player.getHealth() + MC.player.getAbsorptionAmount() - noSuicide.getValue())
                                continue;
                            if (damage < EntityUtil.getHealth(pap.player)) {
                                if (damage < getDamage(pap.player)) continue;
                                if (smart.getValue()) {
                                    if (getDamage(pap.player) == forceMin.getValue()) {
                                        if (damage < selfDamage - 2.5) {
                                            continue;
                                        }
                                    } else {
                                        if (damage < selfDamage) {
                                            continue;
                                        }
                                    }
                                }
                            }
                            breakPos = new BlockPosX(crystal.getPos());
                            if (damage > tempDamage) {
                                displayTarget = pap.player;
                                //tempDamage = damage;
                            }
                        }
                    }
                }
            }
            if (doCrystal.getValue() && breakPos != null && !shouldReturn) {
                doBreak(breakPos);
                breakPos = null;
            }
            if (antiSurround.getValue() && PacketMine.getBreakPos() != null && PacketMine.progress >= 0.9 && !OtherBlockUtils.hasEntity(PacketMine.getBreakPos(), false)) {
                if (tempDamage <= antiSurroundMax.getValue()) {
                    for (PlayerAndPredict pap : list) {
                        for (Direction i : Direction.values()) {
                            if (i == Direction.DOWN || i == Direction.UP) continue;
                            BlockPos offsetPos = new BlockPosX(pap.player.getPos().add(0, 0.5, 0)).offset(i);
                            if (offsetPos.equals(PacketMine.getBreakPos())) {
                                if (canPlaceCrystal(offsetPos.offset(i), false, false)) {
                                    float selfDamage = calculateDamage(offsetPos.offset(i), self.player, self.predict);
                                    if (selfDamage < maxSelf.getValue() && !(noSuicide.getValue() > 0 && selfDamage > MC.player.getHealth() + MC.player.getAbsorptionAmount() - noSuicide.getValue())) {
                                        tempPos = offsetPos.offset(i);
                                        if (doCrystal.getValue() && tempPos != null && !shouldReturn) {
                                            doCrystal(tempPos);
                                        }
                                        return;
                                    }
                                }
                                for (Direction ii : Direction.values()) {
                                    if (ii == Direction.DOWN || ii == i) continue;
                                    if (canPlaceCrystal(offsetPos.offset(ii), false, false)) {
                                        float selfDamage = calculateDamage(offsetPos.offset(ii), self.player, self.predict);
                                        if (selfDamage < maxSelf.getValue() && !(noSuicide.getValue() > 0 && selfDamage > MC.player.getHealth() + MC.player.getAbsorptionAmount() - noSuicide.getValue())) {
                                            tempPos = offsetPos.offset(ii);
                                            if (doCrystal.getValue() && tempPos != null && !shouldReturn) {
                                                doCrystal(tempPos);
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (doCrystal.getValue() && tempPos != null && !shouldReturn) {
            doCrystal(tempPos);
        }
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        BlockPos boost2 = boost.up();

        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
                && OtherBlockUtils.getClickSideStrict(obsPos) != null
                && noEntityBlockCrystal(boost, ignoreCrystal, ignoreItem)
                && noEntityBlockCrystal(boost2, ignoreCrystal, ignoreItem)
                && (MC.world.isAir(boost) || hasCrystal(boost) && getBlock(boost) == Blocks.FIRE)
                && (!Payload.getInstance().moduleManager.antiCheat.lowVersion.getValue() || MC.world.isAir(boost2));
    }

    private boolean liteCheck(Vec3d from, Vec3d to) {
        return !canSee(from, to) && !canSee(from, to.add(0, 1.8, 0));
    }

    private boolean noEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : OtherBlockUtils.getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.obsMode.getValue())
                continue;
            if (entity instanceof EndCrystalEntity) {
                if (!ignoreCrystal) return false;
                if (MC.player.canSee(entity) || MC.player.getEyePos().distanceTo(entity.getPos()) <= wallRange.getValue()) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    public boolean behindWall(BlockPos pos) {
        Vec3d testVec;
        /*if (CombatSetting.INSTANCE.lowVersion.getValue()) {
            testVec = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        } else {
            testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        }*/
        testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        HitResult result = MC.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, MC.player));
        if (result == null || result.getType() == HitResult.Type.MISS) return false;
        return MC.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > wallRange.getValue();
    }

    private boolean canTouch(BlockPos pos) {
        Direction side = OtherBlockUtils.getClickSideStrict(pos);
        return side != null && pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5)).distanceTo(MC.player.getEyePos()) <= range.getValue();
    }

    private void doCrystal(BlockPos pos) {
        if (canPlaceCrystal(pos, false, false)) {
            doPlace(pos);
        } else {
            doBreak(pos);
        }
    }

    public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
        return calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), player, predict);
    }

    public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
        if (ignoreMine.getValue() && PacketMine.getBreakPos() != null) {
            if (MC.player.getEyePos().distanceTo(PacketMine.getBreakPos().toCenterPos()) <= PacketMine.INSTANCE.range.getValue()) {
                if (PacketMine.progress >= constantProgress.getValue() / 100) {
                    CombatUtil.modifyPos = PacketMine.getBreakPos();
                    CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                }
            }
        }
        if (terrainIgnore.getValue()) {
            CombatUtil.terrainIgnore = true;
        }
        float damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
        CombatUtil.modifyPos = null;
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    private double getDamage(PlayerEntity target) {
        /*
        if (!PacketMine.INSTANCE.obsidian.isPressed() && slowPlace.getValue() && lastBreakTimer.passed(slowDelay.getValue()) && !PistonCrystal.INSTANCE.isOn()) {
            return slowMinDamage.getValue();
        }

         */

        if (slowPlace.getValue() && lastBreakTimer.passedMs(getLong(slowDelay.getValue()))) {
            return slowMinDamage.getValue();
        }
        if (forcePlace.getValue() && EntityUtil.getHealth(target) <= forceMaxHealth.getValue()) { // && !PacketMine.INSTANCE.obsidian.isPressed() && !PistonCrystal.INSTANCE.isOn()) {
            return forceMin.getValue();
        }
        if (armorBreaker.getValue()) {
            DefaultedList<ItemStack> armors = target.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.isEmpty()) continue;
                if (EntityUtil.getDamagePercent(armor) > maxDurable.getValue()) continue;
                return armorBreakerDamage.getValue();
            }
        }
        /*
        if (PistonCrystal.INSTANCE.isOn()) {
            return autoMinDamage.getValue();
        }
        
         */
        return minDamage.getValue();
    }

    public boolean findCrystal() {
        if (autoSwap.getValue() == SwapMode.Off) return false;
        return getCrystal() != -1;
    }

    private final CacheTimer syncTimer = new CacheTimer();

    private void doBreak(BlockPos pos) {
        noPosTimer.reset();
        if (!breakSetting.getValue()) return;
        if (displayTarget != null && displayTarget.hurtTime > waitHurt.getValue() && !syncTimer.passedMs(getLong(syncTimeout.getValue()))) {
            return;
        }
        lastBreakTimer.reset();
        if (!switchTimer.passedMs(getLong(switchCooldown.getValue()))) {
            return;
        }
        syncTimer.reset();
        for (EndCrystalEntity entity : OtherBlockUtils.getEndCrystals(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1))) {
            if (entity.age < minAge.getValue()) continue;
            if (rotate.getValue() && onBreak.getValue()) {
                if (!faceVector(entity.getPos().add(0, yOffset.getValue(), 0))) return;
            }
            if (!CombatUtil.breakTimer.passedMs(getLong(breakDelay.getValue()))) return;
            CombatUtil.breakTimer.reset();
            syncPos = pos;
            MC.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, MC.player.isSneaking()));
            MC.player.resetLastAttackedTicks();
            EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
            if (breakRemove.getValue()) {
                MC.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
            }
            if (crystalPos != null && displayTarget != null && lastDamage >= getDamage(displayTarget) && afterBreak.getValue()) {
                if (!yawStep.getValue() || !checkFov.getValue() || Payload.getInstance().rotationManager.inFov(entity.getPos(), fov.getValue())) {
                    doPlace(crystalPos);
                }
            }
            /*
            if (forceWeb.getValue() && AutoWeb.INSTANCE.isOn()) {
                AutoWeb.force = true;
            }

             */
            if (rotate.getValue() && !yawStep.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
                Payload.getInstance().rotationManager.snapBack();
            }
            return;
        }
    }

    private void doPlace(BlockPos pos) {
        noPosTimer.reset();
        if (!place.getValue()) return;
        if (!MC.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !MC.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            return;
        }
        if (!canTouch(pos.down())) {
            return;
        }
        BlockPos obsPos = pos.down();
        Direction facing = OtherBlockUtils.getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5);
        if (facing != Direction.UP && facing != Direction.DOWN) {
            vec = vec.add(0, 0.45, 0);
        }
        if (rotate.getValue()) {
            if (!faceVector(vec)) return;
        }
        if (!placeTimer.passedMs(getLong(placeDelay.getValue()))) return;
        if (MC.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || MC.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
            placeTimer.reset();
            syncPos = pos;
            placeCrystal(pos);
        } else {
            placeTimer.reset();
            syncPos = pos;
            int old = MC.player.getInventory().selectedSlot;
            int crystal = getCrystal();
            if (crystal == -1) return;
            doSwap(crystal);
            placeCrystal(pos);
            if (autoSwap.getValue() == SwapMode.Silent) {
                doSwap(old);
            } else if (autoSwap.getValue() == SwapMode.Inventory) {
                doSwap(crystal);
                EntityUtil.syncInventory();
            }
        }
    }

    private void doSwap(int slot) {
        if (autoSwap.getValue() == SwapMode.Silent || autoSwap.getValue() == SwapMode.Normal) {
            InventoryUtil.switchToSlot(slot);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            InventoryUtil.inventorySwap(slot, MC.player.getInventory().selectedSlot);
        }
    }

    private int getCrystal() {
        if (autoSwap.getValue() == SwapMode.Silent || autoSwap.getValue() == SwapMode.Normal) {
            return InventoryUtil.findItem(Items.END_CRYSTAL);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
        }
        return -1;
    }

    private void placeCrystal(BlockPos pos) {
        //PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
        boolean offhand = MC.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = OtherBlockUtils.getClickSide(obsPos);
        OtherBlockUtils.clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, swingMode.getValue());
    }

    private boolean faceVector(Vec3d directionVec) {
        if (!yawStep.getValue()) {
            Payload.getInstance().rotationManager.lookAt(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            if (Payload.getInstance().rotationManager.inFov(directionVec, fov.getValue())) {
                return true;
            }
        }
        return !checkFov.getValue();
    }

    private class PlayerAndPredict {
        final PlayerEntity player;
        final PlayerEntity predict;

        private PlayerAndPredict(PlayerEntity player) {
            this.player = player;
            if (predictTicks.getValue() > 0) {
                predict = new PlayerEntity(MC.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "CrystalAuraPredictionEntity")) {
                    @Override
                    public boolean isSpectator() {
                        return false;
                    }

                    @Override
                    public boolean isCreative() {
                        return false;
                    }

                    @Override
                    public boolean isOnGround() {
                        return player.isOnGround();
                    }
                };
                predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, Math.round(predictTicks.getValue()), true)));
                predict.setHealth(player.getHealth());
                predict.prevX = player.prevX;
                predict.prevZ = player.prevZ;
                predict.prevY = player.prevY;
                predict.setOnGround(player.isOnGround());
                predict.getInventory().clone(player.getInventory());
                predict.setPose(player.getPose());
                for (StatusEffectInstance se : new ArrayList<>(player.getStatusEffects())) {
                    predict.addStatusEffect(se);
                }
            } else {
                predict = player;
            }
        }
    }
}