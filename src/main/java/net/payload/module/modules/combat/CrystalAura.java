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

import java.util.*;

import static net.payload.utils.block.OtherBlockUtils.getBlock;
import static net.payload.utils.block.OtherBlockUtils.hasCrystal;

public class CrystalAura extends Module implements TickListener, Render3DListener, LookAtListener, SendPacketListener, SendMovementPacketListener {

    private enum SwapMode { Off, Normal, Silent, Inventory }

    // Settings groups
    private final SettingGroup sgGeneral = group("autocrystal_general", "General", "General AutoCrystal settings");
    private final SettingGroup sgRotation = group("autocrystal_rotation", "Rotation", "Rotation settings");
    private final SettingGroup sgInteraction = group("autocrystal_interaction", "Interaction", "Placement & breaking settings");
    private final SettingGroup sgCalculation = group("autocrystal_calculation", "Calculation", "Damage & prediction");
    private final SettingGroup sgMisc = group("autocrystal_misc", "Miscellaneous", "Specialized combat options");
    private final SettingGroup sgRender = group("autocrystal_render", "Render", "ESP settings");

    // General
    private final BooleanSetting breakOnlyHasCrystal = bool(sgGeneral, "autocrystal_only_hold", "Only Hold", "Only break when holding a crystal", true);
    private final EnumSetting<SwingSide> swingMode = enum_(sgGeneral, "autocrystal_swing", "Swing", "Hand to swing", SwingSide.All);
    private final BooleanSetting eatingPause = bool(sgGeneral, "autocrystal_eating_pause", "Eating Pause", "Pause while eating", true);
    private final FloatSetting switchCooldown = float_(sgGeneral, "autocrystal_switch_pause", "Switch Pause", "Cooldown after switch (ms)", 100f, 0f, 1000f, 10f);
    private final FloatSetting targetRange = float_(sgGeneral, "autocrystal_target_range", "Target Range", "Max target distance", 12f, 0f, 20f, 0.5f);
    private final FloatSetting updateDelay = float_(sgGeneral, "autocrystal_update_delay", "Update Delay", "Calc interval (ms)", 50f, 0f, 1000f, 10f);
    // wallRange is kept for backward compatibility but no longer used internally – replaced by placeWallRange / breakWallRange
    private final FloatSetting wallRange = float_(sgGeneral, "autocrystal_wall_range", "Wall Range", "Max distance through walls (legacy)", 6f, 0f, 6f, 0.1f);

    // Rotation
    private final BooleanSetting rotate = bool(sgRotation, "autocrystal_rotate", "Rotate", "Rotate towards crystals", true);
    private final BooleanSetting onBreak = bool(sgRotation, "autocrystal_on_break", "On Break", "Only rotate when breaking", false);
    private final FloatSetting yOffset = float_(sgRotation, "autocrystal_y_offset", "Y Offset", "Vertical rotation offset", 0.05f, 0f, 1f, 0.01f);
    private final BooleanSetting yawStep = bool(sgRotation, "autocrystal_yaw_step", "Yaw Step", "Gradual rotation", false);
    private final FloatSetting steps = float_(sgRotation, "autocrystal_steps", "Steps", "Yaw step size", 0.8f, 0f, 1f, 0.1f);
    private final BooleanSetting checkFov = bool(sgRotation, "autocrystal_only_looking", "Only Looking", "Only crystals in FOV", false);
    private final FloatSetting fov = float_(sgRotation, "autocrystal_fov", "FOV", "FOV angle (degrees)", 30f, 0f, 50f, 1f);
    private final FloatSetting priority = float_(sgRotation, "autocrystal_priority", "Priority", "Rotation priority", 10f, 0f, 100f, 1f);

    // Interaction (damage + place/break)
    private final FloatSetting minDamage = float_(sgInteraction, "autocrystal_min", "Min", "Min target damage", 5f, 0f, 36f, 0.5f);
    private final FloatSetting maxSelf = float_(sgInteraction, "autocrystal_self", "Self", "Max self damage", 12f, 0f, 36f, 0.5f);
    private final FloatSetting range = float_(sgInteraction, "autocrystal_range", "Range", "Place range", 5f, 0f, 6f, 0.1f);
    private final FloatSetting placeWallRange = float_(sgInteraction, "autocrystal_place_wall_range", "Place Wall Range", "Max place distance through walls", 2.5f, 0f, 6f, 0.1f);
    private final FloatSetting breakWallRange = float_(sgInteraction, "autocrystal_break_wall_range", "Break Wall Range", "Max break distance through walls", 2.5f, 0f, 6f, 0.1f);
    private final FloatSetting noSuicide = float_(sgInteraction, "autocrystal_no_suicide", "No Suicide", "Health buffer", 3f, 0f, 10f, 0.5f);
    private final BooleanSetting smart = bool(sgInteraction, "autocrystal_smart", "Smart", "Optimize placement", true);
    private final BooleanSetting place = bool(sgInteraction, "autocrystal_place", "Place", "Place crystals", true);
    private final FloatSetting placeDelay = float_(sgInteraction, "autocrystal_place_delay", "Place Delay", "Delay between placements (ms)", 150f, 0f, 1000f, 10f);
    private final EnumSetting<SwapMode> autoSwap = enum_(sgInteraction, "autocrystal_auto_swap", "Auto Swap", "Swap mode", SwapMode.Normal);
    private final BooleanSetting afterBreak = bool(sgInteraction, "autocrystal_after_break", "After Break", "Place after breaking", true);
    private final BooleanSetting breakSetting = bool(sgInteraction, "autocrystal_break", "Break", "Break crystals", true);
    private final FloatSetting breakDelay = float_(sgInteraction, "autocrystal_break_delay", "Break Delay", "Delay between breaks (ms)", 150f, 0f, 1000f, 10f);
    private final FloatSetting minAge = float_(sgInteraction, "autocrystal_min_age", "Min Age", "Min crystal age (ticks)", 0f, 0f, 20f, 1f);
    private final BooleanSetting breakRemove = bool(sgInteraction, "autocrystal_remove", "Memory Remove", "Remove crystal client-side", false);
    private final BooleanSetting onlyTick = bool(sgInteraction, "autocrystal_only_tick", "Only Tick", "Only operate on ticks", false);

    // Calculation
    private final BooleanSetting doCrystal = bool(sgCalculation, "autocrystal_thread_interact", "Thread Interact", "Interact in calc thread", false);
    private final BooleanSetting lite = bool(sgCalculation, "autocrystal_less_cpu", "Less CPU", "Optimize CPU usage", false);
    private final FloatSetting predictTicks = float_(sgCalculation, "autocrystal_predict", "Predict", "Prediction ticks", 4f, 0f, 10f, 1f);
    private final BooleanSetting terrainIgnore = bool(sgCalculation, "autocrystal_terrain_ignore", "Terrain Ignore", "Ignore terrain in damage calc", true);

    // Misc
    private final BooleanSetting ignoreMine = bool(sgMisc, "autocrystal_ignore_mine", "Ignore Mine", "Ignore mined blocks", true);
    private final FloatSetting constantProgress = float_(sgMisc, "autocrystal_progress", "Progress", "Mining progress threshold (%)", 90f, 0f, 100f, 1f);
    private final BooleanSetting antiSurround = bool(sgMisc, "autocrystal_anti_surround", "Anti Surround", "Break enemy surrounds", false);
    private final FloatSetting antiSurroundMax = float_(sgMisc, "autocrystal_when_lower", "When Lower", "Max damage for anti-surround", 5f, 0f, 36f, 0.5f);
    private final BooleanSetting slowPlace = bool(sgMisc, "autocrystal_timeout", "Timeout", "Timeout after no target", true);
    private final FloatSetting slowDelay = float_(sgMisc, "autocrystal_timeout_delay", "Timeout Delay", "Timeout duration (ms)", 200f, 0f, 2000f, 50f);
    private final FloatSetting slowMinDamage = float_(sgMisc, "autocrystal_timeout_min", "Timeout Override HP", "Min damage to bypass timeout", 1.5f, 0f, 36f, 0.5f);
    private final BooleanSetting forcePlace = bool(sgMisc, "autocrystal_force_place", "Force Place", "Force place on low HP", true);
    private final FloatSetting forceMaxHealth = float_(sgMisc, "autocrystal_lower_than", "Lower Than", "Target HP to force", 7f, 0f, 36f, 1f);
    private final FloatSetting forceMin = float_(sgMisc, "autocrystal_force_min", "Force Min HP", "Min damage for forced place", 1.5f, 0f, 36f, 0.5f);
    private final BooleanSetting armorBreaker = bool(sgMisc, "autocrystal_armor_breaker", "Armor Breaker", "Target low durability armor", true);
    private final FloatSetting maxDurable = float_(sgMisc, "autocrystal_max_durable", "Max Durable", "Max durability % to target", 8f, 0f, 100f, 1f);
    private final FloatSetting armorBreakerDamage = float_(sgMisc, "autocrystal_breaker_min", "Breaker Min", "Min damage for armor break", 3f, 0f, 36f, 0.5f);
    private final FloatSetting hurtTime = float_(sgMisc, "autocrystal_hurt_time", "Hurt Time", "Max hurt time (ticks)", 10f, 0f, 10f, 1f);
    private final FloatSetting waitHurt = float_(sgMisc, "autocrystal_wait_hurt", "Wait Hurt", "Delay after hurt (ticks)", 10f, 0f, 10f, 1f);
    private final FloatSetting syncTimeout = float_(sgMisc, "autocrystal_wait_timeout", "Wait Timeout", "Max sync wait (ms)", 500f, 0f, 2000f, 10f);

    // Render
    private final BooleanSetting targetRender = bool(sgRender, "autocrystal_targetrender", "TargetESP", "Render target box", true);
    private final ColorSetting targetColor = color(sgRender, "autocrystal_targetcolor", "Target Color", "", new Color(0, 255, 0, 75));
    private final BooleanSetting posRender = bool(sgRender, "autocrystal_posrender", "PlaceESP", "Render placement box", true);
    private final ColorSetting posColor = color(sgRender, "autocrystal_poscolor", "Placement Color", "", new Color(255, 0, 0, 75));
    private final FloatSetting lineWidth = float_(sgRender, "autocrystal_line_width", "Line Width", "Outline width", 1.5f, 0.1f, 5f, 0.1f);

    // Legacy public fields
    public static BlockPos crystalPos;
    public PlayerEntity displayTarget;
    public final BooleanSetting replace = bool(sgMisc, "autocrystal_replace", "Web Replace", "Replace blocks with crystals", false);
    public float breakDamage, tempDamage, lastDamage;
    public Vec3d directionVec = null;

    private BlockPos tempPos, breakPos, syncPos;
    private Vec3d placeVec3d;
    public final CacheTimer lastBreakTimer = new CacheTimer();
    private final CacheTimer placeTimer = new CacheTimer(), noPosTimer = new CacheTimer(), switchTimer = new CacheTimer(), calcDelay = new CacheTimer();
    private final CacheTimer syncTimer = new CacheTimer();

    // Helper methods for clean registration
    private SettingGroup group(String id, String name, String desc) {
        SettingGroup g = SettingGroup.Builder.builder().id(id).displayName(name).description(desc).build();
        this.addSetting(g);
        SettingManager.registerSetting(g);
        return g;
    }

    private FloatSetting float_(SettingGroup g, String id, String name, String desc, float def, float min, float max, float step) {
        FloatSetting s = FloatSetting.builder().id(id).displayName(name).description(desc).defaultValue(def).minValue(min).maxValue(max).step(step).build();
        g.addSetting(s);
        SettingManager.registerSetting(s);
        return s;
    }

    private BooleanSetting bool(SettingGroup g, String id, String name, String desc, boolean def) {
        BooleanSetting s = BooleanSetting.builder().id(id).displayName(name).description(desc).defaultValue(def).build();
        g.addSetting(s);
        SettingManager.registerSetting(s);
        return s;
    }

    private <T extends Enum<T>> EnumSetting<T> enum_(SettingGroup g, String id, String name, String desc, T def) {
        EnumSetting<T> s = EnumSetting.<T>builder().id(id).displayName(name).description(desc).defaultValue(def).build();
        g.addSetting(s);
        SettingManager.registerSetting(s);
        return s;
    }

    private ColorSetting color(SettingGroup g, String id, String name, String desc, Color def) {
        ColorSetting s = ColorSetting.builder().id(id).displayName(name).description(desc).defaultValue(def).build();
        g.addSetting(s);
        SettingManager.registerSetting(s);
        return s;
    }

    public CrystalAura() {
        super("CrystalAura");
        this.setCategory(Category.of("Combat"));
        this.setDescription("Destroys and places endcrystals to kill opponents");
    }

    public static boolean canSee(Vec3d from, Vec3d to) {
        HitResult result = MC.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, MC.player));
        return result == null || result.getType() == HitResult.Type.MISS;
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
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(SendMovementPacketListener.class, this);
        crystalPos = null;
        tempPos = null;
    }

    @Override public void onToggle() {}

    @Override
    public void onTick(TickEvent.Pre event) {
        if (Payload.getInstance().moduleManager.autoeat.isEating()) return;
        if (!threadUnused()) updateCrystalPos();
        doInteract();
    }

    @Override public void onTick(TickEvent.Post event) {}

    @Override
    public void onLook(LookAtEvent event) {
        if (Payload.getInstance().moduleManager.autoeat.isEating()) return;
        if (rotate.getValue() && yawStep.getValue() && directionVec != null && !noPosTimer.passedMs(1000)) {
            event.setTarget(directionVec, steps.getValue(), priority.getValue());
        }
    }

    @Override
    public void onRender(Render3DEvent event) {
        if (nullCheck()) return;
        if (Payload.getInstance().moduleManager.autoeat.isEating()) return;
        if (!onlyTick.getValue()) doInteract();

        BlockPos cpos = crystalPos != null ? syncPos : crystalPos;
        placeVec3d = cpos != null ? cpos.down().toCenterPos() : null;

        if (posRender.getValue() && placeVec3d != null) {
            Box cbox = new Box(placeVec3d, placeVec3d).expand(0.5);
            Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), cbox, posColor.getValue(), lineWidth.getValue());
        }

        if (targetRender.getValue() && displayTarget != null && !noPosTimer.passed(500)) {
            doRender(event.getRenderTickCounter().getTickDelta(true), displayTarget, event);
        }
    }

    public void doRender(float partialTicks, Entity entity, Render3DEvent event) {
        if (targetRender.getValue()) {
            Vec3d pos = new Vec3d(
                Interpolation.interpolate(entity.lastRenderX, entity.getX(), partialTicks),
                Interpolation.interpolate(entity.lastRenderY, entity.getY(), partialTicks),
                Interpolation.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks)
            );
            Render3D.draw3DBox(event.GetMatrix(), event.getCamera(),
                ((IEntity) entity).getDimensions().getBoxAt(pos).expand(0, 0.1, 0),
                targetColor.getValue(), lineWidth.getValue());
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (event.isCancelled()) return;
        if (event.GetPacket() instanceof UpdateSelectedSlotC2SPacket) switchTimer.reset();
    }

    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
        if (Payload.getInstance().moduleManager.autoeat.isEating()) return;
        if (!threadUnused()) updateCrystalPos();
        if (!onlyTick.getValue()) doInteract();
    }

    @Override public void onSendMovementPacket(SendMovementPacketEvent.Post event) {}

    private boolean threadUnused() { return false; }

    private void doInteract() {
        if (shouldReturn()) return;
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
        if (eatingPause.getValue() && MC.player.isUsingItem()) {
            lastBreakTimer.reset();
            return true;
        }
        return false;
    }

    // --------------- Wall-range helpers ---------------
    private boolean isPlaceInRange(BlockPos baseBlock) {
        double dist = MC.player.getEyePos().distanceTo(baseBlock.toCenterPos().add(0, -0.5, 0));
        // Use click-side raycast to determine visibility
        if (OtherBlockUtils.getClickSide(baseBlock) != null) {
            return dist <= range.getValue();
        } else {
            return dist <= placeWallRange.getValue();
        }
    }

    private boolean isBreakInRange(EndCrystalEntity crystal) {
        double dist = MC.player.getEyePos().distanceTo(crystal.getPos());
        if (MC.player.canSee(crystal)) {
            return dist <= range.getValue();
        } else {
            return dist <= breakWallRange.getValue();
        }
    }
    // ------------------------------------------------

    private void getCrystalPos() {
        if (nullCheck()) {
            lastBreakTimer.reset();
            tempPos = null;
            return;
        }
        if (!calcDelay.passedMs((long)(float)updateDelay.getValue())) return;
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
            return;
        }
        for (BlockPos pos : OtherBlockUtils.getSphere((float) range.getValue() + 1)) {
            if (behindWall(pos)) continue;               // still uses the old wallRange inside behindWall, but we'll update behindWall to use placeWallRange
            if (!isPlaceInRange(pos.down())) continue;   // NEW: wall‑aware range check
            if (!canTouch(pos.down())) continue;
            if (!canPlaceCrystal(pos, true, false)) continue;
            for (PlayerAndPredict pap : list) {
                if (lite.getValue() && liteCheck(pos.toCenterPos().add(0, -0.5, 0), pap.predict.getPos())) continue;
                float damage = calculateDamage(pos, pap.player, pap.predict);
                if (tempPos == null || damage > tempDamage) {
                    float selfDamage = calculateDamage(pos, self.player, self.predict);
                    if (selfDamage > maxSelf.getValue()) continue;
                    if (noSuicide.getValue() > 0 && selfDamage > MC.player.getHealth() + MC.player.getAbsorptionAmount() - noSuicide.getValue()) continue;
                    if (damage < EntityUtil.getHealth(pap.player)) {
                        if (damage < getDamage(pap.player)) continue;
                        if (smart.getValue()) {
                            if (getDamage(pap.player) == forceMin.getValue()) {
                                if (damage < selfDamage - 2.5) continue;
                            } else {
                                if (damage < selfDamage) continue;
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
                if (!isBreakInRange(crystal)) continue;   // NEW: uses breakWallRange for non‑visible
                for (PlayerAndPredict pap : list) {
                    float damage = calculateDamage(crystal.getPos(), pap.player, pap.predict);
                    if (breakPos == null || damage > breakDamage) {
                        float selfDamage = calculateDamage(crystal.getPos(), self.player, self.predict);
                        if (selfDamage > maxSelf.getValue()) continue;
                        if (noSuicide.getValue() > 0 && selfDamage > MC.player.getHealth() + MC.player.getAbsorptionAmount() - noSuicide.getValue()) continue;
                        if (damage < EntityUtil.getHealth(pap.player)) {
                            if (damage < getDamage(pap.player)) continue;
                            if (smart.getValue()) {
                                if (getDamage(pap.player) == forceMin.getValue()) {
                                    if (damage < selfDamage - 2.5) continue;
                                } else {
                                    if (damage < selfDamage) continue;
                                }
                            }
                        }
                        breakPos = new BlockPosX(crystal.getPos());
                        if (damage > tempDamage) {
                            displayTarget = pap.player;
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
                && (MC.world.isAir(boost) || (hasCrystal(boost) && getBlock(boost) == Blocks.FIRE))
                && (!Payload.getInstance().moduleManager.antiCheat.lowVersion.getValue() || MC.world.isAir(boost2));
    }

    private boolean liteCheck(Vec3d from, Vec3d to) {
        return !canSee(from, to) && !canSee(from, to.add(0, 1.8, 0));
    }

    private boolean noEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : OtherBlockUtils.getEntities(new Box(pos))) {
            if (!entity.isAlive() || (ignoreItem && entity instanceof ItemEntity) || (entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.obsMode.getValue()))
                continue;
            if (entity instanceof EndCrystalEntity) {
                if (!ignoreCrystal) return false;
                if (MC.player.canSee(entity) || MC.player.getEyePos().distanceTo(entity.getPos()) <= breakWallRange.getValue()) continue;
            }
            return false;
        }
        return true;
    }

    public boolean behindWall(BlockPos pos) {
        // Now uses placeWallRange instead of the old wallRange
        Vec3d testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        HitResult result = MC.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, MC.player));
        if (result == null || result.getType() == HitResult.Type.MISS) return false;
        return MC.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > placeWallRange.getValue();
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
        if (terrainIgnore.getValue()) CombatUtil.terrainIgnore = true;
        float damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
        CombatUtil.modifyPos = null;
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    private double getDamage(PlayerEntity target) {
        if (slowPlace.getValue() && lastBreakTimer.passedMs((long)(float)slowDelay.getValue())) {
            return slowMinDamage.getValue();
        }
        if (forcePlace.getValue() && EntityUtil.getHealth(target) <= forceMaxHealth.getValue()) {
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
        return minDamage.getValue();
    }

    public boolean findCrystal() {
        if (autoSwap.getValue() == SwapMode.Off) return false;
        return getCrystal() != -1;
    }

    private void doBreak(BlockPos pos) {
        noPosTimer.reset();
        if (!breakSetting.getValue()) return;
        if (displayTarget != null && displayTarget.hurtTime > waitHurt.getValue() && !syncTimer.passedMs((long)(float)syncTimeout.getValue())) return;
        lastBreakTimer.reset();
        if (!switchTimer.passedMs((long)(float)switchCooldown.getValue())) return;
        syncTimer.reset();
        for (EndCrystalEntity entity : OtherBlockUtils.getEndCrystals(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1))) {
            if (entity.age < minAge.getValue()) continue;
            if (rotate.getValue() && onBreak.getValue()) {
                if (!faceVector(entity.getPos().add(0, yOffset.getValue(), 0))) return;
            }
            if (!CombatUtil.breakTimer.passedMs((long)(float)breakDelay.getValue())) return;
            CombatUtil.breakTimer.reset();
            syncPos = pos;
            MC.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, MC.player.isSneaking()));
            MC.player.resetLastAttackedTicks();
            EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
            if (breakRemove.getValue()) MC.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
            if (crystalPos != null && displayTarget != null && lastDamage >= getDamage(displayTarget) && afterBreak.getValue()) {
                if (!yawStep.getValue() || !checkFov.getValue() || Payload.getInstance().rotationManager.inFov(entity.getPos(), fov.getValue())) {
                    doPlace(crystalPos);
                }
            }
            if (rotate.getValue() && !yawStep.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
                Payload.getInstance().rotationManager.snapBack();
            }
            return;
        }
    }

    private void doPlace(BlockPos pos) {
        noPosTimer.reset();
        if (!place.getValue()) return;
        if (!MC.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !MC.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) return;
        if (!canTouch(pos.down())) return;
        BlockPos obsPos = pos.down();
        Direction facing = OtherBlockUtils.getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5);
        if (facing != Direction.UP && facing != Direction.DOWN) vec = vec.add(0, 0.45, 0);
        if (rotate.getValue() && !faceVector(vec)) return;
        if (!placeTimer.passedMs((long)(float)placeDelay.getValue())) return;
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
                    @Override public boolean isSpectator() { return false; }
                    @Override public boolean isCreative() { return false; }
                    @Override public boolean isOnGround() { return player.isOnGround(); }
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