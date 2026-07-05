package net.payload.module.modules.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.chunk.Chunk;
import net.payload.Payload;
import net.payload.event.events.GameLeftEvent;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.GameLeftListener;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BlocksSetting;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.Dimension;
import net.payload.utils.entity.PlayerUtils;
import net.payload.utils.render.Render3D;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Search extends Module implements TickListener, Render3DListener, GameLeftListener, ReceivePacketListener {

    public BlocksSetting blocks = BlocksSetting.builder().id("search_blocks").displayName("Search Blocks")
            .description("what blocks to look for")
            .defaultValue(new HashSet<Block>(Lists.newArrayList(
                    Blocks.NETHER_PORTAL,
                    Blocks.END_PORTAL
                    )))
            .onUpdate(this::reset)
            .build();

    private ColorSetting foundcolor = ColorSetting.builder()
            .id("search_color")
            .displayName("Block Color")
            .description("color")
            .defaultValue(new Color(255,0,0,50))
            .build();

    private FloatSetting despawnDistance = FloatSetting.builder()
            .id("search_despawn_distance")
            .displayName("Despawn Distance")
            .description("Distance in chunks to stop rendering blocks")
            .defaultValue(8.0f)
            .minValue(1.0f)
            .maxValue(128.0f)
            .step(1f)
            .build();

    public FloatSetting lineThickness = FloatSetting.builder()
            .id("search_thickness")
            .displayName("Line Thickness")
            .defaultValue(0.5f)
            .minValue(0f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    private BooleanSetting breakUpdate = BooleanSetting.builder()
            .id("search_breakupdate")
            .displayName("Break Update")
            .description("Updates search when a block is broken")
            .defaultValue(true)
            .build();

    private BooleanSetting tracers = BooleanSetting.builder()
            .id("search_tracers")
            .displayName("Tracers")
            .description("funny lines")
            .defaultValue(true)
            .build();

    public Search()
    {
        super("Search");
        this.setCategory(Category.of("Render"));
        this.setDescription("Reveals where blocks of your choosing are");

        this.addSetting(blocks);
        this.addSetting(despawnDistance);
        this.addSetting(foundcolor);
        this.addSetting(lineThickness);
        this.addSetting(tracers);
    }

    private final Long2ObjectMap<Set<BlockPos>> foundBlocks = new Long2ObjectOpenHashMap<>();
    private final Set<Chunk> chunksToCheck = new ReferenceOpenHashSet<>();
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private final ExecutorService workerThread = Executors.newSingleThreadExecutor();
    private net.payload.utils.block.Dimension lastDimension;


    @Override
    public void onDisable() {
        chunksToCheck.clear();
        foundBlocks.clear();
       // workerThread.shutdown();
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
        chunksToCheck.clear();
        foundBlocks.clear();
    }

    public void reset(HashSet<Block> block) {
        chunksToCheck.clear();
        foundBlocks.clear();
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onGameLeft(GameLeftEvent event) {
        chunksToCheck.clear();
        foundBlocks.clear();
    }

    @Override
    public void onRender(Render3DEvent event) {
        if (nullCheck()) return;

        ChunkPos playerChunk = MC.player.getChunkPos();
        float maxDespawnDistance = despawnDistance.getValue();
        boolean viewBobbing = MC.options.getBobView().getValue();
        if (!MC.options.getPerspective().isFirstPerson()) return;

        Entity renderEntity = MC.getCameraEntity() == null ? MC.player : MC.getCameraEntity();

        if (!foundBlocks.isEmpty()) {
            foundBlocks.forEach((chunkLong, positions) -> {
                ChunkPos blockChunk = new ChunkPos(chunkLong);

                double chunkDistance = Math.sqrt(
                        Math.pow(playerChunk.x - blockChunk.x, 2) +
                                Math.pow(playerChunk.z - blockChunk.z, 2)
                );

                // Only render if within despawn distance
                if (chunkDistance <= maxDespawnDistance && positions != null) {
                    positions.forEach(pos -> {
                        Box box = new Box(pos);
                        Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), box, foundcolor.getValue(), lineThickness.getValue());

                        if (tracers.getValue()) {
                            MC.options.getBobView().setValue(false);

                            final Vec3d rotation = new Vec3d(0, 0, 75)
                                    .rotateX(-(float) Math.toRadians(renderEntity.getPitch()))
                                    .rotateY(-(float) Math.toRadians(renderEntity.getYaw()))
                                    .add(renderEntity.getEyePos());

                            Vec3d start = new Vec3d(rotation.x, rotation.y, rotation.z);
                            Vec3d end = pos.toCenterPos();

                            Render3D.drawLine3D(
                                    event.GetMatrix(),
                                    event.getCamera(),
                                    start,
                                    end,
                                    foundcolor.getValue(),
                                    lineThickness.getValue()
                            );
                        }
                    });
                }
            });
        }

        MC.options.getBobView().setValue(viewBobbing);
    }

    private void processChunks() {
        Iterator<Chunk> iterator = chunksToCheck.iterator();
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            iterator.remove();

            // Scan chunk for matching blocks
            Set<BlockPos> positions = new HashSet<>();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = chunk.getBottomY(); y < chunk.getTopYInclusive(); y++) {
                        blockPos.set(
                                chunk.getPos().x * 16 + x,
                                y,
                                chunk.getPos().z * 16 + z
                        );

                        Block block = MC.world.getBlockState(blockPos).getBlock();
                        if (blocks.getValue().contains(block)) {
                            positions.add(blockPos.toImmutable());
                        }
                    }
                }
            }

            if (!positions.isEmpty()) {
                foundBlocks.put(chunk.getPos().toLong(), positions);
            }
        }
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        // Check for dimension change
        Dimension currentDimension = PlayerUtils.getDimension();
        if (lastDimension != currentDimension) {
            lastDimension = currentDimension;
            chunksToCheck.clear();
            foundBlocks.clear();
        }

        // Queue new chunks for scanning
        int viewDistance = MC.options.getViewDistance().getValue();
        ChunkPos playerChunk = MC.player.getChunkPos();

        for (int x = -viewDistance; x <= viewDistance; x++) {
            for (int z = -viewDistance; z <= viewDistance; z++) {
                Chunk chunk = MC.world.getChunk(playerChunk.x + x, playerChunk.z + z);
                if (chunk != null && !chunk.isSectionEmpty(chunk.getBottomSectionCoord())) {
                    chunksToCheck.add(chunk);
                }
            }
        }

        // Process chunks in worker thread
        if (!chunksToCheck.isEmpty()) {
            workerThread.execute(this::processChunks);
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        if (breakUpdate.get()) {
            if (readPacketEvent.getPacket() instanceof WorldEventS2CPacket worldEvent) {
                // Check for block break event, then refresh.
                if (worldEvent.getEventId() == WorldEvents.BLOCK_BROKEN) {
                    reset(blocks.getValue());
                }
            }
        }
    }
}
