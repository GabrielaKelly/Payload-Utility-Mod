
package net.payload.module.modules.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldEvents;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.player.InvUtils;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.render.Render3D;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SelfTrapTest extends Module implements EntitySpawnListener, EntityRemoveListener, GameLeftListener, TickListener, ReceivePacketListener, Render3DListener, PlayerMoveEventListener {

    private final FloatSetting placeRange = FloatSetting.builder()
            .id("selftrap_place_range")
            .displayName("Place Range")
            .description("The placement range for selftrap_ blocks")
            .defaultValue(4.0f)
            .minValue(0.0f)
            .maxValue(6.0f)
            .step(0.5f)
            .build();

    private final BooleanSetting head = BooleanSetting.builder()
            .id("selftrap_head")
            .displayName("HeadPlace")
            .description("Place blocks at head")
            .defaultValue(true)
            .build();

    private final BooleanSetting autoDisable = BooleanSetting.builder()
            .id("selftrap_autodisable")
            .displayName("AutoDisable")
            .description("Disables after placing the blocks")
            .defaultValue(false)
            .build();

    private final BooleanSetting rotate = BooleanSetting.builder()
            .id("selftrap_rotate")
            .displayName("Rotate")
            .description("Rotates to block before placing")
            .defaultValue(true)
            .build();

    private final BooleanSetting grim = BooleanSetting.builder()
            .id("selftrap_grim")
            .displayName("Grim")
            .description("Attacks crystals in the way of selftrap_")
            .defaultValue(true)
            .build();

    private final BooleanSetting strictDirection = BooleanSetting.builder()
            .id("selftrap_strictdirection")
            .displayName("Strict Direction")
            .description("Attacks crystals in the way of selftrap_")
            .defaultValue(true)
            .build();

    private final BooleanSetting attack = BooleanSetting.builder()
            .id("selftrap_attack")
            .displayName("Attack")
            .description("Attacks crystals in the way of selftrap_")
            .defaultValue(true)
            .build();

    private final BooleanSetting center = BooleanSetting.builder()
            .id("selftrap_center")
            .displayName("Center")
            .description("Centers the player before placing blocks")
            .defaultValue(false)
            .build();

    private final BooleanSetting extend = BooleanSetting.builder()
            .id("selftrap_extend")
            .displayName("Extend")
            .description("Extends selftrap_ if player is not centered")
            .defaultValue(false)
            .build();

    private final BooleanSetting support = BooleanSetting.builder()
            .id("selftrap_support")
            .displayName("Support")
            .description("Creates a floor for the selftrap_ if none exists")
            .defaultValue(true)
            .build();

    private final FloatSetting shiftTicks = FloatSetting.builder()
            .id("selftrap_shift_ticks")
            .displayName("Shift Ticks")
            .description("Number of blocks to place per tick")
            .defaultValue(2f)
            .minValue(1f)
            .maxValue(5f)
            .step(1f)
            .build();

    private final FloatSetting shiftDelayConfig = FloatSetting.builder()
            .id("selftrap_shift_delay")
            .displayName("Shift Delay")
            .description("Delay between each block placement interval")
            .defaultValue(3f)
            .minValue(1f)
            .maxValue(5f)
            .step(1f)
            .build();

    private final BooleanSetting render = BooleanSetting.builder()
            .id("selftrap_render")
            .displayName("Render")
            .description("Renders where selftrap_ is placing blocks")
            .defaultValue(true)
            .build();

    private final ColorSetting renderColor = ColorSetting.builder()
            .id("selftrap_render_color")
            .displayName("Render Color")
            .description("Color for selftrap_ rendering")
            .defaultValue(new Color(0, 255, 100, 30))
            .build();

    private final BooleanSetting inventorySwap = BooleanSetting.builder()
            .id("selftrap_invswap")
            .displayName("InvSwap")
            .description("Swap from inventory")
            .defaultValue(false)
            .build();

    private final FloatSetting priority = FloatSetting.builder()
            .id("selftrap_priority")
            .displayName("Priority")
            .description("Module execution priority")
            .defaultValue(25f)
            .minValue(0f)
            .maxValue(100f)
            .step(5f)
            .build();


    private final Map<BlockPos, Long> placementPositions = new HashMap<>();
    private List<BlockPos> selftrap_ = new ArrayList();
    private List<BlockPos> placements = new ArrayList();
    private int blocksPlaced;
    private int shiftDelay;
    private double prevY;
    private boolean shouldCenter = true;

    public SelfTrapTest() {
        super("SelfTrapTest");

        this.setCategory(Category.of("Combat"));
        this.setDescription("Self Traps you");

        // Register range and timing settings
        this.addSetting(placeRange);
        this.addSetting(shiftTicks);
        this.addSetting(shiftDelayConfig);
        this.addSetting(priority);

        // Register core functionality settings
        this.addSetting(head);
        this.addSetting(autoDisable);
        this.addSetting(rotate);
        this.addSetting(grim);
        this.addSetting(strictDirection);
        this.addSetting(attack);

        // Register placement behavior settings
        this.addSetting(center);
        this.addSetting(extend);
        this.addSetting(support);
        this.addSetting(inventorySwap);

        // Register render settings
        this.addSetting(render);
        this.addSetting(renderColor);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(PlayerMoveEventListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(EntitySpawnListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(EntityRemoveListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);

        this.placementPositions.clear();
        this.selftrap_.clear();
        this.placements.clear();
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.AddListener(PlayerMoveEventListener.class, this);
        Payload.getInstance().eventManager.AddListener(EntitySpawnListener.class, this);
        Payload.getInstance().eventManager.AddListener(EntityRemoveListener.class, this);
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);

        shouldCenter = true;

        if (nullCheck()) return;
        this.prevY = MC.player.getY();
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onDelete(EntityRemoveEvent event) {
        if (nullCheck()) return;

        if (event.getEntity() == MC.player && this.state.getValue()) {
            this.toggle();
        }
    }

    @Override
    public void onSpawn(EntitySpawnEvent readPacketEvent) {
        if (nullCheck()) return;

        Entity entity = readPacketEvent.getEntity();
        // Check if entity is an EndCrystal
        if (entity instanceof EndCrystalEntity crystal) {
            if ((Boolean)this.attack.getValue()) {
                // Check if crystal is at any of our selftrap_ing positions
                for (BlockPos blockPos : this.selftrap_) {
                    if (crystal.getBlockPos().equals(blockPos)) {
                        // Attack the crystal
                        MC.getNetworkHandler().sendPacket(
                                PlayerInteractEntityC2SPacket.attack(
                                        crystal,
                                        MC.player.isSneaking()
                                )
                        );
                        MC.getNetworkHandler().sendPacket(
                                new HandSwingC2SPacket(Hand.MAIN_HAND)
                        );
                        break;
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onGameLeft(GameLeftEvent event) {
        if (nullCheck()) return;

        if (this.state.getValue()) {
            this.toggle();
        }
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        if (nullCheck()) return;

        if (MC.player != null) {
            Packet<?> packet = readPacketEvent.getPacket();

            // Handle block update packets
            if (packet instanceof BlockUpdateS2CPacket blockUpdate) {
                BlockState state = blockUpdate.getState();
                BlockPos targetPos = blockUpdate.getPos();

                if (this.selftrap_.contains(targetPos) && state.isAir()) {
                    ++this.blocksPlaced;
                    // Schedule block placement on render thread
                    RenderSystem.recordRenderCall(() -> {
                        this.attackPlace(targetPos);
                    });
                }
            }

            else if (packet instanceof WorldEventS2CPacket worldEvent) {
                if (worldEvent.getEventId() == WorldEvents.BLOCK_BROKEN) {

                    BlockPos targetPos = new BlockPos(
                            worldEvent.getPos().getX(),
                            worldEvent.getPos().getY(),
                            worldEvent.getPos().getZ()
                    );

                    if (this.selftrap_.contains(targetPos)) {
                        ++this.blocksPlaced;
                        // Schedule block placement on render thread
                        RenderSystem.recordRenderCall(() -> {
                            this.attackPlace(targetPos);
                        });
                    }
                }
            }
        }
    }

    @Override
    public void onRender(Render3DEvent event) {
        if (nullCheck()) return;

        if ((Boolean)this.render.getValue()) {
            // Remove old positions (optional, for cleanup)
            long currentTime = System.currentTimeMillis();
            placementPositions.entrySet().removeIf(entry ->
                    currentTime - entry.getValue() > 1000); // Remove after 1 second

            // Render boxes for all placement positions
            for (BlockPos pos : placementPositions.keySet()) {
                // Create box for the block position
                Box box = new Box(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
                );

                // Render the box
                Render3D.draw3DBox(
                        event.GetMatrix(), event.getCamera(),
                        box,
                        renderColor.getValue(),
                        1f
                );
            }
        }
    }

    public static BlockPos getRoundedBlockPos(double x, double y, double z) {
        int flooredX = MathHelper.floor(x);
        int flooredY = (int)Math.round(y);  // Special case for Y
        int flooredZ = MathHelper.floor(z);
        return new BlockPos(flooredX, flooredY, flooredZ);
    }

    public Direction getInteractDirection(BlockPos blockPos, boolean grimMode, boolean strictDirection) {
        Set<Direction> validDirections =
                getPlaceDirectionsGrim(MC.player.getEyePos(), blockPos);
        //getPlaceDirectionsNCP(MC.player.getEyePos(), blockPos.toCenterPos());

        Direction interactDirection = null;

        for (Direction direction : Direction.values()) {
            BlockState state = MC.world.getBlockState(blockPos.offset(direction));

            if (!state.isAir() &&
                    state.isSolid() &&
                    (!strictDirection || validDirections.contains(direction.getOpposite()))) {
                interactDirection = direction;
                break;
            }
        }

        return interactDirection == null ? null : interactDirection.getOpposite();
    }

    public Set<Direction> getPlaceDirectionsGrim(Vec3d eyePos, BlockPos blockPos) {
        return getPlaceDirectionsGrim(
                eyePos.x, eyePos.y, eyePos.z,
                blockPos
        );
    }

    public Set<Direction> getPlaceDirectionsGrim(double x, double y, double z, BlockPos pos) {
        Set<Direction> dirs = new HashSet<>(6);
        Box combined = new Box(pos);

        // Create eye position box with specific offsets
        Box eyePositions = new Box(
                x, y + 0.4, z,
                x, y + 1.62, z
        ).expand(2.0E-4);

        // Check each direction based on box intersections
        if (eyePositions.minZ <= combined.minZ) {
            dirs.add(Direction.NORTH);
        }
        if (eyePositions.maxZ >= combined.maxZ) {
            dirs.add(Direction.SOUTH);
        }
        if (eyePositions.maxX >= combined.maxX) {
            dirs.add(Direction.EAST);
        }
        if (eyePositions.minX <= combined.minX) {
            dirs.add(Direction.WEST);
        }
        if (eyePositions.maxY >= combined.maxY) {
            dirs.add(Direction.UP);
        }
        if (eyePositions.minY <= combined.minY) {
            dirs.add(Direction.DOWN);
        }

        return dirs;
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        this.blocksPlaced = 0;
        // Check if player has moved vertically more than 0.5 blocks
        if ((Boolean)this.autoDisable.getValue() &&
                Math.abs(MC.player.getY() - this.prevY) > 0.5) {
            this.toggle();
        } else {
            // Get player's current position rounded to block coordinates
            BlockPos pos = getRoundedBlockPos(
                    MC.player.getX(),
                    MC.player.getY(),
                    MC.player.getZ()
            );

            // Handle placement delay
            if (this.shiftDelay < Math.round(this.shiftDelayConfig.getValue())) {
                ++this.shiftDelay;
            } else {
                // Get positions for self trap
                this.selftrap_ = this.getSelfTrapPositions(pos);
                // Filter for air blocks only
                this.placements = this.selftrap_.stream()
                        .filter(blockPos -> MC.world.getBlockState(blockPos).isAir())
                        .collect(Collectors.toList());

                if (!this.placements.isEmpty()) {
                    // Handle support blocks if enabled
                    if ((Boolean)this.support.getValue()) {
                        for (BlockPos block : new ArrayList<>(this.placements)) {
                            Direction direction = getInteractDirection(
                                    block,
                                    (Boolean)this.grim.getValue(),
                                    (Boolean)this.strictDirection.getValue()
                            );
                            if (direction == null) {
                                this.placements.add(block.down());
                            }
                        }
                    }

                    Collections.reverse(this.placements);
                    int shiftTicks = Math.round(this.shiftTicks.getValue());

                    // Place blocks up to shiftTicks limit
                    while (this.blocksPlaced < shiftTicks &&
                            !this.placements.isEmpty() &&
                            this.blocksPlaced < this.placements.size()) {
                        BlockPos block = this.placements.get(this.blocksPlaced);
                        ++this.blocksPlaced;
                        this.shiftDelay = 0;
                        this.attackPlace(block);
                    }
                }
            }
        }
    }

    private void attack(Entity entity) {
        MC.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, MC.player.isSneaking()));
        MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private void attackPlace(BlockPos targetPos) {
        int slot = getBlock();
        if (slot != -1) {
            this.attackPlace(targetPos, slot);
        }
    }

    private void doSwap(int slot) {
        if (Payload.getInstance().inventoryUtil != null) {
            if (inventorySwap.getValue()) {
                InventoryUtil.inventorySwap(slot, MC.player.getInventory().selectedSlot);
            } else {
                InventoryUtil.switchToSlot(slot);
            }
        }
    }

    private void attackPlace(BlockPos targetPos, int slot) {
        // Check if attack mode is enabled
        if ((Boolean)this.attack.getValue()) {
            // Get all end crystals at target position
            List<Entity> entities = MC.world.getOtherEntities(null,
                            new Box(targetPos))
                    .stream()
                    .filter(e -> e instanceof EndCrystalEntity)
                    .toList();

            // Attack all end crystals found
            for (Entity entity : entities) {
                this.attack(entity);
            }
        }

        Direction side = OtherBlockUtils.getPlaceSide(targetPos);
        int old = MC.player.getInventory().selectedSlot;
        doSwap(slot);
        // OtherBlockUtils.placeBlock(targetPos, this.rotate.getValue(), this.grimConfig.getValue());

        if (side != null) {
            OtherBlockUtils.clickBlock(targetPos.offset(side), side.getOpposite(), rotate.getValue(), Hand.MAIN_HAND, grim.getValue());
        }

        placementPositions.put(targetPos, System.currentTimeMillis());

        if (inventorySwap.getValue()) {
            doSwap(slot);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
    }

    private int getBlock() {
        if (Payload.getInstance().inventoryUtil != null) {
            if (inventorySwap.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            } else {
                return InventoryUtil.findBlock(Blocks.OBSIDIAN);
            }
        }
        sendErrorMessage("Error: Failed to find InventoryUtils");
        return 0;
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    public List<BlockPos> getSelfTrapPositions(BlockPos pos) {
        // Initialize position collection
        List<BlockPos> entities = new LinkedList<>();
        entities.add(pos);

        // Handle extended trap positions
        if (this.extend.getValue()) {
            for (Direction dir : Direction.values()) {
                if (dir.getAxis().isHorizontal()) {
                    BlockPos offsetPos = pos.offset(dir);
                    // Get entities that might interfere with trap

                    List<Entity> entitiesInBox = MC.world.getEntitiesByType(
                            TypeFilter.instanceOf(Entity.class),
                            new Box(offsetPos).expand(0.5),
                            entity -> {
                                // Streamlined entity validation
                                return entity != null
                                        && entity != MC.player
                                        && !this.isEntityBlockingTrap(entity)
                                        && entity.isAlive()
                                        && entity.getBoundingBox().intersects(new Box(offsetPos));
                            }
                    );

                    if (!entitiesInBox.isEmpty()) {
                        for (Entity entity : entitiesInBox) {
                            entities.addAll(this.getAllInBox(entity.getBoundingBox(), pos));
                        }
                    }
                }
            }
        }

        // Calculate trap block positions
        List<BlockPos> blocks = new CopyOnWriteArrayList<>();

        // Add horizontal selftrap_ing blocks
        for (BlockPos blockPos : entities) {
            for (Direction dir2 : Direction.values()) {
                if (dir2.getAxis().isHorizontal()) {
                    BlockPos offsetPos = blockPos.offset(dir2);
                    if (!entities.contains(offsetPos) && !blocks.contains(offsetPos)) {
                        double dist = MC.player.squaredDistanceTo(offsetPos.toCenterPos());
                        if (dist <= (this.placeRange).getValueSqr()) {
                            blocks.add(offsetPos);
                        }
                    }
                }
            }
        }

        // Add bottom blocks
        for (BlockPos blockPos : entities) {
            blocks.add(blockPos.down());
        }

        // Add top blocks
        for (BlockPos blockPos : blocks) {
            BlockPos topPos = blockPos.up();
            if (!entities.contains(blockPos) && !entities.contains(topPos)) {
                double distance = topPos.getSquaredDistance(
                        MC.player.getX(),
                        MC.player.getY(),
                        MC.player.getZ()
                );
                if (distance <= (this.placeRange).getValueSqr()) {
                    blocks.add(topPos);
                }
            }
        }

        // Sort blocks by distance (furthest first)
        blocks.sort(Comparator.comparingDouble(blockPos ->
                -MC.player.squaredDistanceTo(
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ()
                )
        ));

        // Add head block if enabled
        if (this.head.getValue()) {
            BlockPos headPos = getRoundedBlockPos(
                    MC.player.getX(),
                    MC.player.getY(),
                    MC.player.getZ()
            ).up(2);

            Direction direction = getInteractDirection(
                    headPos,
                    (Boolean)this.grim.getValue(),
                    (Boolean)this.strictDirection.getValue()
            );

            if (direction != null) {
                blocks.add(headPos);
            }
        }
        return blocks;
    }

    private boolean isEntityBlockingTrap(Entity entity) {
        return entity instanceof ItemFrameEntity ||
                entity instanceof ArmorStandEntity ||
                (entity instanceof EndCrystalEntity && (Boolean)this.attack.getValue());
    }

    public List<BlockPos> getAllInBox(Box box, BlockPos pos) {
        List<BlockPos> intersections = new ArrayList<>();

        // Calculate block positions within the box bounds
        for (int x = (int)Math.floor(box.minX); x < Math.ceil(box.maxX); ++x) {
            for (int z = (int)Math.floor(box.minZ); z < Math.ceil(box.maxZ); ++z) {
                intersections.add(new BlockPos(x, pos.getY(), z));
            }
        }

        return intersections;
    }

    @Override
    public void onMove(PlayerMoveEvent event) {
        if (nullCheck() || !center.getValue() || MC.player.isGliding()) {
            return;
        }

        if (shouldCenter) {
            if (this.center.getValue()) {
                double x = Math.floor(MC.player.getX()) + 0.5;
                double z = Math.floor(MC.player.getZ()) + 0.5;
                event.setX((x - MC.player.getX()) / 2.0);
                event.setZ((z - MC.player.getZ()) / 2.0);
                //Managers.MOVEMENT.setMotionXZ((x - MC.player.getX()) / 2.0, (z - MC.player.getZ()) / 2.0);
            }
            shouldCenter = false;
        }
    }
}