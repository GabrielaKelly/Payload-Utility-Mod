package net.payload.module.modules.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.AcknowledgeChunksC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.*;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.GameLeftListener;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.render.Render3D;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewChunks extends Module implements TickListener, GameLeftListener, ReceivePacketListener, Render3DListener {
	private final SettingGroup detectionSettings;
	private final SettingGroup optimizationSettings;
	private final SettingGroup renderSettings;

	public enum DetectMode {
		Normal,
		IgnoreBlockExploit,
		BlockExploitMode
	}

		// Detection Settings
		public final BooleanSetting paletteExploit = BooleanSetting.builder()
				.id("cd_palette_exploit")
				.displayName("Palette Exploit")
				.defaultValue(true)
				.build();

		public final BooleanSetting beingUpdatedDetector = BooleanSetting.builder()
				.id("cd_being_updated")
				.displayName("Legacy Update Detector")
				.defaultValue(true)
				.build();

		// Old Chunk Detectors
		public final BooleanSetting overworldOldChunksDetector = BooleanSetting.builder()
				.id("cd_old_overworld")
				.displayName("Pre-1.17 Overworld")
				.defaultValue(true)
				.build();

		public final BooleanSetting netherOldChunks = BooleanSetting.builder()
				.id("cd_old_nether")
				.displayName("Pre-1.16 Nether")
				.defaultValue(true)
				.build();

		public final BooleanSetting endOldChunks = BooleanSetting.builder()
				.id("cd_old_end")
				.displayName("Pre-1.13 End")
				.defaultValue(true)
				.build();

		// Core Detection

		private final EnumSetting<DetectMode> detectMode = EnumSetting.<DetectMode>builder()
			.id("cd_detect_mode")
			.displayName("Detect Mode")
			.defaultValue(DetectMode.Normal)
			.build();

		public final BooleanSetting liquidExploit = BooleanSetting.builder()
				.id("cd_liquid_exploit")
				.displayName("Liquid Analysis")
				.defaultValue(false)
				.build();

		public final BooleanSetting blockUpdateExploit = BooleanSetting.builder()
				.id("cd_block_exploit")
				.displayName("Block Update Tracking")
				.defaultValue(false)
				.build();

		// Data Management

		public final BooleanSetting autoreload = BooleanSetting.builder()
			.id("cd_autoreload")
			.displayName("Reload Chunks")
			.defaultValue(false)
			.build();

		public final BooleanSetting remove = BooleanSetting.builder()
				.id("cd_remove_disable")
				.displayName("Disable Reset")
				.defaultValue(true)
				.build();

		public final BooleanSetting worldleaveremove = BooleanSetting.builder()
				.id("cd_remove_leave")
				.displayName("DIM Reset")
				.defaultValue(true)
				.build();

		// Render Settings

		public final BooleanSetting removerenderdist = BooleanSetting.builder()
			.id("cd_remove_renderdist")
			.displayName("Remove Outside Render Distance")
			.defaultValue(false)
			.build();

		public final FloatSetting renderDistance = FloatSetting.builder()
				.id("cd_render_dist")
				.displayName("Distance")
				.defaultValue(64f)
				.minValue(6f)
				.maxValue(1024f)
				.step(1f)
				.build();

		public final FloatSetting renderHeight = FloatSetting.builder()
				.id("cd_render_height")
				.displayName("Height")
				.defaultValue(0f)
				.minValue(-112f)
				.maxValue(319f)
				.step(1f)
				.build();

		// Color Settings
		public final ColorSetting newChunksColor = ColorSetting.builder()
				.id("cd_new_color")
				.displayName("New Chunks")
				.defaultValue(new Color(255, 0, 0, 60))
				.build();

		public final ColorSetting oldChunksColor = ColorSetting.builder()
				.id("cd_old_color")
				.displayName("Old Chunks")
				.defaultValue(new Color(0, 210, 0, 60))
				.build();

		public final ColorSetting tickexploitChunksColor = ColorSetting.builder()
			.id("cd_tickexploit_color")
			.displayName("Tick Exploit Chunks")
			.defaultValue(new Color(0, 0, 255, 60))
			.build();

		public final ColorSetting beingUpdatedOldChunksColor = ColorSetting.builder()
			.id("cd_updating_color")
			.displayName("Updating Chunks")
			.defaultValue(new Color(250, 0, 255, 60))
			.build();

		// Data Persistence
		public final BooleanSetting save = BooleanSetting.builder()
				.id("cd_save_data")
				.displayName("Save Data")
				.defaultValue(true)
				.build();

		public final BooleanSetting load = BooleanSetting.builder()
				.id("cd_load_data")
				.displayName("Load Data")
				.defaultValue(true)
				.build();

		public final FloatSetting removedelay = FloatSetting.builder()
			.id("cd_removedelay")
			.displayName("Remove Delay")
			.defaultValue(60f)
			.minValue(1f)
			.maxValue(300f)
			.step(10f)
			.build();


	private static final ExecutorService taskExecutor = Executors.newCachedThreadPool();
	private int deletewarningTicks=666;
	private int deletewarning=0;
	private String serverip;
	private String world;
	private final Set<ChunkPos> newChunks = Collections.synchronizedSet(new HashSet<>());
	private final Set<ChunkPos> oldChunks = Collections.synchronizedSet(new HashSet<>());
	private final Set<ChunkPos> beingUpdatedOldChunks = Collections.synchronizedSet(new HashSet<>());
	private final Set<ChunkPos> OldGenerationOldChunks = Collections.synchronizedSet(new HashSet<>());
	private final Set<ChunkPos> tickexploitChunks = Collections.synchronizedSet(new HashSet<>());
	private static final Direction[] searchDirs = new Direction[] { Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP };
	private int errticks=0;
	private int autoreloadticks=0;
	private int loadingticks=0;
	private boolean worldchange=false;
	private int justenabledsavedata=0;
	private boolean saveDataWasOn = false;
	MinecraftClient mc = MinecraftClient.getInstance();

	private static final Set<Block> ORE_BLOCKS = new HashSet<>();
	static {
		ORE_BLOCKS.add(Blocks.COAL_ORE);
		ORE_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
		ORE_BLOCKS.add(Blocks.COPPER_ORE);
		ORE_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
		ORE_BLOCKS.add(Blocks.IRON_ORE);
		ORE_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
		ORE_BLOCKS.add(Blocks.GOLD_ORE);
		ORE_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
		ORE_BLOCKS.add(Blocks.LAPIS_ORE);
		ORE_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
		ORE_BLOCKS.add(Blocks.DIAMOND_ORE);
		ORE_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
		ORE_BLOCKS.add(Blocks.REDSTONE_ORE);
		ORE_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
		ORE_BLOCKS.add(Blocks.EMERALD_ORE);
		ORE_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
	}
	private static final Set<Block> DEEPSLATE_BLOCKS = new HashSet<>();
	static {
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE);
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
		DEEPSLATE_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
	}
	private static final Set<Block> NEW_OVERWORLD_BLOCKS = new HashSet<>();
	static {
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.AMETHYST_BLOCK);
		NEW_OVERWORLD_BLOCKS.add(Blocks.BUDDING_AMETHYST);
		NEW_OVERWORLD_BLOCKS.add(Blocks.AZALEA);
		NEW_OVERWORLD_BLOCKS.add(Blocks.FLOWERING_AZALEA);
		NEW_OVERWORLD_BLOCKS.add(Blocks.BIG_DRIPLEAF);
		NEW_OVERWORLD_BLOCKS.add(Blocks.BIG_DRIPLEAF_STEM);
		NEW_OVERWORLD_BLOCKS.add(Blocks.SMALL_DRIPLEAF);
		NEW_OVERWORLD_BLOCKS.add(Blocks.CAVE_VINES);
		NEW_OVERWORLD_BLOCKS.add(Blocks.CAVE_VINES_PLANT);
		NEW_OVERWORLD_BLOCKS.add(Blocks.SPORE_BLOSSOM);
		NEW_OVERWORLD_BLOCKS.add(Blocks.COPPER_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.GLOW_LICHEN);
		NEW_OVERWORLD_BLOCKS.add(Blocks.RAW_COPPER_BLOCK);
		NEW_OVERWORLD_BLOCKS.add(Blocks.RAW_IRON_BLOCK);
		NEW_OVERWORLD_BLOCKS.add(Blocks.DRIPSTONE_BLOCK);
		NEW_OVERWORLD_BLOCKS.add(Blocks.MOSS_BLOCK);
		NEW_OVERWORLD_BLOCKS.add(Blocks.MOSS_CARPET);
		NEW_OVERWORLD_BLOCKS.add(Blocks.POINTED_DRIPSTONE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.SMOOTH_BASALT);
		NEW_OVERWORLD_BLOCKS.add(Blocks.TUFF);
		NEW_OVERWORLD_BLOCKS.add(Blocks.CALCITE);
		NEW_OVERWORLD_BLOCKS.add(Blocks.HANGING_ROOTS);
		NEW_OVERWORLD_BLOCKS.add(Blocks.ROOTED_DIRT);
		NEW_OVERWORLD_BLOCKS.add(Blocks.AZALEA_LEAVES);
		NEW_OVERWORLD_BLOCKS.add(Blocks.FLOWERING_AZALEA_LEAVES);
		NEW_OVERWORLD_BLOCKS.add(Blocks.POWDER_SNOW);
	}
	private static final Set<Block> NEW_NETHER_BLOCKS = new HashSet<>();
	static {
		NEW_NETHER_BLOCKS.add(Blocks.ANCIENT_DEBRIS);
		NEW_NETHER_BLOCKS.add(Blocks.BASALT);
		NEW_NETHER_BLOCKS.add(Blocks.BLACKSTONE);
		NEW_NETHER_BLOCKS.add(Blocks.GILDED_BLACKSTONE);
		NEW_NETHER_BLOCKS.add(Blocks.POLISHED_BLACKSTONE_BRICKS);
		NEW_NETHER_BLOCKS.add(Blocks.CRIMSON_STEM);
		NEW_NETHER_BLOCKS.add(Blocks.CRIMSON_NYLIUM);
		NEW_NETHER_BLOCKS.add(Blocks.NETHER_GOLD_ORE);
		NEW_NETHER_BLOCKS.add(Blocks.WARPED_NYLIUM);
		NEW_NETHER_BLOCKS.add(Blocks.WARPED_STEM);
		NEW_NETHER_BLOCKS.add(Blocks.TWISTING_VINES);
		NEW_NETHER_BLOCKS.add(Blocks.WEEPING_VINES);
		NEW_NETHER_BLOCKS.add(Blocks.BONE_BLOCK);
		NEW_NETHER_BLOCKS.add(Blocks.CHAIN);
		NEW_NETHER_BLOCKS.add(Blocks.OBSIDIAN);
		NEW_NETHER_BLOCKS.add(Blocks.CRYING_OBSIDIAN);
		NEW_NETHER_BLOCKS.add(Blocks.SOUL_SOIL);
		NEW_NETHER_BLOCKS.add(Blocks.SOUL_FIRE);
	}
	Set<Path> FILE_PATHS = new HashSet<>(Set.of(
			Paths.get("OldChunkCoords.txt"),
			Paths.get("BeingUpdatedChunkCoords.txt"),
			Paths.get("OldGenerationChunkCoords.txt"),
			Paths.get("NewChunkCoords.txt"),
			Paths.get("BlockExploitChunkCoords.txt")
	));

	public NewChunks() {
		super("NewChunks");
		this.setCategory(Category.of("World"));
		this.setDescription("Reveals whether or not a player has already visited your loaded chunks");
		detectionSettings = SettingGroup.Builder.builder()
				.id("detection")
				.displayName("Detection")
				.description("Chunk detection methods")
				.build();

		optimizationSettings = SettingGroup.Builder.builder()
				.id("optimization")
				.displayName("Optimizations")
				.description("Performance settings")
				.build();

		renderSettings = SettingGroup.Builder.builder()
				.id("render")
				.displayName("Render")
				.description("Visual settings")
				.build();

		detectionSettings.addSetting(liquidExploit);      // Liquid Analysis
		detectionSettings.addSetting(blockUpdateExploit); // BlockUpdate
		detectionSettings.addSetting(paletteExploit);     // Palette Exploit
		detectionSettings.addSetting(beingUpdatedDetector); // Legacy Update
		detectionSettings.addSetting(overworldOldChunksDetector); // Pre-1.17 OW
		detectionSettings.addSetting(netherOldChunks);    // Pre-1.16 Nether
		detectionSettings.addSetting(endOldChunks);       // Pre-1.13 End

		// Add settings to Optimizations group
		optimizationSettings.addSetting(remove);          // Disable Reset
		optimizationSettings.addSetting(worldleaveremove); // DIM Reset
		optimizationSettings.addSetting(autoreload);      // Reload Chunks

		// Add settings to Render group
		renderSettings.addSetting(newChunksColor);        // New Chunks
		renderSettings.addSetting(oldChunksColor);        // Old Chunks
		renderSettings.addSetting(tickexploitChunksColor); // Tick Exploiting Chunks
		renderSettings.addSetting(beingUpdatedOldChunksColor); // Updating Chunks

		this.addSetting(detectMode);
		this.addSetting(detectionSettings);
		this.addSetting(optimizationSettings);
		this.addSetting(renderSettings);
		this.addSetting(renderDistance);
		this.addSetting(renderHeight);
		this.addSetting(removedelay);
		this.addSetting(save);
		this.addSetting(load);
		this.addSetting(removerenderdist);

		// Register Detection settings
		SettingManager.registerSetting(liquidExploit);
		SettingManager.registerSetting(blockUpdateExploit);
		SettingManager.registerSetting(paletteExploit);
		SettingManager.registerSetting(beingUpdatedDetector);
		SettingManager.registerSetting(overworldOldChunksDetector);
		SettingManager.registerSetting(netherOldChunks);
		SettingManager.registerSetting(endOldChunks);

// Register Optimization settings
		SettingManager.registerSetting(remove);
		SettingManager.registerSetting(worldleaveremove);
		SettingManager.registerSetting(autoreload);

// Register Render settings
		SettingManager.registerSetting(newChunksColor);
		SettingManager.registerSetting(oldChunksColor);
		SettingManager.registerSetting(tickexploitChunksColor);
		SettingManager.registerSetting(beingUpdatedOldChunksColor);

// Register uncategorized settings
		SettingManager.registerSetting(detectMode);
		SettingManager.registerSetting(renderDistance);
		SettingManager.registerSetting(renderHeight);
		SettingManager.registerSetting(removedelay);
		SettingManager.registerSetting(save);
		SettingManager.registerSetting(load);
		SettingManager.registerSetting(removerenderdist);

// Register the setting groups themselves
		SettingManager.registerSetting(detectionSettings);
		SettingManager.registerSetting(optimizationSettings);
		SettingManager.registerSetting(renderSettings);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);

		autoreloadticks=0;
		loadingticks=0;
		worldchange=false;
		justenabledsavedata=0;
		if (remove.get()|autoreload.get()) {
			clearChunkData();
		}
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
		Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);


		if (save.get())saveDataWasOn = true;
		else if (!save.get())saveDataWasOn = false;
		if (autoreload.get()) {
			clearChunkData();
		}
		if (save.get() || load.get() && mc.world != null) {
			world= mc.world.getRegistryKey().getValue().toString().replace(':', '_');
			if (mc.isInSingleplayer()){
				String[] array = mc.getServer().getSavePath(WorldSavePath.ROOT).toString().replace(':', '_').split("/|\\\\");
				serverip=array[array.length-2];
			} else {
				serverip = mc.getCurrentServerEntry().address.replace(':', '_');
			}
		}
		if (save.get()){
			try {
				Files.createDirectories(Paths.get("payload", "NewChunks", serverip, world));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (save.get() || load.get()) {
			Path baseDir = Paths.get("payload", "NewChunks", serverip, world);

			for (Path fileName : FILE_PATHS) {
				Path fullPath = baseDir.resolve(fileName);
				try {
					Files.createDirectories(fullPath.getParent());
					if (Files.notExists(fullPath)) {
						Files.createFile(fullPath);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (load.get()){
			loadData();
		}
		autoreloadticks=0;
		loadingticks=0;
		worldchange=false;
		justenabledsavedata=0;
	}

	@Override
	public void onToggle() {
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (nullCheck()) return;

		world= mc.world.getRegistryKey().getValue().toString().replace(':', '_');

		if (deletewarningTicks<=100) deletewarningTicks++;
		else deletewarning=0;
		if (deletewarning>=2){
			if (mc.isInSingleplayer()){
				String[] array = mc.getServer().getSavePath(WorldSavePath.ROOT).toString().replace(':', '_').split("/|\\\\");
				serverip=array[array.length-2];
			} else {
				serverip = mc.getCurrentServerEntry().address.replace(':', '_');
			}
			clearChunkData();
			try {
				Files.deleteIfExists(Paths.get("payload", "NewChunks", serverip, world, "NewChunkCoords.txt"));
				Files.deleteIfExists(Paths.get("payload", "NewChunks", serverip, world, "OldChunkCoords.txt"));
				Files.deleteIfExists(Paths.get("payload", "NewChunks", serverip, world, "BeingUpdatedChunkCoords.txt"));
				Files.deleteIfExists(Paths.get("payload", "NewChunks", serverip, world, "OldGenerationChunkCoords.txt"));
				Files.deleteIfExists(Paths.get("payload", "NewChunks", serverip, world, "BlockExploitChunkCoords.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			sendErrorMessage("Chunk Data deleted for this Dimension.");
			deletewarning=0;
		}

		if (detectMode.get() == DetectMode.Normal && blockUpdateExploit.get()){
			if (errticks<6){
				errticks++;}
			if (errticks==5){
				sendErrorMessage("BlockExploitMode RECOMMENDED. Required to determine false positives from the Block Exploit from the OldChunks.");
			}
		} else errticks=0;

		if (load.get()){
			if (loadingticks<1){
				loadData();
				loadingticks++;
			}
		} else if (!load.get()){
			loadingticks=0;
		}

		if (autoreload.get()) {
			autoreloadticks++;
			if (autoreloadticks==removedelay.get()*20){
				clearChunkData();
				if (load.get()){
					loadData();
				}
			} else if (autoreloadticks>=removedelay.get()*20){
				autoreloadticks=0;
			}
		}

		if (load.get() && worldchange){
			if (worldleaveremove.get()){
				clearChunkData();
			}
			loadData();
			worldchange=false;
		}

		if (!save.get())saveDataWasOn = false;
		if (save.get() && justenabledsavedata<=2 && !saveDataWasOn){
			justenabledsavedata++;
			if (justenabledsavedata == 1){
				synchronized (newChunks) {
					for (ChunkPos chunk : newChunks){
						saveData(Paths.get("NewChunkCoords.txt"), chunk);
					}
				}
				synchronized (OldGenerationOldChunks) {
					for (ChunkPos chunk : OldGenerationOldChunks){
						saveData(Paths.get("OldGenerationChunkCoords.txt"), chunk);
					}
				}
				synchronized (beingUpdatedOldChunks) {
					for (ChunkPos chunk : beingUpdatedOldChunks){
						saveData(Paths.get("BeingUpdatedChunkCoords.txt"), chunk);
					}
				}
				synchronized (oldChunks) {
					for (ChunkPos chunk : oldChunks){
						saveData(Paths.get("OldChunkCoords.txt"), chunk);
					}
				}
				synchronized (tickexploitChunks) {
					for (ChunkPos chunk : tickexploitChunks){
						saveData(Paths.get("BlockExploitChunkCoords.txt"), chunk);
					}
				}
			}
		}

		if (removerenderdist.get())removeChunksOutsideRenderDistance();
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	@Override
	public void onGameLeft(GameLeftEvent event) {
		if (worldleaveremove.get()) {
			clearChunkData();
		}
	}

	@Override
	public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
		if (nullCheck()) return;

		if (readPacketEvent.getPacket() instanceof AcknowledgeChunksC2SPacket)return;
		if (!(readPacketEvent.getPacket() instanceof AcknowledgeChunksC2SPacket) && readPacketEvent.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet && liquidExploit.get()) {

			packet.visitUpdates((pos, state) -> {
				ChunkPos chunkPos = new ChunkPos(pos);
				if (!state.getFluidState().isEmpty() && !state.getFluidState().isStill()) {
					for (Direction dir: searchDirs) {
						try {
							if (mc.world != null && mc.world.getBlockState(pos.offset(dir)).getFluidState().isStill() && (!OldGenerationOldChunks.contains(chunkPos) && !beingUpdatedOldChunks.contains(chunkPos) && !newChunks.contains(chunkPos) && !oldChunks.contains(chunkPos))) {
								tickexploitChunks.remove(chunkPos);
								newChunks.add(chunkPos);
								if (save.get()){
									saveData(Paths.get("NewChunkCoords.txt"), chunkPos);
								}
								return;
							}
						} catch (Exception e) {}
					}
				}
			});
		}
		else if (!(readPacketEvent.getPacket() instanceof AcknowledgeChunksC2SPacket) && readPacketEvent.getPacket() instanceof BlockUpdateS2CPacket packet) {
			ChunkPos chunkPos = new ChunkPos(packet.getPos());
			if (blockUpdateExploit.get()){
				try {
					if (!OldGenerationOldChunks.contains(chunkPos) && !beingUpdatedOldChunks.contains(chunkPos) && !tickexploitChunks.contains(chunkPos) && !oldChunks.contains(chunkPos) && !newChunks.contains(chunkPos)){
						tickexploitChunks.add(chunkPos);
						if (save.get()){
							saveData(Paths.get("BlockExploitChunkCoords.txt"), chunkPos);
						}
					}
				}
				catch (Exception e){}
			}
			if (!packet.getState().getFluidState().isEmpty() && !packet.getState().getFluidState().isStill() && liquidExploit.get()) {
				for (Direction dir: searchDirs) {
					try {
						if (mc.world != null && mc.world.getBlockState(packet.getPos().offset(dir)).getFluidState().isStill() && (!OldGenerationOldChunks.contains(chunkPos) && !beingUpdatedOldChunks.contains(chunkPos) && !newChunks.contains(chunkPos) && !oldChunks.contains(chunkPos))) {
							tickexploitChunks.remove(chunkPos);
							newChunks.add(chunkPos);
							if (save.get()){
								saveData(Paths.get("NewChunkCoords.txt"), chunkPos);
							}
							return;
						}
					} catch (Exception e) {}
				}
			}
		}
		else if (!(readPacketEvent.getPacket() instanceof AcknowledgeChunksC2SPacket) && !(readPacketEvent.getPacket() instanceof PlayerMoveC2SPacket) && readPacketEvent.getPacket() instanceof ChunkDataS2CPacket packet && mc.world != null) {
			ChunkPos oldpos = new ChunkPos(packet.getChunkX(), packet.getChunkZ());

			if (mc.world.getChunkManager().getChunk(packet.getChunkX(), packet.getChunkZ()) == null) {
				WorldChunk chunk = new WorldChunk(mc.world, oldpos);
				try {
					CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
						chunk.loadFromPacket(packet.getChunkData().getSectionsDataBuf(), new NbtCompound(),
								packet.getChunkData().getBlockEntities(packet.getChunkX(), packet.getChunkZ()));
					}, taskExecutor);
					future.join();
				} catch (CompletionException e) {}

				boolean isNewChunk = false;
				boolean isOldGeneration = false;
				boolean chunkIsBeingUpdated = false;
				boolean foundAnyOre = false;
				boolean isNewOverworldGeneration = false;
				boolean isNewNetherGeneration = false;
				ChunkSection[] sections = chunk.getSectionArray();

				if (overworldOldChunksDetector.get() && mc.world.getRegistryKey() == World.OVERWORLD && chunk.getStatus().isAtLeast(ChunkStatus.FULL) && !chunk.isEmpty()) {
					for (int i = 0; i < 17; i++) {
						ChunkSection section = sections[i];
						if (section != null && !section.isEmpty()) {
							for (int x = 0; x < 16; x++) {
								for (int y = 0; y < 16; y++) {
									for (int z = 0; z < 16; z++) {
										if (!foundAnyOre && ORE_BLOCKS.contains(section.getBlockState(x, y, z).getBlock())) foundAnyOre = true;
										if (((y >= 5 && i == 4) || i > 4) && !isNewOverworldGeneration && (NEW_OVERWORLD_BLOCKS.contains(section.getBlockState(x, y, z).getBlock()) || DEEPSLATE_BLOCKS.contains(section.getBlockState(x, y, z).getBlock()))) {
											isNewOverworldGeneration = true;
											break;
										}
									}
								}
							}
						}
					}
					if (foundAnyOre && !isOldGeneration && !isNewOverworldGeneration) isOldGeneration = true;
				}

				if (netherOldChunks.get() && mc.world.getRegistryKey() == World.NETHER && chunk.getStatus().isAtLeast(ChunkStatus.FULL) && !chunk.isEmpty()) {
					for (int i = 0; i < 8; i++) {
						ChunkSection section = sections[i];
						if (section != null && !section.isEmpty()) {
							for (int x = 0; x < 16; x++) {
								for (int y = 0; y < 16; y++) {
									for (int z = 0; z < 16; z++) {
										if (!isNewNetherGeneration && NEW_NETHER_BLOCKS.contains(section.getBlockState(x, y, z).getBlock())) {
											isNewNetherGeneration = true;
											break;
										}
									}
								}
							}
						}
					}
					if (!isOldGeneration && !isNewNetherGeneration) isOldGeneration = true;
				}

				if (endOldChunks.get() && mc.world.getRegistryKey() == World.END && chunk.getStatus().isAtLeast(ChunkStatus.FULL) && !chunk.isEmpty()) {
					ChunkSection section = chunk.getSection(0);
					var biomesContainer = section.getBiomeContainer();

					if (biomesContainer instanceof PalettedContainer<RegistryEntry<Biome>> biomesPaletteContainer) {
						Palette<RegistryEntry<Biome>> biomePalette = biomesPaletteContainer.data.palette();
						for (int i = 0; i < biomePalette.getSize(); i++) {
							if (biomePalette.get(i).getKey().get() == BiomeKeys.THE_END) {
								isOldGeneration = true;
								break;
							}
						}
					}
				}

				if (paletteExploit.get()) {
					boolean firstchunkappearsnew = false;
					int loops = 0;
					int newChunkQuantifier = 0;
					int oldChunkQuantifier = 0;
					try {
						for (ChunkSection section : sections) {
							if (section != null) {
								int isNewSection = 0;
								int isBeingUpdatedSection = 0;

								if (!section.isEmpty()) {
									var blockStatesContainer = section.getBlockStateContainer();
									Palette<BlockState> blockStatePalette = blockStatesContainer.data.palette();
									int blockPaletteLength = blockStatePalette.getSize();

									if (blockStatePalette instanceof BiMapPalette<BlockState>){
										Set<BlockState> bstates = new HashSet<>();
										for (int x = 0; x < 16; x++) {
											for (int y = 0; y < 16; y++) {
												for (int z = 0; z < 16; z++) {
													bstates.add(blockStatesContainer.get(x, y, z));
												}
											}
										}
										int bstatesSize = bstates.size();
										if (bstatesSize <= 1) bstatesSize = blockPaletteLength;
										if (bstatesSize < blockPaletteLength) {
											isNewSection = 2;
										}
									}

									for (int i2 = 0; i2 < blockPaletteLength; i2++) {
										BlockState blockPaletteEntry = blockStatePalette.get(i2);
										if (i2 == 0 && loops == 0 && blockPaletteEntry.getBlock() == Blocks.AIR && mc.world.getRegistryKey() != World.END)
											firstchunkappearsnew = true;
										if (i2 == 0 && blockPaletteEntry.getBlock() == Blocks.AIR && mc.world.getRegistryKey() != World.NETHER && mc.world.getRegistryKey() != World.END)
											isNewSection++;
										if (i2 == 1 && (blockPaletteEntry.getBlock() == Blocks.WATER || blockPaletteEntry.getBlock() == Blocks.STONE || blockPaletteEntry.getBlock() == Blocks.GRASS_BLOCK || blockPaletteEntry.getBlock() == Blocks.SNOW_BLOCK) && mc.world.getRegistryKey() != World.NETHER && mc.world.getRegistryKey() != World.END)
											isNewSection++;
										if (i2 == 2 && (blockPaletteEntry.getBlock() == Blocks.SNOW_BLOCK || blockPaletteEntry.getBlock() == Blocks.DIRT || blockPaletteEntry.getBlock() == Blocks.POWDER_SNOW) && mc.world.getRegistryKey() != World.NETHER && mc.world.getRegistryKey() != World.END)
											isNewSection++;
										if (loops == 4 && blockPaletteEntry.getBlock() == Blocks.BEDROCK && mc.world.getRegistryKey() != World.NETHER && mc.world.getRegistryKey() != World.END) {
											if (!chunkIsBeingUpdated && beingUpdatedDetector.get())
												chunkIsBeingUpdated = true;
										}
										if (blockPaletteEntry.getBlock() == Blocks.AIR && (mc.world.getRegistryKey() == World.NETHER || mc.world.getRegistryKey() == World.END))
											isBeingUpdatedSection++;
									}
									if (isBeingUpdatedSection >= 2) oldChunkQuantifier++;
									if (isNewSection >= 2) newChunkQuantifier++;
								}
								if (mc.world.getRegistryKey() == World.END) {
									var biomesContainer = section.getBiomeContainer();
									if (biomesContainer instanceof PalettedContainer<RegistryEntry<Biome>> biomesPaletteContainer) {
										Palette<RegistryEntry<Biome>> biomePalette = biomesPaletteContainer.data.palette();
										for (int i3 = 0; i3 < biomePalette.getSize(); i3++) {
											if (i3 == 0 && biomePalette.get(i3).getKey().get() == BiomeKeys.PLAINS) isNewChunk = true;
											if (!isNewChunk && i3 == 0 && biomePalette.get(i3).getKey().get() != BiomeKeys.THE_END) isNewChunk = false;
										}
									}
								}
								if (!section.isEmpty())loops++;
							}
						}

						if (loops > 0) {
							if (beingUpdatedDetector.get() && (mc.world.getRegistryKey() == World.NETHER || mc.world.getRegistryKey() == World.END)){
								double oldpercentage = ((double) oldChunkQuantifier / loops) * 100;
								if (oldpercentage >= 25) chunkIsBeingUpdated = true;
							}
							else if (mc.world.getRegistryKey() != World.NETHER && mc.world.getRegistryKey() != World.END){
								double percentage = ((double) newChunkQuantifier / loops) * 100;
								if (percentage >= 51) isNewChunk = true;
							}
						}
					} catch (Exception e) {
						if (beingUpdatedDetector.get() && (mc.world.getRegistryKey() == World.NETHER || mc.world.getRegistryKey() == World.END)){
							double oldpercentage = ((double) oldChunkQuantifier / loops) * 100;
							if (oldpercentage >= 25) chunkIsBeingUpdated = true;
						}
						else if (mc.world.getRegistryKey() != World.NETHER && mc.world.getRegistryKey() != World.END){
							double percentage = ((double) newChunkQuantifier / loops) * 100;
							if (percentage >= 51) isNewChunk = true;
						}
					}

					if (firstchunkappearsnew) isNewChunk = true;
					boolean bewlian = (mc.world.getRegistryKey() == World.END) ? isNewChunk : !isOldGeneration;
					if (isNewChunk && !chunkIsBeingUpdated && bewlian) {
						try {
							if (!OldGenerationOldChunks.contains(oldpos) && !beingUpdatedOldChunks.contains(oldpos) && !tickexploitChunks.contains(oldpos) && !oldChunks.contains(oldpos) && !newChunks.contains(oldpos)) {
								newChunks.add(oldpos);
								if (save.get()) {
									saveData(Paths.get("NewChunkCoords.txt"), oldpos);
								}
								return;
							}
						} catch (Exception e) {
						}
					}
					else if (!isNewChunk && !chunkIsBeingUpdated && isOldGeneration) {
						try {
							if (!OldGenerationOldChunks.contains(oldpos) && !beingUpdatedOldChunks.contains(oldpos) && !oldChunks.contains(oldpos) && !tickexploitChunks.contains(oldpos) && !newChunks.contains(oldpos)) {
								OldGenerationOldChunks.add(oldpos);
								if (save.get()){
									saveData(Paths.get("OldGenerationChunkCoords.txt"), oldpos);
								}
								return;
							}
						} catch (Exception e) {
						}
					}
					else if (chunkIsBeingUpdated) {
						try {
							if (!OldGenerationOldChunks.contains(oldpos) && !beingUpdatedOldChunks.contains(oldpos) && !oldChunks.contains(oldpos) && !tickexploitChunks.contains(oldpos) && !newChunks.contains(oldpos)) {
								beingUpdatedOldChunks.add(oldpos);
								if (save.get()){
									saveData(Paths.get("BeingUpdatedChunkCoords.txt"), oldpos);
								}
								return;
							}
						} catch (Exception e) {
						}
					}
					else if (!isNewChunk) {
						try {
							if (!OldGenerationOldChunks.contains(oldpos) && !beingUpdatedOldChunks.contains(oldpos) && !tickexploitChunks.contains(oldpos) && !oldChunks.contains(oldpos) && !newChunks.contains(oldpos)) {
								oldChunks.add(oldpos);
								if (save.get()) {
									saveData(Paths.get("OldChunkCoords.txt"), oldpos);
								}
								return;
							}
						} catch (Exception e) {
						}
					}
				}
				if (liquidExploit.get()) {
					for (int x = 0; x < 16; x++) {
						for (int y = mc.world.getBottomY(); y < mc.world.getTopYInclusive(); y++) {
							for (int z = 0; z < 16; z++) {
								FluidState fluid = chunk.getFluidState(x, y, z);
								try {
									if (!OldGenerationOldChunks.contains(oldpos) && !beingUpdatedOldChunks.contains(oldpos) && !oldChunks.contains(oldpos) && !tickexploitChunks.contains(oldpos) && !newChunks.contains(oldpos) && !fluid.isEmpty() && !fluid.isStill()) {
										oldChunks.add(oldpos);
										if (save.get()){
											saveData(Paths.get("OldChunkCoords.txt"), oldpos);
										}
										return;
									}
								} catch (Exception e) {
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onRender(Render3DEvent event) {
		if (mc.player == null) return;
		BlockPos playerPos = new BlockPos(mc.player.getBlockX(), Math.round(renderHeight.get()), mc.player.getBlockZ());
		if (newChunksColor.get().getAlphaInt() > 5) {
			synchronized (newChunks) {
				for (ChunkPos c : newChunks) {
					if (c != null && playerPos.isWithinDistance(new BlockPos(c.getCenterX(), Math.round(renderHeight.get()), c.getCenterZ()), renderDistance.get()*16)) {
						render(new Box(new Vec3d(c.getStartPos().getX(), c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()), new Vec3d(c.getStartPos().getX()+16, c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()+16)), newChunksColor.get(), event);
					}
				}
			}
		}
		if (tickexploitChunksColor.get().getAlphaInt() > 5) {
			synchronized (tickexploitChunks) {
				for (ChunkPos c : tickexploitChunks) {
					if (c != null && playerPos.isWithinDistance(new BlockPos(c.getCenterX(), Math.round(renderHeight.get()), c.getCenterZ()), renderDistance.get()*16)) {
						if (detectMode.get()== DetectMode.BlockExploitMode && blockUpdateExploit.get()) {
							render(new Box(new Vec3d(c.getStartPos().getX(), c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()), new Vec3d(c.getStartPos().getX()+16, c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()+16)), tickexploitChunksColor.get(), event);
						} else if ((detectMode.get()== DetectMode.Normal) && blockUpdateExploit.get()) {
							render(new Box(new Vec3d(c.getStartPos().getX(), c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()), new Vec3d(c.getStartPos().getX()+16, c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()+16)), newChunksColor.get(), event);
						} else if ((detectMode.get()== DetectMode.IgnoreBlockExploit) && blockUpdateExploit.get()) {
							render(new Box(new Vec3d(c.getStartPos().getX(), c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()), new Vec3d(c.getStartPos().getX()+16, c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()+16)), oldChunksColor.get(), event);
						} else if ((detectMode.get()== DetectMode.BlockExploitMode | detectMode.get()== DetectMode.Normal | detectMode.get()== DetectMode.IgnoreBlockExploit) && !blockUpdateExploit.get()) {
							render(new Box(new Vec3d(c.getStartPos().getX(), c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()), new Vec3d(c.getStartPos().getX()+16, c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()+16)), oldChunksColor.get(), event);
						}
					}
				}
			}
		}
		if (oldChunksColor.get().getAlphaInt() > 5){
			synchronized (oldChunks) {
				for (ChunkPos c : oldChunks) {
					if (c != null && playerPos.isWithinDistance(new BlockPos(c.getCenterX(), Math.round(renderHeight.get()), c.getCenterZ()), renderDistance.get()*16)) {
						render(new Box(new Vec3d(c.getStartPos().getX(), c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()), new Vec3d(c.getStartPos().getX()+16, c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()+16)), oldChunksColor.get(), event);
					}
				}
			}
		}
		if (beingUpdatedOldChunksColor.get().getAlphaInt() > 5){
			synchronized (beingUpdatedOldChunks) {
				for (ChunkPos c : beingUpdatedOldChunks) {
					if (c != null && playerPos.isWithinDistance(new BlockPos(c.getCenterX(), Math.round(renderHeight.get()), c.getCenterZ()), renderDistance.get()*16)) {
						render(new Box(new Vec3d(c.getStartPos().getX(), c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()), new Vec3d(c.getStartPos().getX()+16, c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()+16)), beingUpdatedOldChunksColor.get(), event);
					}
				}
			}
		}
		if (oldChunksColor.get().getAlphaInt() > 5){
			synchronized (OldGenerationOldChunks) {
				for (ChunkPos c : OldGenerationOldChunks) {
					if (c != null && playerPos.isWithinDistance(new BlockPos(c.getCenterX(), Math.round(renderHeight.get()), c.getCenterZ()), renderDistance.get()*16)) {
						render(new Box(new Vec3d(c.getStartPos().getX(), c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()), new Vec3d(c.getStartPos().getX()+16, c.getStartPos().getY()+renderHeight.get(), c.getStartPos().getZ()+16)), oldChunksColor.get(), event);
					}
				}
			}
		}
	}

	private void render(Box box, Color sides, Render3DEvent event) {

		Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), box, sides, 0f);
	}

	private void clearChunkData() {
		newChunks.clear();
		oldChunks.clear();
		beingUpdatedOldChunks.clear();
		OldGenerationOldChunks.clear();
		tickexploitChunks.clear();
	}

	private void loadData() {
		loadChunkData(Paths.get("BlockExploitChunkCoords.txt"), tickexploitChunks);
		loadChunkData(Paths.get("OldChunkCoords.txt"), oldChunks);
		loadChunkData(Paths.get("NewChunkCoords.txt"), newChunks);
		loadChunkData(Paths.get("BeingUpdatedChunkCoords.txt"), beingUpdatedOldChunks);
		loadChunkData(Paths.get("OldGenerationChunkCoords.txt"), OldGenerationOldChunks);
	}

	private void loadChunkData(Path savedDataLocation, Set<ChunkPos> chunkSet) {
		try {
			Path filePath = Paths.get("payload/NewChunks", serverip, world).resolve(savedDataLocation);
			List<String> allLines = Files.readAllLines(filePath);

			for (String line : allLines) {
				if (line != null && !line.isEmpty()) {
					String[] array = line.split(", ");
					if (array.length == 2) {
						int X = Integer.parseInt(array[0].replaceAll("\\[", "").replaceAll("\\]", ""));
						int Z = Integer.parseInt(array[1].replaceAll("\\[", "").replaceAll("\\]", ""));
						ChunkPos chunkPos = new ChunkPos(X, Z);
						if (!OldGenerationOldChunks.contains(chunkPos) && !beingUpdatedOldChunks.contains(chunkPos) && !tickexploitChunks.contains(chunkPos) && !newChunks.contains(chunkPos) && !oldChunks.contains(chunkPos)) {
							chunkSet.add(chunkPos);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println("Null pointer found. ");
			e.printStackTrace();
		}
	}
	private void saveData(Path savedDataLocation, ChunkPos chunkpos) {
		try {
			Path dirPath = Paths.get("payload", "NewChunks", serverip, world);
			Files.createDirectories(dirPath);

			Path filePath = dirPath.resolve(savedDataLocation);
			String data = chunkpos.toString() + System.lineSeparator();

			Files.write(filePath, data.getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void removeChunksOutsideRenderDistance() {
		if (mc.player == null) return;
		BlockPos playerPos = new BlockPos(mc.player.getBlockX(), Math.round(renderHeight.get()), mc.player.getBlockZ());
		double renderDistanceBlocks = renderDistance.get() * 16;

		removeChunksOutsideRenderDistance(newChunks, playerPos, renderDistanceBlocks);
		removeChunksOutsideRenderDistance(oldChunks, playerPos, renderDistanceBlocks);
		removeChunksOutsideRenderDistance(beingUpdatedOldChunks, playerPos, renderDistanceBlocks);
		removeChunksOutsideRenderDistance(OldGenerationOldChunks, playerPos, renderDistanceBlocks);
		removeChunksOutsideRenderDistance(tickexploitChunks, playerPos, renderDistanceBlocks);
	}
	private void removeChunksOutsideRenderDistance(Set<ChunkPos> chunkSet, BlockPos playerPos, double renderDistanceBlocks) {
		chunkSet.removeIf(c -> !playerPos.isWithinDistance(new BlockPos(c.getCenterX(), Math.round(renderHeight.get()), c.getCenterZ()), renderDistanceBlocks));
	}
}
