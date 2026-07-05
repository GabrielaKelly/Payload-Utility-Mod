package net.payload.module.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.client.AntiCheat;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.MovementUtil;
import net.payload.utils.player.combat.CombatUtil;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.render.Render3D;

public class Surround extends Module implements GameLeftListener, TickListener, Render3DListener, PlayerMoveEventListener, LookAtListener, SendMovementPacketListener, PlayerDeathListener {


    private final SettingGroup generalSettings;

    private final SettingGroup rotationSettings;

    private final SettingGroup checkSettings;


    FloatSetting placeDelay = FloatSetting.builder()
            .id("surround_place_delay")
            .displayName("PlaceDelay")
            .description("Delay between block placements")
            .defaultValue(50f)
            .minValue(0f)
            .maxValue(500f)
            .step(1f)
            .build();

    FloatSetting blocksPer = FloatSetting.builder()
            .id("surround_blocks_per")
            .displayName("BlocksPer")
            .description("Number of blocks to place per tick")
            .defaultValue(1f)
            .minValue(1f)
            .maxValue(8f)
            .step(1f)
            .build();

    BooleanSetting packetPlace = BooleanSetting.builder()
            .id("surround_packet_place")
            .displayName("PacketPlace")
            .description("Use packets for block placement")
            .defaultValue(true)
            .build();

    BooleanSetting onlyTick = BooleanSetting.builder()
            .id("surround_only_tick")
            .displayName("OnlyTick")
            .description("Only place on tick")
            .defaultValue(true)
            .build();

    BooleanSetting breakCrystal = BooleanSetting.builder()
            .id("surround_break")
            .displayName("Break")
            .description("Break crystals during surround")
            .defaultValue(true)
            .build();

    BooleanSetting eatPause = BooleanSetting.builder()
            .id("surround_eating_pause")
            .displayName("EatingPause")
            .description("Pause while eating")
            .defaultValue(true)
            .build();

    BooleanSetting center = BooleanSetting.builder()
            .id("surround_center")
            .displayName("Center")
            .description("Center player position")
            .defaultValue(true)
            .build();

    BooleanSetting extend = BooleanSetting.builder()
            .id("surround_extend")
            .displayName("Extend")
            .description("Extend surround placement")
            .defaultValue(true)
            .build();

    BooleanSetting onlySelf = BooleanSetting.builder()
            .id("surround_only_self")
            .displayName("OnlySelf")
            .description("Only extend for self")
            .defaultValue(false)
            .build();

    BooleanSetting inventory = BooleanSetting.builder()
            .id("surround_inventory_swap")
            .displayName("InventorySwap")
            .description("Allow inventory swapping")
            .defaultValue(true)
            .build();

    BooleanSetting enderChest = BooleanSetting.builder()
            .id("surround_enderchest")
            .displayName("EnderChest")
            .description("Use ender chests")
            .defaultValue(true)
            .build();

    private final BooleanSetting rotate = BooleanSetting.builder()
            .id("surround_rotate")
            .displayName("Rotate")
            .description("Enable rotation")
            .defaultValue(true)
            .build();

    private final BooleanSetting yawStep = BooleanSetting.builder()
            .id("surround_yaw_step")
            .displayName("YawStep")
            .description("Enable smooth yaw rotation")
            .defaultValue(false)
            .build();

    private final FloatSetting steps = FloatSetting.builder()
            .id("surround_steps")
            .displayName("Steps")
            .description("Step size for yaw rotation")
            .defaultValue(0.05f)
            .minValue(0f)
            .maxValue(1f)
            .step(0.01f)
            .build();

    private final BooleanSetting checkFov = BooleanSetting.builder()
            .id("surround_check_fov")
            .displayName("OnlyLooking")
            .description("Only place within FOV")
            .defaultValue(true)
            .build();

    private final FloatSetting fov = FloatSetting.builder()
            .id("surround_fov")
            .displayName("Fov")
            .description("Field of view range")
            .defaultValue(5f)
            .minValue(0f)
            .maxValue(30f)
            .step(1f)
            .build();

    private final FloatSetting priority = FloatSetting.builder()
            .id("surround_priority")
            .displayName("Priority")
            .description("Rotation priority level")
            .defaultValue(10f)
            .minValue(0f)
            .maxValue(100f)
            .step(1f)
            .build();

    private final BooleanSetting detectMining = BooleanSetting.builder()
            .id("surround_detect_mining")
            .displayName("DetectMining")
            .description("Detect when blocks are being mined")
            .defaultValue(false)
            .build();

    private final BooleanSetting usingPause = BooleanSetting.builder()
            .id("surround_using_pause")
            .displayName("UsingPause")
            .description("Pause while using items")
            .defaultValue(true)
            .build();

    public final BooleanSetting inAir = BooleanSetting.builder()
            .id("surround_in_air")
            .displayName("InAir")
            .description("Allow surround while in air")
            .defaultValue(true)
            .build();

    private final BooleanSetting moveDisable = BooleanSetting.builder()
            .id("surround_move_disable")
            .displayName("MoveDisable")
            .description("Disable when moving")
            .defaultValue(true)
            .build();

    private final BooleanSetting jumpDisable = BooleanSetting.builder()
            .id("surround_jump_disable")
            .displayName("JumpDisable")
            .description("Disable when jumping")
            .defaultValue(true)
            .build();

    private final SettingGroup renderSettings;

    private final BooleanSetting render = BooleanSetting.builder()
            .id("surround_render")
            .displayName("Render")
            .description("Renders placement positions")
            .defaultValue(true)
            .build();

    private final ColorSetting color = ColorSetting.builder()
            .id("surround_color")
            .displayName("Color")
            .description("Color of the render")
            .defaultValue(new Color(0, 255, 0, 75))
            .build();

    private final FloatSetting lineWidth = FloatSetting.builder()
            .id("surround_line_width")
            .displayName("Line Width")
            .description("Width of outline lines")
            .defaultValue(1.5f)
            .minValue(0.1f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    public Surround() {
        super("Surround");
        this.setCategory(Category.of("Combat"));
        this.setDescription("Surrounds the player with blocks to defend against endcrystals");

        generalSettings = SettingGroup.Builder.builder()
                .id("General")
                .displayName("General")
                .description("General surround settings")
                .build();

        rotationSettings = SettingGroup.Builder.builder()
                .id("surround_rotation")
                .displayName("Rotation")
                .description("Rotation settings")
                .build();

        checkSettings = SettingGroup.Builder.builder()
                .id("surround_check")
                .displayName("Check")
                .description("Check and validation settings")
                .build();

        generalSettings.addSetting(placeDelay);
        generalSettings.addSetting(blocksPer);
        generalSettings.addSetting(packetPlace);
        generalSettings.addSetting(onlyTick);
        generalSettings.addSetting(breakCrystal);
        generalSettings.addSetting(eatPause);
        generalSettings.addSetting(center);
        generalSettings.addSetting(extend);
        generalSettings.addSetting(onlySelf);
        generalSettings.addSetting(inventory);
        generalSettings.addSetting(enderChest);

        rotationSettings.addSetting(rotate);
        rotationSettings.addSetting(yawStep);
        rotationSettings.addSetting(steps);
        rotationSettings.addSetting(checkFov);
        rotationSettings.addSetting(fov);
        rotationSettings.addSetting(priority);

        checkSettings.addSetting(detectMining);
        checkSettings.addSetting(usingPause);
        checkSettings.addSetting(inAir);
        checkSettings.addSetting(moveDisable);
        checkSettings.addSetting(jumpDisable);

        // Add group to module
        this.addSetting(generalSettings);
        this.addSetting(rotationSettings);
        this.addSetting(checkSettings);

        // Register settings with SettingManager
        SettingManager.registerSetting(generalSettings);
        SettingManager.registerSetting(placeDelay);
        SettingManager.registerSetting(blocksPer);
        SettingManager.registerSetting(packetPlace);
        SettingManager.registerSetting(onlyTick);
        SettingManager.registerSetting(breakCrystal);
        SettingManager.registerSetting(eatPause);
        SettingManager.registerSetting(center);
        SettingManager.registerSetting(extend);
        SettingManager.registerSetting(onlySelf);
        SettingManager.registerSetting(inventory);
        SettingManager.registerSetting(enderChest);

        SettingManager.registerSetting(rotationSettings);
        SettingManager.registerSetting(rotate);
        SettingManager.registerSetting(yawStep);
        SettingManager.registerSetting(steps);
        SettingManager.registerSetting(checkFov);
        SettingManager.registerSetting(fov);
        SettingManager.registerSetting(priority);

        SettingManager.registerSetting(checkSettings);
        SettingManager.registerSetting(detectMining);
        SettingManager.registerSetting(usingPause);
        SettingManager.registerSetting(inAir);
        SettingManager.registerSetting(moveDisable);
        SettingManager.registerSetting(jumpDisable);

        renderSettings = SettingGroup.Builder.builder()
                .id("surround_render")
                .displayName("Render")
                .description("Render settings")
                .build();

        renderSettings.addSetting(render);
        renderSettings.addSetting(color);
        renderSettings.addSetting(lineWidth);

        this.addSetting(renderSettings);

        // Register render settings
        SettingManager.registerSetting(renderSettings);
        SettingManager.registerSetting(render);
        SettingManager.registerSetting(color);
        SettingManager.registerSetting(lineWidth);
    }

    double startX = 0;
    double startY = 0;
    double startZ = 0;
    int progress = 0;
    public Vec3d directionVec = null;
    private boolean shouldCenter = true;
    private final CacheTimer timer = new CacheTimer();

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(PlayerMoveEventListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(SendMovementPacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.AddListener(PlayerMoveEventListener.class, this);
        Payload.getInstance().eventManager.AddListener(SendMovementPacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(LookAtListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);

        if (nullCheck()) {
            if (moveDisable.getValue() || jumpDisable.getValue()) this.toggle();
            return;
        }
        startX = MC.player.getX();
        startY = MC.player.getY();
        startZ = MC.player.getZ();
        shouldCenter = true;
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        onUpdate();
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onGameLeft(GameLeftEvent event) {
        if (this.state.getValue()) {
            this.toggle();
        }
    }

    @Override
    public void onMove(PlayerMoveEvent event) {
        if (nullCheck() || !center.getValue() || MC.player.isGliding()) {
            return;
        }

        BlockPos blockPos = EntityUtil.getPlayerPos(true);
        if (MC.player.getX() - blockPos.getX() - 0.5 <= 0.2 && MC.player.getX() - blockPos.getX() - 0.5 >= -0.2 && MC.player.getZ() - blockPos.getZ() - 0.5 <= 0.2 && MC.player.getZ() - 0.5 - blockPos.getZ() >= -0.2) {
            if (shouldCenter && (MC.player.isOnGround() || MovementUtil.isMoving())) {
                event.setX(0);
                event.setZ(0);
                shouldCenter = false;
            }
        } else {
            if (shouldCenter) {
                Vec3d centerPos = EntityUtil.getPlayerPos(true).toCenterPos();
                float rotation = getRotationTo(MC.player.getPos(), centerPos).x;
                float yawRad = rotation / 180.0f * 3.1415927f;
                double dist = MC.player.getPos().distanceTo(new Vec3d(centerPos.x, MC.player.getY(), centerPos.z));
                double cappedSpeed = Math.min(0.2873, dist);
                double x = -(float) Math.sin(yawRad) * cappedSpeed;
                double z = (float) Math.cos(yawRad) * cappedSpeed;
                event.setX(x);
                event.setZ(z);
            }
        }
    }

    @Override
    public void onLook(LookAtEvent event) {
        if (directionVec != null && rotate.getValue() && yawStep.getValue()) {
            event.setTarget(directionVec, steps.getValue(), priority.getValue());
        }
    }

    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
        if (!onlyTick.getValue()) {
            onUpdate();
        }
    }

    @Override
    public void onSendMovementPacket(SendMovementPacketEvent.Post event) {

    }

    public void onUpdate() {
        if (!timer.passed(placeDelay.getValue())) return;
        directionVec = null;
        progress = 0;
        if (!MovementUtil.isMoving() && !MC.options.jumpKey.isPressed()) {
            startX = MC.player.getX();
            startY = MC.player.getY();
            startZ = MC.player.getZ();
        }
        BlockPos pos = EntityUtil.getPlayerPos(true);

        double distanceToStart = MathHelper.sqrt((float) MC.player.squaredDistanceTo(startX, startY, startZ));

        if (getBlock() == -1) {
            sendErrorMessage("Missing Obsidian or EChests");
            this.toggle();
            return;
        }
        if ((moveDisable.getValue() && distanceToStart > 1.0 || jumpDisable.getValue() && Math.abs(startY - MC.player.getY()) > 0.5)) {
            this.toggle();
            return;
        }
        if (usingPause.getValue() && MC.player.isUsingItem()) {
            return;
        }

        if (!inAir.getValue() && !MC.player.isOnGround()) return;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP) continue;
            BlockPos offsetPos = pos.offset(i);
            if (OtherBlockUtils.getPlaceSide(offsetPos) != null) {
                tryPlaceBlock(offsetPos);
            } else if (OtherBlockUtils.canReplace(offsetPos)) {
                tryPlaceBlock(getHelperPos(offsetPos));
            }
            if ((selfIntersectPos(offsetPos) || !onlySelf.getValue() && otherIntersectPos(offsetPos)) && extend.getValue()) {
                for (Direction i2 : Direction.values()) {
                    if (i2 == Direction.UP) continue;
                    BlockPos offsetPos2 = offsetPos.offset(i2);
                    if (selfIntersectPos(offsetPos2)|| !onlySelf.getValue() && otherIntersectPos(offsetPos2)) {
                        for (Direction i3 : Direction.values()) {
                            if (i3 == Direction.UP) continue;
                            tryPlaceBlock(offsetPos2);
                            BlockPos offsetPos3 = offsetPos2.offset(i3);
                            tryPlaceBlock(OtherBlockUtils.getPlaceSide(offsetPos3) != null || !OtherBlockUtils.canReplace(offsetPos3) ? offsetPos3 : getHelperPos(offsetPos3));
                        }
                    }
                    tryPlaceBlock(OtherBlockUtils.getPlaceSide(offsetPos2) != null || !OtherBlockUtils.canReplace(offsetPos2) ? offsetPos2 : getHelperPos(offsetPos2));
                }
            }
        }
    }
    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) return;
        if (detectMining.getValue() && Payload.getInstance().breakManager.isMining(pos)) return;
        if (!(progress < blocksPer.getValue())) return;
        int block = getBlock();
        if (block == -1) return;
        Direction side = OtherBlockUtils.getPlaceSide(pos);
        if (side == null) return;
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (!OtherBlockUtils.canPlace(pos, 6, true)) return;
        if (rotate.getValue()) {
            if (!faceVector(directionVec)) return;
        }
        if (breakCrystal.getValue()) {
            CombatUtil.attackCrystal(pos, rotate.getValue(), eatPause.getValue());
        } else if (OtherBlockUtils.hasEntity(pos, false)) return;
        int old = MC.player.getInventory().selectedSlot;
        doSwap(block);
        if (OtherBlockUtils.airPlace()) {
            OtherBlockUtils.placedPos.add(pos);
            OtherBlockUtils.clickBlock(pos, Direction.DOWN, false, Hand.MAIN_HAND, packetPlace.getValue());
        } else {
            OtherBlockUtils.placedPos.add(pos);
            OtherBlockUtils.clickBlock(pos.offset(side), side.getOpposite(), false, Hand.MAIN_HAND, packetPlace.getValue());
        }
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        if (rotate.getValue() && !yawStep.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
            Payload.getInstance().rotationManager.snapBack();
        }
        progress++;
        timer.reset();
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
    public static boolean selfIntersectPos(BlockPos pos) {
        return MC.player.getBoundingBox().intersects(new Box(pos));
    }
    public static boolean otherIntersectPos(BlockPos pos) {
        for (PlayerEntity player : MC.world.getPlayers()) {
            if (player.getBoundingBox().intersects(new Box(pos))) {
                return true;
            }
        }
        return false;
    }
    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, MC.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (inventory.getValue()) {
            if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
        } else {
            if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlock(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
        }
    }

    public BlockPos getHelperPos(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (detectMining.getValue() && Payload.getInstance().breakManager.isMining(pos.offset(i))) continue;
            if (!OtherBlockUtils.isStrictDirection(pos.offset(i), i.getOpposite())) continue;
            if (OtherBlockUtils.canPlace(pos.offset(i))) return pos.offset(i);
        }
        return null;
    }

    public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
        Vec3d vec3d = posTo.subtract(posFrom);
        return getRotationFromVec(vec3d);
    }

    private static Vec2f getRotationFromVec(Vec3d vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f((float) yaw, (float) pitch);
    }

    private static double normalizeAngle(double angleIn) {
        double angle = angleIn;
        if ((angle %= 360.0) >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent readPacketEvent) {
        if (this.state.getValue()) {
            this.toggle();
        }
    }

    @Override
    public void onRender(Render3DEvent event) {
        if (!render.getValue() || nullCheck()) return;

        // Get player position
        BlockPos pos = EntityUtil.getPlayerPos(true);

        // Render base surround positions
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;

            BlockPos offsetPos = pos.offset(dir);
            if (OtherBlockUtils.getPlaceSide(offsetPos) != null || OtherBlockUtils.canReplace(offsetPos)) {
                renderPos(event, offsetPos);
            }

            // Render extended positions if enabled
            if (extend.getValue()) {
                if (selfIntersectPos(offsetPos) || (!onlySelf.getValue() && otherIntersectPos(offsetPos))) {
                    for (Direction dir2 : Direction.values()) {
                        if (dir2 == Direction.UP) continue;

                        BlockPos offsetPos2 = offsetPos.offset(dir2);
                        if (selfIntersectPos(offsetPos2) || (!onlySelf.getValue() && otherIntersectPos(offsetPos2))) {
                            renderPos(event, offsetPos2);

                            for (Direction dir3 : Direction.values()) {
                                if (dir3 == Direction.UP) continue;
                                BlockPos offsetPos3 = offsetPos2.offset(dir3);
                                renderPos(event, offsetPos3);
                            }
                        }
                        renderPos(event, offsetPos2);
                    }
                }
            }
        }
    }

    private void renderPos(Render3DEvent event, BlockPos pos) {
        if (pos == null) return;
        if (!OtherBlockUtils.canPlace(pos, 6, true)) return;

        // Create box for the block position
        Box box = new Box(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
        );

        // Render filled box and outline
        Render3D.draw3DBox(event.GetMatrix(), event.getCamera(),box, color.getValue(), lineWidth.get());
    }
}
