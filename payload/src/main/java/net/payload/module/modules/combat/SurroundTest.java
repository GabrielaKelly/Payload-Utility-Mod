package net.payload.module.modules.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.util.Hand;
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
import net.payload.utils.player.InteractUtils;
import net.payload.utils.player.InvUtils;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.render.Render3D;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.minecraft.util.math.MathHelper.EPSILON;

public class SurroundTest extends Module implements GameLeftListener, TickListener, EntitySpawnListener, ReceivePacketListener, Render3DListener, PlayerMoveEventListener {

    private final FloatSetting placeRange = FloatSetting.builder()
            .id("surround_place_range")
            .displayName("Place Range")
            .description("The placement range for surround blocks")
            .defaultValue(4.0f)
            .minValue(0.0f)
            .maxValue(6.0f)
            .step(0.5f)
            .build();

    private final BooleanSetting jumpDisable = BooleanSetting.builder()
            .id("surround_jumpdisable")
            .displayName("Jump Disable")
            .description("Attacks crystals in the way of surround")
            .defaultValue(true)
            .build();

    private final BooleanSetting grimConfig = BooleanSetting.builder()
            .id("surround_grim")
            .displayName("Grim")
            .description("Attacks crystals in the way of surround")
            .defaultValue(true)
            .build();

    private final BooleanSetting strictDirectionConfig = BooleanSetting.builder()
            .id("surround_strictdirection")
            .displayName("Strict Direction")
            .description("Attacks crystals in the way of surround")
            .defaultValue(true)
            .build();

    private final BooleanSetting rotate = BooleanSetting.builder()
            .id("surround_rotate")
            .displayName("Rotate")
            .description("Rotates to block before placing")
            .defaultValue(true)
            .build();

    private final BooleanSetting attack = BooleanSetting.builder()
            .id("surround_attack")
            .displayName("Attack")
            .description("Attacks crystals in the way of surround")
            .defaultValue(true)
            .build();

    private final BooleanSetting centerConfig = BooleanSetting.builder()
            .id("surround_center")
            .displayName("Center")
            .description("Centers the player before placing blocks")
            .defaultValue(false)
            .build();

    private final BooleanSetting extend = BooleanSetting.builder()
            .id("surround_extend")
            .displayName("Extend")
            .description("Extends surround if player is not centered")
            .defaultValue(false)
            .build();

    private final BooleanSetting supportConfig = BooleanSetting.builder()
            .id("surround_support")
            .displayName("Support")
            .description("Creates a floor for the surround if none exists")
            .defaultValue(true)
            .build();

    private final FloatSetting shiftTicks = FloatSetting.builder()
            .id("surround_shift_ticks")
            .displayName("Shift Ticks")
            .description("Number of blocks to place per tick")
            .defaultValue(2f)
            .minValue(1f)
            .maxValue(5f)
            .step(1f)
            .build();

    private final FloatSetting shiftDelayConfig = FloatSetting.builder()
            .id("surround_shift_delay")
            .displayName("Shift Delay")
            .description("Delay between each block placement interval")
            .defaultValue(1f)
            .minValue(0f)
            .maxValue(5f)
            .step(1f)
            .build();

    private final BooleanSetting enderChest = BooleanSetting.builder()
            .id("surround_ender_chest")
            .displayName("EnderChest")
            .defaultValue(false)
            .build();

    private final BooleanSetting inventorySwap = BooleanSetting.builder()
            .id("surround_invswap")
            .displayName("InvSwap")
            .description("Swap from inventory")
            .defaultValue(true)
            .build();

    private final BooleanSetting render = BooleanSetting.builder()
            .id("surround_render")
            .displayName("Render")
            .description("Renders where surround is placing blocks")
            .defaultValue(true)
            .build();

    private final ColorSetting renderColor = ColorSetting.builder()
            .id("surround_render_color")
            .displayName("Render Color")
            .description("Color for surround rendering")
            .defaultValue(new Color(0, 255, 100, 30))
            .build();

    private final FloatSetting priority = FloatSetting.builder()
            .id("surround_priority")
            .displayName("Priority")
            .description("Module execution priority")
            .defaultValue(25f)
            .minValue(0f)
            .maxValue(100f)
            .step(5f)
            .build();

    private final Map<BlockPos, Long> placementPositions = new HashMap<>();
    private List<BlockPos> surround = new ArrayList();
    private List<BlockPos> placements = new ArrayList();
    private int blocksPlaced;
    private int shiftDelay;
    private double prevY;
    private boolean selfTrapEnabled = false;
    private boolean shouldCenter = true;

    public SurroundTest() {
        super("SurroundTest");
        this.setCategory(Category.of("Combat"));
        this.setDescription("Surrounds the player with blocks.");

        this.addSetting(placeRange);
        this.addSetting(rotate);
        this.addSetting(grimConfig);
        this.addSetting(strictDirectionConfig);
        this.addSetting(attack);
        this.addSetting(centerConfig);
        this.addSetting(extend);
        this.addSetting(jumpDisable);
        this.addSetting(enderChest);
        this.addSetting(supportConfig);
        this.addSetting(shiftTicks);
        this.addSetting(shiftDelayConfig);
        this.addSetting(inventorySwap);
        this.addSetting(render);
        this.addSetting(renderColor);
        this.addSetting(priority);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(PlayerMoveEventListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(EntitySpawnListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.AddListener(PlayerMoveEventListener.class, this);
        Payload.getInstance().eventManager.AddListener(EntitySpawnListener.class, this);
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);

        shouldCenter = true;

        if (nullCheck()) return;
        this.prevY = MC.player.getY();
    }

    @Override
    public void onGameLeft(GameLeftEvent event) {
        if (this.state.getValue()) {
            this.toggle();
        }
    }

    @Override
    public void onMove(PlayerMoveEvent event) {
        if (nullCheck()) return;

        if (!centerConfig.getValue() || MC.player.isGliding()) {
            return;
        }

        if (shouldCenter) {
            if (this.centerConfig.getValue()) {
                double x = Math.floor(MC.player.getX()) + 0.5;
                double z = Math.floor(MC.player.getZ()) + 0.5;
                event.setX((x - MC.player.getX()) / 2.0);
                event.setZ((z - MC.player.getZ()) / 2.0);
                //Managers.MOVEMENT.setMotionXZ((x - MC.player.getX()) / 2.0, (z - MC.player.getZ()) / 2.0);
            }
            shouldCenter = false;
        }
    }

    @Override
    public void onSpawn(EntitySpawnEvent readPacketEvent) {
        if (nullCheck()) return;

        Entity entity = readPacketEvent.getEntity();
        if (entity instanceof EndCrystalEntity crystal) {
            if (this.attack.getValue() && !Payload.getInstance().moduleManager.selfTrapTest.state.getValue()) {
                for (BlockPos blockPos : this.surround) {
                    if (crystal.getBlockPos() == blockPos) {
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

    public static BlockPos getRoundedBlockPos(double x, double y, double z) {
        int flooredX = MathHelper.floor(x);
        int flooredY = (int)Math.round(y);  // Special case for Y
        int flooredZ = MathHelper.floor(z);
        return new BlockPos(flooredX, flooredY, flooredZ);
    }

    /**
     * Gets the valid interaction direction for block placement based on anti-cheat settings
     */
    public Direction getInteractDirection(BlockPos blockPos, boolean grimMode, boolean strictDirection) {
        Set<Direction> validDirections =
                InteractUtils.getPlaceDirectionsGrim(MC.player.getEyePos(), blockPos);
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

    private int getBlock() {
        if (Payload.getInstance().inventoryUtil != null && !nullCheck()) {
            if (inventorySwap.getValue()) {
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
        sendErrorMessage("Error: Failed to find InventoryUtils");
        return 0;
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        if (!Payload.getInstance().moduleManager.selfTrapTest.state.getValue()) {
            this.blocksPlaced = 0;

            if ((Boolean)this.jumpDisable.getValue() &&
                    Math.abs(MC.player.getY() - this.prevY) > 0.5) {
                this.toggle();
                return;
            }

            BlockPos pos = getRoundedBlockPos(
                    MC.player.getX(),
                    MC.player.getY(),
                    MC.player.getZ()
            );

            if (this.shiftDelay < Math.round(this.shiftDelayConfig.getValue())) {
                ++this.shiftDelay;
                return;
            }

            int slot = getBlock();
            if (slot != -1) {
                this.surround = this.getSurroundPositions(pos);
                this.placements = this.surround.stream()
                        .filter(blockPos -> MC.world.getBlockState(blockPos).isAir())
                        .collect(Collectors.toList());

                if (!this.placements.isEmpty()) {
                    if ((Boolean)this.supportConfig.getValue()) {
                        for (BlockPos block : new ArrayList<>(this.placements)) {
                            Direction direction = getInteractDirection(
                                    block,
                                    (Boolean)this.grimConfig.getValue(),
                                    (Boolean)this.strictDirectionConfig.getValue()
                            );
                            if (direction == null) {
                                this.placements.add(block.down());
                            }
                        }
                    }

                    Collections.reverse(this.placements);
                    int shiftTicks = Math.round(this.shiftTicks.getValue());

                    while (this.blocksPlaced < shiftTicks &&
                            !this.placements.isEmpty() &&
                            this.blocksPlaced < this.placements.size()) {
                        BlockPos block = this.placements.get(this.blocksPlaced);
                        ++this.blocksPlaced;
                        this.shiftDelay = 0;
                        this.attackPlace(block, slot);
                    }
                }
            }
        }
    }

    private void attack(Entity entity) {
        // Send swing animation packet
        MC.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, MC.player.isSneaking()));
        // Send hand swing packet
        MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private void attackPlace(BlockPos targetPos) {
        int slot = getBlock();
        if (slot != -1) {
            this.attackPlace(targetPos, slot);
        }
    }

    private void doSwap(int slot) {
        if (Payload.getInstance().inventoryUtil != null && !nullCheck()) {
            if (inventorySwap.getValue()) {
                InventoryUtil.inventorySwap(slot, MC.player.getInventory().selectedSlot);
            } else {
                InventoryUtil.switchToSlot(slot);
            }
        }
    }

    private void attackPlace(BlockPos targetPos, int slot) {
        if (nullCheck()) return;
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
            OtherBlockUtils.clickBlock(targetPos.offset(side), side.getOpposite(), rotate.getValue(), Hand.MAIN_HAND, grimConfig.getValue());
        }

        placementPositions.put(targetPos, System.currentTimeMillis());

        if (inventorySwap.getValue()) {
            doSwap(slot);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
    }




    public List<BlockPos> getSurroundPositions(BlockPos pos) {
        List<BlockPos> entities = this.getSurroundEntities(pos);
        List<BlockPos> blocks = new CopyOnWriteArrayList<>();

        for (BlockPos entityPos : entities) {
            for (Direction direction : Direction.values()) {
                if (direction.getAxis().isHorizontal()) {
                    BlockPos adjacentPos = entityPos.offset(direction.getOpposite());

                    if (!entities.contains(adjacentPos) && !blocks.contains(adjacentPos)) {
                        double dist = MC.player.squaredDistanceTo(adjacentPos.toCenterPos());
                        if (!(dist > (this.placeRange).getValueSqr())) {
                            blocks.add(adjacentPos);
                        }
                    }
                }
            }
        }

        for (BlockPos entityPos : entities) {
            if (entityPos != pos) {
                blocks.add(entityPos.down());
            }
        }

        return blocks;
    }

    public List<BlockPos> getSurroundEntities(Entity entity) {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(entity.getBlockPos());

        if ((Boolean)this.extend.getValue()) {
            for (Direction direction : Direction.values()) {
                if (direction.getAxis().isHorizontal()) {
                    entities.addAll(getAllInBox(
                            entity.getBoundingBox(),
                            entity.getBlockPos()
                    ));
                }
            }
        }

        return entities;
    }

    public static List<BlockPos> getAllInBox(Box box, BlockPos pos) {
        validateInputs(box, pos);

        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.ceil(box.maxX);
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.ceil(box.maxZ);
        int y = pos.getY();

        return IntStream.range(minX, maxX)
                .parallel()
                .mapToObj(x ->
                        IntStream.range(minZ, maxZ)
                                .mapToObj(z -> new BlockPos(x, y, z))
                                .collect(Collectors.toList())
                )
                .flatMap(List::stream)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    private static void validateInputs(Box box, BlockPos pos) {
        if (box == null || pos == null) {
            throw new IllegalArgumentException("Box and position cannot be null");
        }
        validateBox(box);
    }

    private static void validateBox(Box box) {
        if (!isValidBox(box)) {
            throw new IllegalArgumentException("Invalid box dimensions");
        }
    }

    private static boolean isValidBox(Box box) {
        return box.maxX - box.minX > -EPSILON &&
                box.maxY - box.minY > -EPSILON &&
                box.maxZ - box.minZ > -EPSILON;
    }


    public List<BlockPos> getSurroundEntities(BlockPos pos) {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(pos);

        if ((Boolean)this.extend.getValue()) {
            for (Direction dir : Direction.values()) {
                if (dir.getAxis().isHorizontal()) {
                    BlockPos adjacentPos = pos.offset(dir.getOpposite());
                    List<Entity> entitiesInBox = MC.world.getOtherEntities(null,
                                    new Box(adjacentPos))
                            .stream()
                            .filter(e -> !this.isEntityBlockingSurround(e))
                            .toList();

                    if (!entitiesInBox.isEmpty()) {
                        for (Entity entity : entitiesInBox) {
                            entities.addAll(getAllInBox(
                                    entity.getBoundingBox(),
                                    pos
                            ));
                        }
                    }
                }
            }
        }
        return entities;
    }

    public List<BlockPos> getEntitySurroundNoSupport(Entity entity) {
        List<BlockPos> entities = this.getSurroundEntities(entity);
        List<BlockPos> blocks = new CopyOnWriteArrayList<>();

        for (BlockPos entityPos : entities) {
            for (Direction dir : Direction.values()) {
                if (dir.getAxis().isHorizontal()) {
                    BlockPos adjacentPos = entityPos.offset(dir.getOpposite());
                    if (!entities.contains(adjacentPos) && !blocks.contains(adjacentPos)) {
                        double dist = MC.player.squaredDistanceTo(adjacentPos.toCenterPos());
                        if (!(dist > (this.placeRange).getValueSqr())) {
                            blocks.add(adjacentPos);
                        }
                    }
                }
            }
        }
        return blocks;
    }

    public boolean isEntityBlockingSurround(Entity entity) {
        return entity instanceof ItemEntity ||
                entity instanceof ExperienceOrbEntity ||
                (entity instanceof EndCrystalEntity && (Boolean)this.attack.getValue());
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        if (MC.player != null && !Payload.getInstance().moduleManager.selfTrapTest.state.getValue()) {
            Packet<?> packet = readPacketEvent.getPacket();

            // Handle BlockUpdatePacket
            if (packet instanceof BlockUpdateS2CPacket blockUpdate) {
                BlockState state = blockUpdate.getState();
                BlockPos targetPos = blockUpdate.getPos();

                if (this.surround.contains(targetPos) && state.isAir()) {
                    ++this.blocksPlaced;
                    RenderSystem.recordRenderCall(() -> {
                        this.attackPlace(targetPos);
                    });
                }
            }
            // Handle WorldEventS2CPacket
            else if (packet instanceof WorldEventS2CPacket worldEvent) {
                // Check for block break event
                if (worldEvent.getEventId() == WorldEvents.BLOCK_BROKEN) {

                    BlockPos targetPos = new BlockPos(
                            worldEvent.getPos().getX(),
                            worldEvent.getPos().getY(),
                            worldEvent.getPos().getZ()
                    );

                    if (this.surround.contains(targetPos)) {
                        ++this.blocksPlaced;
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
        if (!Payload.getInstance().moduleManager.selfTrapTest.state.getValue() && (Boolean)this.render.getValue()) {
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

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onToggle() {

    }
}
