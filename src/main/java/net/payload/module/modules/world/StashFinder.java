package net.payload.module.modules.world;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BlocksSetting;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
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
import java.util.concurrent.atomic.AtomicInteger;

public class StashFinder extends Module implements TickListener, Render3DListener, GameLeftListener, ReceivePacketListener {
	private final SettingGroup blockDetectionSettings;
	private final SettingGroup entityDetectionSettings;
	private final SettingGroup blockConfigSettings;
	private final SettingGroup renderSettings;


	private final BooleanSetting chatFeedback = BooleanSetting.builder()
			.id("basefinder_chat_feedback")
			.displayName("Chat Feedback")
			.description("Displays info for you.")
			.defaultValue(true)
			.build();

	private final BooleanSetting displayCoords = BooleanSetting.builder()
			.id("basefinder_display_coords")
			.displayName("Display Coords")
			.description("Displays coords of air disturbances in chat.")
			.defaultValue(true)
			.build();

	private final FloatSetting minY = FloatSetting.builder()
			.id("basefinder_min_y")
			.displayName("Y-Scan Min")
			.description("Scans blocks above or at this many blocks from minimum build limit.")
			.defaultValue(0f)
			.minValue(0f)
			.maxValue(319f)
			.step(1f)
			.build();

	private final FloatSetting maxY = FloatSetting.builder()
			.id("basefinder_max_y")
			.displayName("Y-Scan Max")
			.description("Scans blocks below or at this many blocks from maximum build limit.")
			.defaultValue(0f)
			.minValue(0f)
			.maxValue(319f)
			.step(1f)
			.build();

	private final FloatSetting bsefndtickdelay = FloatSetting.builder()
			.id("basefinder_base_found_delay")
			.displayName("Message Delay")
			.description("Delays the allowance of Base Found messages to reduce spam.")
			.defaultValue(5f)
			.minValue(0f)
			.maxValue(300f)
			.step(1f)
			.build();

	// Block Detection Settings
	private final BooleanSetting signFinder = BooleanSetting.builder()
			.id("basefinder_sign_finder")
			.displayName("Written Signs")
			.description("Finds signs that have text on them because they are not natural.")
			.defaultValue(true)
			.build();

	private final BooleanSetting portalFinder = BooleanSetting.builder()
			.id("basefinder_portal_finder")
			.displayName("Open Portals")
			.description("Finds End/Nether portals that are open because they are usually not natural.")
			.defaultValue(true)
			.build();

	private final BooleanSetting bubblesFinder = BooleanSetting.builder()
			.id("basefinder_bubbles_finder")
			.displayName("Bubble Columns")
			.description("Finds bubble column blocks made by soul sand because they are not natural.")
			.defaultValue(true)
			.build();


	private final BooleanSetting bedrockFind = BooleanSetting.builder()
			.id("basefinder_bedrock_find")
			.displayName("Illegal Bedrock")
			.description("If Bedrock Blocks higher than they can naturally generate in the Overworld or Nether, flag chunk as possible build.")
			.defaultValue(true)
			.build();


	private final BooleanSetting spawner = BooleanSetting.builder()
			.id("basefinder_spawner")
			.displayName("Mob Grinders")
			.description("If a spawner doesn't have the proper natural companion blocks with it in the chunk, flag as possible build.")
			.defaultValue(true)
			.build();

	private final BooleanSetting roofDetector = BooleanSetting.builder()
			.id("basefinder_roof_detector")
			.displayName("Nether Roof Blocks")
			.description("If anything but mushrooms on the nether roof, flag as possible build.")
			.defaultValue(true)
			.build();

	// Entity Detection Settings
	private final FloatSetting entityScanDelay = FloatSetting.builder()
			.id("basefinder_entity_scan_delay")
			.displayName("Scan Delay")
			.description("Delay between scanning all the entities within render distance.")
			.defaultValue(20f)
			.minValue(0f)
			.maxValue(300f)
			.step(1f)
			.build();

	private final BooleanSetting frameFinder = BooleanSetting.builder()
			.id("basefinder_frame_finder")
			.displayName("Item Frames")
			.description("Finds item frames that do not contain an elytra because they are not natural.")
			.defaultValue(true)
			.build();

	private final BooleanSetting pearlFinder = BooleanSetting.builder()
			.id("basefinder_pearl_finder")
			.displayName("Ender Pearls")
			.description("Finds ender pearls entities because they are not natural.")
			.defaultValue(true)
			.build();

	private final BooleanSetting nameFinder = BooleanSetting.builder()
			.id("basefinder_name_finder")
			.displayName("Named Mobs")
			.description("Finds mobs with a nametag because they are not natural.")
			.defaultValue(true)
			.build();

	private final BooleanSetting villagerFinder = BooleanSetting.builder()
			.id("basefinder_villager_finder")
			.displayName("Active Villagers")
			.description("Finds villagers with a level greater than 1 because they are not natural.")
			.defaultValue(true)
			.build();

	private final BooleanSetting boatFinder = BooleanSetting.builder()
			.id("basefinder_boat_finder")
			.displayName("Placed Boats")
			.description("Finds boats because they are not natural.")
			.defaultValue(true)
			.build();

	private final BooleanSetting entityClusterFinder = BooleanSetting.builder()
			.id("basefinder_entity_cluster_finder")
			.displayName("Mob Clusters")
			.description("Finds clusters of entities per chunk.")
			.defaultValue(true)
			.build();

	private final FloatSetting animalsFoundThreshold = FloatSetting.builder()
			.id("basefinder_animals_threshold")
			.displayName("Cluster Minimum")
			.description("Once this many entities are found in a chunk trigger it as being a base.")
			.defaultValue(14f)
			.minValue(1f)
			.maxValue(100f)
			.step(1f)
			.build();

	// Block List Settings

	public BlocksSetting buildingBlocks = BlocksSetting.builder().id("sf_blockslist1").displayName("BuildingBlocks")
			.defaultValue(new HashSet<Block>(Lists.newArrayList(Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK,
					Blocks.CRAFTER, Blocks.SPRUCE_SAPLING, Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.CHERRY_SAPLING, Blocks.BAMBOO_SAPLING,
					Blocks.CHERRY_BUTTON, Blocks.CHERRY_DOOR, Blocks.CHERRY_FENCE, Blocks.CHERRY_FENCE_GATE, Blocks.CHERRY_PLANKS, Blocks.CHERRY_PRESSURE_PLATE, Blocks.CHERRY_STAIRS, Blocks.CHERRY_WOOD, Blocks.CHERRY_TRAPDOOR, Blocks.CHERRY_SLAB,
					Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_BUTTON, Blocks.MANGROVE_DOOR, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_FENCE_GATE, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_SLAB, Blocks.MANGROVE_TRAPDOOR,
					Blocks.BIRCH_DOOR, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_BUTTON, Blocks.ACACIA_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.POLISHED_BLACKSTONE_BUTTON, Blocks.SPRUCE_BUTTON,
					Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_BUTTON, Blocks.BAMBOO_DOOR, Blocks.BAMBOO_FENCE, Blocks.BAMBOO_FENCE_GATE, Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_MOSAIC_SLAB, Blocks.BAMBOO_MOSAIC_STAIRS, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_PRESSURE_PLATE, Blocks.BAMBOO_SLAB, Blocks.BAMBOO_STAIRS, Blocks.BAMBOO_TRAPDOOR, Blocks.CHISELED_BOOKSHELF,
					Blocks.BLACK_CONCRETE, Blocks.BLUE_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.BROWN_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE, Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.GREEN_CONCRETE,
					Blocks.BLACK_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER,
					Blocks.PURPLE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.MAGENTA_GLAZED_TERRACOTTA, Blocks.PINK_GLAZED_TERRACOTTA, Blocks.GRAY_GLAZED_TERRACOTTA, Blocks.BLUE_GLAZED_TERRACOTTA, Blocks.BROWN_GLAZED_TERRACOTTA, Blocks.GREEN_GLAZED_TERRACOTTA,
					Blocks.OXIDIZED_COPPER, Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER, Blocks.CUT_COPPER_SLAB, Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB, Blocks.CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER, Blocks.OXIDIZED_CHISELED_COPPER, Blocks.COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR, Blocks.OXIDIZED_COPPER_DOOR, Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE, Blocks.OXIDIZED_COPPER_GRATE, Blocks.COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR,
					Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE, Blocks.WAXED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR,
					Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.POTTED_MANGROVE_PROPAGULE, Blocks.POTTED_AZALEA_BUSH, Blocks.POTTED_CHERRY_SAPLING, Blocks.POTTED_FERN, Blocks.POTTED_ACACIA_SAPLING, Blocks.POTTED_WARPED_FUNGUS, Blocks.POTTED_WARPED_ROOTS, Blocks.POTTED_CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_ROOTS, Blocks.POTTED_OAK_SAPLING, Blocks.POTTED_WITHER_ROSE, Blocks.WITHER_ROSE,
					Blocks.CAKE, Blocks.CANDLE_CAKE, Blocks.BLUE_CANDLE_CAKE, Blocks.BLACK_CANDLE_CAKE, Blocks.BROWN_CANDLE_CAKE, Blocks.CYAN_CANDLE_CAKE, Blocks.GRAY_CANDLE_CAKE, Blocks.GREEN_CANDLE_CAKE, Blocks.LIGHT_BLUE_CANDLE_CAKE, Blocks.LIGHT_GRAY_CANDLE_CAKE, Blocks.LIME_CANDLE_CAKE, Blocks.MAGENTA_CANDLE_CAKE, Blocks.ORANGE_CANDLE_CAKE, Blocks.PINK_CANDLE_CAKE, Blocks.PURPLE_CANDLE_CAKE, Blocks.RED_CANDLE_CAKE, Blocks.WHITE_CANDLE_CAKE, Blocks.YELLOW_CANDLE_CAKE,
					Blocks.BLUE_CANDLE, Blocks.BLACK_CANDLE, Blocks.BROWN_CANDLE, Blocks.CYAN_CANDLE, Blocks.GRAY_CANDLE, Blocks.GREEN_CANDLE, Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_GRAY_CANDLE, Blocks.LIME_CANDLE, Blocks.MAGENTA_CANDLE, Blocks.ORANGE_CANDLE, Blocks.PINK_CANDLE, Blocks.PURPLE_CANDLE, Blocks.YELLOW_CANDLE,
					Blocks.SMOOTH_RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE_STAIRS, Blocks.RED_SANDSTONE_WALL,
					Blocks.ANDESITE_STAIRS, Blocks.ANDESITE_SLAB, Blocks.ANDESITE_WALL, Blocks.POLISHED_ANDESITE_SLAB, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.POLISHED_GRANITE_SLAB, Blocks.POLISHED_GRANITE_STAIRS, Blocks.POLISHED_DIORITE_SLAB, Blocks.POLISHED_DIORITE_STAIRS,
					Blocks.TUFF_SLAB, Blocks.TUFF_STAIRS, Blocks.TUFF_WALL, Blocks.TUFF_BRICK_SLAB, Blocks.TUFF_BRICK_STAIRS, Blocks.TUFF_BRICK_WALL,
					Blocks.CRACKED_NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS, Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICK_WALL, Blocks.RED_NETHER_BRICKS, Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICK_WALL,
					Blocks.ORANGE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS,
					Blocks.CRIMSON_PRESSURE_PLATE, Blocks.CRIMSON_BUTTON, Blocks.CRIMSON_DOOR, Blocks.CRIMSON_FENCE, Blocks.CRIMSON_FENCE_GATE, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_TRAPDOOR,
					Blocks.WARPED_PRESSURE_PLATE, Blocks.WARPED_BUTTON, Blocks.WARPED_DOOR, Blocks.WARPED_FENCE, Blocks.WARPED_FENCE_GATE, Blocks.WARPED_PLANKS, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.WARPED_SLAB, Blocks.WARPED_STAIRS, Blocks.WARPED_TRAPDOOR,
					Blocks.SCAFFOLDING, Blocks.CHERRY_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN, Blocks.SLIME_BLOCK, Blocks.SPONGE, Blocks.TINTED_GLASS,
					Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN,
					Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_STAIRS, Blocks.OCHRE_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT, Blocks.VERDANT_FROGLIGHT, Blocks.PETRIFIED_OAK_SLAB,
					Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_CHERRY_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_BIRCH_WOOD, Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM,
					Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON, Blocks.ACTIVATOR_RAIL, Blocks.BEACON, Blocks.BEEHIVE, Blocks.REPEATING_COMMAND_BLOCK, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.IRON_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.RAW_GOLD_BLOCK, Blocks.CONDUIT, Blocks.DAYLIGHT_DETECTOR, Blocks.DETECTOR_RAIL, Blocks.DRIED_KELP_BLOCK, Blocks.DROPPER, Blocks.ENCHANTING_TABLE,
					Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.HEAVY_CORE,
					Blocks.HONEY_BLOCK, Blocks.HONEYCOMB_BLOCK, Blocks.JUKEBOX, Blocks.LIGHTNING_ROD, Blocks.LODESTONE, Blocks.OBSERVER, Blocks.POWERED_RAIL, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE, Blocks.BIRCH_PRESSURE_PLATE, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.MANGROVE_PRESSURE_PLATE, Blocks.CRIMSON_PRESSURE_PLATE, Blocks.WARPED_PRESSURE_PLATE, Blocks.RESPAWN_ANCHOR, Blocks.CALIBRATED_SCULK_SENSOR, Blocks.SNIFFER_EGG,
					Blocks.POTTED_PALE_OAK_SAPLING, Blocks.PALE_OAK_SAPLING, Blocks.PALE_OAK_BUTTON, Blocks.PALE_OAK_DOOR, Blocks.PALE_OAK_FENCE, Blocks.PALE_OAK_FENCE_GATE, Blocks.PALE_OAK_PLANKS, Blocks.PALE_OAK_PRESSURE_PLATE, Blocks.PALE_OAK_HANGING_SIGN, Blocks.PALE_OAK_SIGN, Blocks.PALE_OAK_WALL_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN, Blocks.PALE_OAK_SLAB, Blocks.PALE_OAK_STAIRS, Blocks.PALE_OAK_TRAPDOOR, Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD, Blocks.SPRUCE_WALL_SIGN, Blocks.POLISHED_DIORITE, Blocks.NOTE_BLOCK, Blocks.MANGROVE_WOOD, Blocks.WEATHERED_COPPER)))
			.build();

	public BlocksSetting storageBlocks = BlocksSetting.builder().id("sf_blockslist2").displayName("StorageBlocks")
			.defaultValue(new HashSet<Block>(Lists.newArrayList(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
					Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX,
					Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.BARREL)))
			.build();

	private final BooleanSetting list1Activar = BooleanSetting.builder()
			.id("basefinder_list1_active")
			.displayName("Toggle BuildingBlocks")
			.description("Activates checks for List #1")
			.defaultValue(true)
			.build();

	private final BooleanSetting list2Activar = BooleanSetting.builder()
			.id("basefinder_list2_active")
			.displayName("Toggle StorageBlocks")
			.description("Activates checks for List #2")
			.defaultValue(true)
			.build();

	// Block List Thresholds
	private final FloatSetting buildingblocksNum = FloatSetting.builder()
			.id("basefinder_list1_threshold")
			.displayName("BuildingBlocks Threshold")
			.description("How many blocks it takes from any of the listed blocks to throw a base location.")
			.defaultValue(25f)
			.minValue(1f)
			.maxValue(100f)
			.step(1f)
			.build();

	private final FloatSetting storageblocksNum = FloatSetting.builder()
			.id("basefinder_list2_threshold")
			.displayName("StorageBlocks Threshold")
			.description("How many blocks it takes from any of the listed blocks to throw a base location.")
			.defaultValue(25f)
			.minValue(1f)
			.maxValue(100f)
			.step(1f)
			.build();

	// Cache Data Settings
	private final BooleanSetting remove = BooleanSetting.builder()
			.id("basefinder_remove_on_disable")
			.displayName("Remove On Disable")
			.description("Removes the cached chunks containing bases when disabling the module.")
			.defaultValue(true)
			.build();

	private final BooleanSetting worldleaveremove = BooleanSetting.builder()
			.id("basefinder_remove_on_leave")
			.displayName("Remove On Leave")
			.description("Removes the cached chunks containing bases when leaving the world or changing dimensions.")
			.defaultValue(true)
			.build();

	private final BooleanSetting removerenderdist = BooleanSetting.builder()
			.id("basefinder_remove_outside_render")
			.displayName("Remove Outside Render Distance")
			.description("Removes the cached chunks when they leave the defined render distance.")
			.defaultValue(true)
			.build();

	// Saved Data Settings
	private final BooleanSetting save = BooleanSetting.builder()
			.id("basefinder_save_data")
			.displayName("Save Base Data")
			.description("Saves the cached bases to a file.")
			.defaultValue(false)
			.build();


	// Render Settings
	private final FloatSetting renderDistance = FloatSetting.builder()
			.id("basefinder_render_distance")
			.displayName("Distance")
			.description("How many chunks from the character to render the detected chunks with bases.")
			.defaultValue(128f)
			.minValue(6f)
			.maxValue(1024f)
			.step(1f)
			.build();

	private final FloatSetting renderHeightY = FloatSetting.builder()
			.id("basefinder_render_height_top")
			.displayName("Render Max")
			.description("The render height.")
			.defaultValue(256f)
			.minValue(-128f)
			.maxValue(512f)
			.step(1f)
			.build();

	private final FloatSetting renderHeightYBottom = FloatSetting.builder()
			.id("basefinder_render_height_bottom")
			.displayName("Render Min")
			.description("The render height.")
			.defaultValue(64f)
			.minValue(-128f)
			.maxValue(512f)
			.step(1f)
			.build();

	private final ColorSetting baseChunksSideColor = ColorSetting.builder()
			.id("basefinder_chunks_side_color")
			.displayName("Base Chunks Waypoint Color")
			.description("Color of the waypoints indicating chunks that may contain bases or builds.")
			.defaultValue(new Color(1f, 0.5f, 0f, 0.16f))
			.build();

	public StashFinder() {
		super("StashFinder");
		this.setCategory(Category.of("World"));
		this.setDescription("Searches for potential bases and logs these coordinates in the .minecraft folder");
		blockDetectionSettings = SettingGroup.Builder.builder()
				.id("world")
				.displayName("Block Detection")
				.description("Block-based detection methods")
				.build();

		entityDetectionSettings = SettingGroup.Builder.builder()
				.id("entities")
				.displayName("Entity Detection")
				.description("Entity-based detection methods")
				.build();

		blockConfigSettings = SettingGroup.Builder.builder()
				.id("blocks")
				.displayName("Block Config")
				.description("Block list configurations")
				.build();

		renderSettings = SettingGroup.Builder.builder()
				.id("render")
				.displayName("Render")
				.description("Visual settings")
				.build();
		// General Settings
		blockDetectionSettings.addSetting(signFinder);
		blockDetectionSettings.addSetting(portalFinder);
		blockDetectionSettings.addSetting(bubblesFinder);
		blockDetectionSettings.addSetting(bedrockFind);
		blockDetectionSettings.addSetting(spawner);
		blockDetectionSettings.addSetting(roofDetector);

		// Entity Detection Settings
		entityDetectionSettings.addSetting(entityScanDelay);
		entityDetectionSettings.addSetting(frameFinder);
		entityDetectionSettings.addSetting(pearlFinder);
		entityDetectionSettings.addSetting(nameFinder);
		entityDetectionSettings.addSetting(villagerFinder);
		entityDetectionSettings.addSetting(boatFinder);
		entityDetectionSettings.addSetting(entityClusterFinder);
		entityDetectionSettings.addSetting(animalsFoundThreshold);

		// Block Config Settings
		blockConfigSettings.addSetting(list1Activar);        // Set A toggle
		blockConfigSettings.addSetting(list2Activar);        // Set B toggle
		blockConfigSettings.addSetting(buildingblocksNum);          // Set A threshold
		blockConfigSettings.addSetting(storageblocksNum);          // Set B threshold
		blockConfigSettings.addSetting(buildingBlocks);            // Set A block list
		blockConfigSettings.addSetting(storageBlocks);            // Set B block list

		// Render Settings
		renderSettings.addSetting(renderDistance);           // Render Distance
		renderSettings.addSetting(renderHeightY);            // Render Max
		renderSettings.addSetting(renderHeightYBottom);      // Render Min
		renderSettings.addSetting(baseChunksSideColor);      // Base Chunks Waypoints
		renderSettings.addSetting(chatFeedback);
		renderSettings.addSetting(displayCoords);
		renderSettings.addSetting(bsefndtickdelay);
		this.addSetting(minY);
		this.addSetting(maxY);
		this.addSetting(blockDetectionSettings);
		this.addSetting(entityDetectionSettings);
		this.addSetting(blockConfigSettings);
		this.addSetting(renderSettings);
		// Add remaining uncategorized settings
		this.addSetting(remove);
		this.addSetting(worldleaveremove);
		this.addSetting(removerenderdist);
		this.addSetting(save);


		// Register all settings with SettingManager
		// Block Detection Settings
		SettingManager.registerSetting(signFinder);
		SettingManager.registerSetting(portalFinder);
		SettingManager.registerSetting(bubblesFinder);
		SettingManager.registerSetting(bedrockFind);
		SettingManager.registerSetting(spawner);
		SettingManager.registerSetting(roofDetector);

		// Entity Detection Settings
		SettingManager.registerSetting(entityScanDelay);
		SettingManager.registerSetting(frameFinder);
		SettingManager.registerSetting(pearlFinder);
		SettingManager.registerSetting(nameFinder);
		SettingManager.registerSetting(villagerFinder);
		SettingManager.registerSetting(boatFinder);
		SettingManager.registerSetting(entityClusterFinder);
		SettingManager.registerSetting(animalsFoundThreshold);

		// Block Config Settings
		SettingManager.registerSetting(list1Activar);
		SettingManager.registerSetting(list2Activar);
		SettingManager.registerSetting(buildingblocksNum);
		SettingManager.registerSetting(storageblocksNum);
		SettingManager.registerSetting(buildingBlocks);
		SettingManager.registerSetting(storageBlocks);

		// Render Settings
		SettingManager.registerSetting(renderDistance);
		SettingManager.registerSetting(renderHeightY);
		SettingManager.registerSetting(renderHeightYBottom);
		SettingManager.registerSetting(baseChunksSideColor);

		// Register remaining uncategorized settings
		SettingManager.registerSetting(chatFeedback);
		SettingManager.registerSetting(displayCoords);
		SettingManager.registerSetting(minY);
		SettingManager.registerSetting(maxY);
		SettingManager.registerSetting(bsefndtickdelay);
		SettingManager.registerSetting(remove);
		SettingManager.registerSetting(worldleaveremove);
		SettingManager.registerSetting(removerenderdist);
		SettingManager.registerSetting(save);

		// Register the groups themselves
		SettingManager.registerSetting(blockDetectionSettings);
		SettingManager.registerSetting(entityDetectionSettings);
		SettingManager.registerSetting(blockConfigSettings);
		SettingManager.registerSetting(renderSettings);
	}

	private static final ExecutorService taskExecutor = Executors.newCachedThreadPool();
	private int basefoundspamTicks=0;
	private boolean basefound=false;
	private int deletewarningTicks=666;
	private int deletewarning=0;
	private boolean checkingchunk1=false;
	private int found1 = 0;
	private boolean checkingchunk2=false;
	private int found2 = 0;
	private ChunkPos LastBaseFound = new ChunkPos(2000000000, 2000000000);
	private int closestbaseX=2000000000;
	private int closestbaseZ=2000000000;
	private double basedistance=2000000000;
	private String serverip;
	private String world;
	private ChunkPos basepos;
	private BlockPos blockposi;
	private final Set<ChunkPos> baseChunks = Collections.synchronizedSet(new HashSet<>());
	private static int isBaseFinderModuleOn=0;
	private int autoreloadticks=0;
	private int loadingticks=0;
	private boolean worldchange=false;
	private int justenabledsavedata=0;
	private boolean saveDataWasOn = false;
	private int findnearestbaseticks=0;
	private boolean spawnernaturalblocks=false;
	private boolean spawnerfound=false;
	private int spawnerY;
	private String lastblockfound1;
	private String lastblockfound2;
	private int entityScanTicks;

	private void clearChunkData() {
		baseChunks.clear();
		closestbaseX=2000000000;
		closestbaseZ=2000000000;
		basedistance=2000000000;
		LastBaseFound= new ChunkPos(2000000000, 2000000000);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);

		isBaseFinderModuleOn=0;
		autoreloadticks=0;
		loadingticks=0;
		worldchange=false;
		justenabledsavedata = 0;
		if (remove.get()) {
			clearChunkData();
		}
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
		Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);

		isBaseFinderModuleOn=1;
		if (save.get())saveDataWasOn = true;
		else if (!save.get())saveDataWasOn = false;

		if (save.get()) {
			if (MC.isInSingleplayer()){
				String[] array = MC.getServer().getSavePath(WorldSavePath.ROOT).toString().replace(':', '_').split("/|\\\\");
				serverip=array[array.length-2];
				world= MC.world.getRegistryKey().getValue().toString().replace(':', '_');
			} else {
				serverip = MC.getCurrentServerEntry().address.replace(':', '_');}
			world= MC.world.getRegistryKey().getValue().toString().replace(':', '_');
			if (save.get()){
				try {
					Files.createDirectories(Paths.get("payload", "StashFinder", serverip, world));
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}

		}
		autoreloadticks=0;
		loadingticks=0;
		worldchange=false;
		justenabledsavedata = 0;
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onGameLeft(GameLeftEvent event) {
		if (worldleaveremove.get()) {
			clearChunkData();
		}
	}

	private void render(Box box, Color sides, Render3DEvent event) {

		Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), box, sides, 1f);
		//event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sides, new Color(0,0,0,0), 0);
	}
	private void render2(Box box, Color sides, Color lines, Render3DEvent event) {
		Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), box, sides, 1f);
		//event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sides, new Color(0,0,0,0), 0);
	}

	@Override
	public void onRender(Render3DEvent event) {
		int topY = Math.round(renderHeightY.get());
		int bottomY = Math.round(renderHeightYBottom.get());
		int midpoint = (topY + bottomY) / 2;
		BlockPos playerPos = new BlockPos(MC.player.getBlockX(), midpoint, MC.player.getBlockZ());
		if (baseChunksSideColor.get().getAlphaInt() > 5){
				synchronized (baseChunks) {
					for (ChunkPos c : baseChunks) {
						if (playerPos.isWithinDistance(new BlockPos(c.getCenterX(), midpoint, c.getCenterZ()), renderDistance.get() * 16)) {
							render(new Box(new Vec3d(c.getStartPos().getX() + 7, c.getStartPos().getY() + renderHeightYBottom.get(), c.getStartPos().getZ() + 7), new Vec3d(c.getStartPos().getX() + 8, c.getStartPos().getY() + renderHeightY.get(), c.getStartPos().getZ() + 8)), baseChunksSideColor.get(), event);
						}
					}
				}
		}
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		world = MC.world.getRegistryKey().getValue().toString().replace(':', '_');

		if (basefound && basefoundspamTicks < bsefndtickdelay.get()) basefoundspamTicks++;
		else if (basefoundspamTicks >= bsefndtickdelay.get()) {
			basefound = false;
			basefoundspamTicks = 0;
		}
		if (deletewarningTicks <= 100) deletewarningTicks++;
		if (deletewarning>=2){
			if (MC.isInSingleplayer()){
				String[] array = MC.getServer().getSavePath(WorldSavePath.ROOT).toString().replace(':', '_').split("/|\\\\");
				serverip=array[array.length-2];
			} else {
				serverip = MC.getCurrentServerEntry().address.replace(':', '_');
			}
			clearChunkData();
			try {
				Files.deleteIfExists(Paths.get("payload", "StashFinder", serverip, world, "BaseChunkData.txt"));
			} catch (IOException e) {
				//e.printStackTrace();
			}
			sendErrorMessage("Chunk Data deleted for this Dimension.");
			deletewarning=0;
		}


		try {
			if (baseChunks.stream().toList().size() > 0) {
				for (int b = 0; b < baseChunks.stream().toList().size(); b++) {
					if (basedistance > Math.sqrt(Math.pow(baseChunks.stream().toList().get(b).x - MC.player.getChunkPos().x, 2) + Math.pow(baseChunks.stream().toList().get(b).z - MC.player.getChunkPos().z, 2))) {
						closestbaseX = baseChunks.stream().toList().get(b).x;
						closestbaseZ = baseChunks.stream().toList().get(b).z;
						basedistance = Math.sqrt(Math.pow(baseChunks.stream().toList().get(b).x - MC.player.getChunkPos().x, 2) + Math.pow(baseChunks.stream().toList().get(b).z - MC.player.getChunkPos().z, 2));
					}
				}
				basedistance = 2000000000;
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}

		if (findnearestbaseticks == 1) {
			if (closestbaseX < 1000000000 && closestbaseZ < 1000000000)
				sendChatMessage("#Nearest possible base at X" + closestbaseX * 16 + " x Z" + closestbaseZ * 16);
			if (!(closestbaseX < 1000000000 && closestbaseZ < 1000000000))
				sendErrorMessage("No Bases Logged Yet.");
			findnearestbaseticks = 0;
		}

		if (save.get()) {
			if (MC.isInSingleplayer()) {
				String[] array = MC.getServer().getSavePath(WorldSavePath.ROOT).toString().replace(':', '_').split("/|\\\\");
				serverip = array[array.length - 2];
				world = MC.world.getRegistryKey().getValue().toString().replace(':', '_');
			} else {
				serverip = MC.getCurrentServerEntry().address.replace(':', '_');
			}
			world = MC.world.getRegistryKey().getValue().toString().replace(':', '_');
		}

		if (removerenderdist.get()) removeChunksOutsideRenderDistance();

		if (entityScanTicks < entityScanDelay.get()) entityScanTicks++;
		if (entityScanTicks >= entityScanDelay.get() && (pearlFinder.get() || frameFinder.get() || villagerFinder.get() || nameFinder.get() || boatFinder.get() || entityClusterFinder.get())) {
			if (MC.world == null) return;

			int renderDistance = MC.options.getViewDistance().getValue();
			ChunkPos playerChunkPos = new ChunkPos(MC.player.getBlockPos());
			for (int chunkX = playerChunkPos.x - renderDistance; chunkX <= playerChunkPos.x + renderDistance; chunkX++) {
				for (int chunkZ = playerChunkPos.z - renderDistance; chunkZ <= playerChunkPos.z + renderDistance; chunkZ++) {
					WorldChunk chunk = MC.world.getChunk(chunkX, chunkZ);
					if (chunk != null && chunk.getStatus().isAtLeast(ChunkStatus.FULL)) {
						Box chunkBox = new Box(
								chunk.getPos().getStartX(), MC.world.getBottomY(), chunk.getPos().getStartZ(),
								chunk.getPos().getEndX() + 1, MC.world.getTopYInclusive(), chunk.getPos().getEndZ() + 1
						);
						if (!baseChunks.contains(chunk.getPos())) {
							AtomicInteger animalsFound = new AtomicInteger();
							MC.world.getEntitiesByClass(Entity.class, chunkBox, entity -> true).forEach(entity -> {
								if ((entity instanceof ItemFrameEntity || entity instanceof GlowItemFrameEntity) && frameFinder.get()) {
									ItemFrameEntity itemFrame = (ItemFrameEntity) entity;
									Item heldItem = itemFrame.getHeldItemStack().getItem();
									if (heldItem != Items.ELYTRA) {
										baseChunks.add(chunk.getPos());
										if (save.get()) {
											saveBaseChunkData(chunk.getPos());
										}
										if (basefoundspamTicks == 0) {
											if (chatFeedback.get()){
												if (displayCoords.get()) {
													sendChatMessage("Item Frame located near X"  + Math.round(entity.getPos().getX()) + ", Y" + Math.round(entity.getPos().getY()) + ", Z" + Math.round(entity.getPos().getZ()));
												}
												
												else sendChatMessage("Item Frame located!");
											}
											LastBaseFound = new ChunkPos(chunk.getPos().x, chunk.getPos().z);
											basefound = true;
										}
									}
								} else if (entity instanceof EnderPearlEntity && pearlFinder.get()) {
									baseChunks.add(chunk.getPos());
									if (save.get()) {
										saveBaseChunkData(chunk.getPos());
									}
									if (basefoundspamTicks == 0) {
										if (chatFeedback.get()){
											if (displayCoords.get()) {
												sendChatMessage("Ender Pearl located near X"  + Math.round(entity.getPos().getX()) + ", Y" + Math.round(entity.getPos().getY()) + ", Z" + Math.round(entity.getPos().getZ()));
											}
											else sendChatMessage("Ender Pearl located!");
										}
										LastBaseFound = new ChunkPos(chunk.getPos().x, chunk.getPos().z);
										basefound = true;
									}
								} else if (entity instanceof VillagerEntity && villagerFinder.get()) {
									if (((VillagerEntity) entity).getVillagerData().getLevel() > 1) {
										baseChunks.add(chunk.getPos());
										if (save.get()) {
											saveBaseChunkData(chunk.getPos());
										}
										if (basefoundspamTicks == 0) {
											if (chatFeedback.get()){
												if (displayCoords.get())
													sendChatMessage("Illegal Villager located near X" + Math.round(entity.getPos().getX()) + ", Y" + Math.round(entity.getPos().getY()) + ", Z" + Math.round(entity.getPos().getZ()));
												else sendChatMessage("Illegal Villager located!");
											}
											LastBaseFound = new ChunkPos(chunk.getPos().x, chunk.getPos().z);
											basefound = true;
										}
									}
								} else if (entity.hasCustomName() && nameFinder.get()) {
									baseChunks.add(chunk.getPos());
									if (save.get()) {
										saveBaseChunkData(chunk.getPos());
									}
									if (basefoundspamTicks == 0) {
										if (chatFeedback.get()){
											if (displayCoords.get())sendChatMessage("NameTagged Entity located near X" + Math.round(entity.getPos().getX()) + ", Y" + Math.round(entity.getPos().getY()) + ", Z" + Math.round(entity.getPos().getZ()));
											else sendChatMessage("NameTagged Entity located!");
										}
										LastBaseFound = new ChunkPos(chunk.getPos().x, chunk.getPos().z);
										basefound = true;
									}
								} else if ((entity instanceof ChestBoatEntity || entity instanceof BoatEntity) && boatFinder.get()) {
									baseChunks.add(chunk.getPos());
									if (save.get()) {
										saveBaseChunkData(chunk.getPos());
									}
									if (basefoundspamTicks == 0) {
										if (chatFeedback.get()){
											if (displayCoords.get())sendChatMessage("Illegal Boat located near X" + Math.round(entity.getPos().getX()) + ", Y" + Math.round(entity.getPos().getY()) + ", Z" + Math.round(entity.getPos().getZ()));
											else sendChatMessage("Illegal Boat located!");
										}
										LastBaseFound = new ChunkPos(chunk.getPos().x, chunk.getPos().z);
										basefound = true;
									}
								}

								else if (entity instanceof LivingEntity && entityClusterFinder.get()) {
									animalsFound.getAndIncrement();
								}

							});
							if (animalsFound.get() >= animalsFoundThreshold.get() && entityClusterFinder.get()){
								baseChunks.add(chunk.getPos());
								if (save.get()) {
									saveBaseChunkData(chunk.getPos());
								}
								if (basefoundspamTicks == 0) {
									if (chatFeedback.get()){
										if (displayCoords.get())sendChatMessage("Illegal amount of entities located near X" + chunk.getPos().getCenterX() + ", Z" + chunk.getPos().getCenterZ());
										else sendChatMessage("Illegal amount of entities located!");
									}
									LastBaseFound = new ChunkPos(chunk.getPos().x, chunk.getPos().z);
									basefound = true;
								}
							}
						}
					}
				}
			}
			entityScanTicks = 0;
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	@Override
	public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
		if (readPacketEvent.getPacket() instanceof PlayerMoveC2SPacket) return; //this keeps getting cast to the chunkdata for no reason
		if (!(readPacketEvent.getPacket() instanceof PlayerMoveC2SPacket) && readPacketEvent.getPacket() instanceof ChunkDataS2CPacket packet && MC.world != null) {

			basepos = new ChunkPos(packet.getChunkX(), packet.getChunkZ());

			if (MC.world.getChunkManager().getChunk(packet.getChunkX(), packet.getChunkZ()) == null) {
				WorldChunk chunk = new WorldChunk(MC.world, basepos);
				try {
					CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
						chunk.loadFromPacket(packet.getChunkData().getSectionsDataBuf(), new NbtCompound(),
								packet.getChunkData().getBlockEntities(packet.getChunkX(), packet.getChunkZ()));
					}, taskExecutor);
					future.join();
				} catch (CompletionException e) {}

				if (bubblesFinder.get() || spawner.get() || signFinder.get() || portalFinder.get() || roofDetector.get() || bedrockFind.get() || !buildingBlocks.get().isEmpty() || !storageBlocks.get().isEmpty()){
					int Ymin = MC.world.getBottomY() + Math.round(minY.get());
					int Ymax = MC.world.getTopYInclusive() - Math.round(maxY.get());
					try {
						Set<BlockPos> blockpositions1 = Collections.synchronizedSet(new HashSet<>());
						Set<BlockPos> blockpositions2 = Collections.synchronizedSet(new HashSet<>());
						ChunkSection[] sections = chunk.getSectionArray();
						int Y = MC.world.getBottomY();
						for (ChunkSection section: sections){
							if (section == null || section.isEmpty()) {
								Y+=16;
								continue;
							}
							for (int x = 0; x < 16; x++) {
								for (int y = 0; y < 16; y++) {
									for (int z = 0; z < 16; z++) {
										int currentY = Y + y;
										if (currentY <= Ymin || currentY >= Ymax) continue;
										blockposi=new BlockPos(x, currentY, z);
										BlockState blerks = section.getBlockState(x,y,z);
										if (blerks.getBlock()!= Blocks.AIR && blerks.getBlock()!=Blocks.STONE){
											if (!(blerks.getBlock()==Blocks.DEEPSLATE) && !(blerks.getBlock()==Blocks.DIRT) && !(blerks.getBlock()==Blocks.GRASS_BLOCK) && !(blerks.getBlock()==Blocks.WATER) && !(blerks.getBlock()==Blocks.SAND) && !(blerks.getBlock()==Blocks.GRAVEL)  && !(blerks.getBlock()==Blocks.BEDROCK)&& !(blerks.getBlock()==Blocks.NETHERRACK) && !(blerks.getBlock()==Blocks.LAVA)){
												if (signFinder.get() && blerks.getBlock() instanceof SignBlock || blerks.getBlock() instanceof HangingSignBlock) {
													for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
														Boolean signtextfound = false;
														if (blockEntity instanceof SignBlockEntity){
															SignText signText = ((SignBlockEntity) blockEntity).getFrontText();
															SignText signText2 = ((SignBlockEntity) blockEntity).getBackText();
															Text[] lines = signText.getMessages(false);
															Text[] lines2 = signText2.getMessages(false);
															int i = 0;
															for (Text line : lines) {
																if (line.getLiteralString().length() != 0 && (line.getString() != "<----" && i == 1) && (line.getString() != "---->" && i == 2)){ //handling for arrows is for igloos
																	signtextfound = true;
																	if (signtextfound) break;
																}
																i++;
															}
															for (Text line2 : lines2) {
																if (signtextfound) break;
																if (line2.getLiteralString().length() != 0){
																	signtextfound = true;
																	if (signtextfound) break;
																}
															}
														} else if (blockEntity instanceof HangingSignBlockEntity) {
															SignText signText = ((HangingSignBlockEntity) blockEntity).getFrontText();
															SignText signText2 = ((HangingSignBlockEntity) blockEntity).getBackText();
															Text[] lines = signText.getMessages(false);
															Text[] lines2 = signText2.getMessages(false);
															for (Text line : lines) {
																if (line.getLiteralString().length() != 0){ //handling for arrows is for igloos
																	signtextfound = true;
																	if (signtextfound) break;
																}
															}
															for (Text line2 : lines2) {
																if (signtextfound) break;
																if (line2.getLiteralString().length() != 0){
																	signtextfound = true;
																	if (signtextfound) break;
																}
															}
														}
														if (signtextfound && !baseChunks.contains(basepos)){
															baseChunks.add(basepos);
															if (save.get()) {
																saveBaseChunkData(basepos);
															}
															if (basefoundspamTicks==0){
																if (chatFeedback.get()){
																	if (displayCoords.get())sendChatMessage("Written Sign located near X"+ blockEntity.getPos().getX()+", Y"+blockEntity.getPos().getY()+", Z"+blockEntity.getPos().getZ());
																	else sendChatMessage("Written Sign located!");
																}
																LastBaseFound= new ChunkPos(basepos.x, basepos.z);
																basefound=true;
															}
														}
													}
												}

												if (bubblesFinder.get() && blerks.getBlock() instanceof BubbleColumnBlock && !blerks.get(BubbleColumnBlock.DRAG)) {
													if (!baseChunks.contains(basepos)){
														baseChunks.add(basepos);
														if (save.get()) {
															saveBaseChunkData(basepos);
														}
														if (basefoundspamTicks==0){
															if (chatFeedback.get()){
																if (displayCoords.get())sendChatMessage("(Bubble Column)Possible build located near X"+basepos.getCenterX()+", Y"+currentY+", Z"+basepos.getCenterZ());
																else sendChatMessage("(Bubble Column)Possible build located!");
															}
															LastBaseFound= new ChunkPos(basepos.x, basepos.z);
															basefound=true;
														}
													}
												}
												if (portalFinder.get() && (blerks.getBlock()==Blocks.NETHER_PORTAL || blerks.getBlock()==Blocks.END_PORTAL)) {
													if (!baseChunks.contains(basepos)){
														baseChunks.add(basepos);
														if (save.get()) {
															saveBaseChunkData(basepos);
														}
														if (basefoundspamTicks==0){
															if (chatFeedback.get()){
																if (displayCoords.get())sendChatMessage("(Open Portal)Possible build located near X"+basepos.getCenterX()+", Y"+currentY+", Z"+basepos.getCenterZ());
																else sendChatMessage("(Open Portal)Possible build located!");
															}
															LastBaseFound= new ChunkPos(basepos.x, basepos.z);
															basefound=true;
														}
													}
												}
												if (bedrockFind.get() && blerks.getBlock()==Blocks.BEDROCK && ((currentY>MC.world.getBottomY() && MC.world.getRegistryKey() == World.OVERWORLD) || (currentY>MC.world.getBottomY() && (currentY < 123 || currentY > 127) && MC.world.getRegistryKey() == World.NETHER))) {
													if (!baseChunks.contains(basepos)){
														baseChunks.add(basepos);
														if (save.get()) {
															saveBaseChunkData(basepos);
														}
														if (basefoundspamTicks==0){
															if (chatFeedback.get()){
																if (displayCoords.get())sendChatMessage("(Unnatural Bedrock)Possible build located near X"+basepos.getCenterX()+", Y"+currentY+", Z"+basepos.getCenterZ());
																else sendChatMessage("(Unnatural Bedrock)Possible build located!");
															}
															LastBaseFound= new ChunkPos(basepos.x, basepos.z);
															basefound=true;
														}
													}
												}
												if (roofDetector.get() && blerks.getBlock()!=Blocks.RED_MUSHROOM && blerks.getBlock()!=Blocks.BROWN_MUSHROOM && currentY>=128 && MC.world.getRegistryKey() == World.NETHER){
													if (!baseChunks.contains(basepos)){
														baseChunks.add(basepos);
														if (save.get()) {
															saveBaseChunkData(basepos);
														}
														if (basefoundspamTicks==0){
															if (chatFeedback.get()){
																if (displayCoords.get())sendChatMessage("(Nether Roof)Possible build located near X"+basepos.getCenterX()+", Y"+currentY+", Z"+basepos.getCenterZ());
																else sendChatMessage("(Nether Roof)Possible build located!");
															}
															LastBaseFound= new ChunkPos(basepos.x, basepos.z);
															basefound=true;
														}
													}
												}
												if (spawner.get()){
													if (blerks.getBlock()==Blocks.SPAWNER){
														spawnerY=currentY;
														spawnerfound=true;
													}
													//dungeon MOSSY_COBBLESTONE, mineshaft COBWEB, fortress NETHER_BRICK_FENCE, stronghold STONE_BRICK_STAIRS, bastion CHAIN
													if (MC.world.getRegistryKey() == World.OVERWORLD && (blerks.getBlock()==Blocks.MOSSY_COBBLESTONE || blerks.getBlock()==Blocks.COBWEB || blerks.getBlock()==Blocks.STONE_BRICK_STAIRS || blerks.getBlock()==Blocks.BUDDING_AMETHYST))spawnernaturalblocks=true;
													else if (MC.world.getRegistryKey() == World.NETHER && (blerks.getBlock()==Blocks.NETHER_BRICK_FENCE || blerks.getBlock()==Blocks.CHAIN))spawnernaturalblocks=true;
												}
												if (list1Activar.get() && !buildingBlocks.get().isEmpty()){
													if (buildingBlocks.get().contains(blerks.getBlock())) {
														blockpositions1.add(blockposi);
														found1= blockpositions1.size();
														lastblockfound1=blerks.getBlock().toString();
													}
												}
												if (list2Activar.get() && !storageBlocks.get().isEmpty()){
													if (storageBlocks.get().contains(blerks.getBlock())) {
														blockpositions2.add(blockposi);
														found2= blockpositions2.size();
														lastblockfound2=blerks.getBlock().toString();
													}
												}
											}
										}
										if (!buildingBlocks.get().isEmpty())checkingchunk1=true;
										if (!storageBlocks.get().isEmpty())checkingchunk2=true;
									}
								}
							}
							Y+=16;
						}
						//CheckList 1
						if (!buildingBlocks.get().isEmpty()){
							if (checkingchunk1 && found1>= buildingblocksNum.get()) {
								if (!baseChunks.contains(basepos)){
									baseChunks.add(basepos);
									if (save.get()) {
										saveBaseChunkData(basepos);
									}
									if (basefoundspamTicks== 0) {
										if (chatFeedback.get()){
											if (displayCoords.get())sendChatMessage("(List1)Possible build located near X" + basepos.getCenterX() + ", Y" + blockpositions1.stream().toList().get(0).getY() + ", Z" + basepos.getCenterZ() + " (" + lastblockfound1 + ")");
											else sendChatMessage("(List1)Possible build located!"+" ("+lastblockfound1+")");
										}
										LastBaseFound= new ChunkPos(basepos.x, basepos.z);
										basefound=true;
									}
								}
								blockpositions1.clear();
								found1 = 0;
								checkingchunk1=false;
							} else if (checkingchunk1 && found1< buildingblocksNum.get()){
								blockpositions1.clear();
								found1 = 0;
								checkingchunk1=false;
							}
						}

						//CheckList 2
						if (!storageBlocks.get().isEmpty()){
							if (checkingchunk2 && found2>= storageblocksNum.get()) {
								if (!baseChunks.contains(basepos)){
									baseChunks.add(basepos);
									if (save.get()) {
										saveBaseChunkData(basepos);
									}
									if (basefoundspamTicks== 0) {
										if (chatFeedback.get()){
											if (displayCoords.get())sendChatMessage("(List2)Possible build located near X" + basepos.getCenterX() + ", Y" + blockpositions2.stream().toList().get(0).getY() + ", Z" + basepos.getCenterZ() + " (" + lastblockfound2 + ")");
											else sendChatMessage("(List2)Possible build located!"+" ("+lastblockfound2+")");
										}
										LastBaseFound= new ChunkPos(basepos.x, basepos.z);
										basefound=true;
									}
								}
								blockpositions2.clear();
								found2 = 0;
								checkingchunk2=false;
							} else if (checkingchunk2 && found2< storageblocksNum.get()){
								blockpositions2.clear();
								found2 = 0;
								checkingchunk2=false;
							}
						}
					}
					catch (Exception e){
						//e.printStackTrace();
					}
				}
				if (spawnerfound && !spawnernaturalblocks){
					if (!baseChunks.contains(basepos)){
						baseChunks.add(basepos);
						if (save.get()) {
							saveBaseChunkData(basepos);
						}
						if (basefoundspamTicks== 0) {
							if (chatFeedback.get()){
								if (displayCoords.get())sendChatMessage("Possible modified spawner located near X"+basepos.getCenterX()+", Y"+spawnerY+", Z"+basepos.getCenterZ());
								else sendChatMessage("Possible modified spawner located!");
							}
							LastBaseFound= new ChunkPos(basepos.x, basepos.z);
							basefound=true;
						}
					}
					spawnerfound=false;
					spawnernaturalblocks=false;
				} else if ((spawnerfound && spawnernaturalblocks) || (!spawnerfound && spawnernaturalblocks) || (!spawnerfound && !spawnernaturalblocks)){
					spawnerfound=false;
					spawnernaturalblocks=false;
				}
			}
		}
	}

	private void loadData() {
		try {
			List<String> allLines = Files.readAllLines(Paths.get("payload/StashFinder/"+serverip+"/"+world+"/BaseChunkData.txt"));

			for (String line : allLines) {
				String s = line;
				String[] array = s.split(", ");
				int X = Integer.parseInt(array[0].replaceAll("\\[", "").replaceAll("\\]",""));
				int Z = Integer.parseInt(array[1].replaceAll("\\[", "").replaceAll("\\]",""));
				basepos = new ChunkPos(X,Z);
				baseChunks.add(basepos);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	private void saveBaseChunkData(ChunkPos basepos) {
		Path dirPath = Paths.get("payload", "StashFinder", serverip, world);
		Path filePath = dirPath.resolve("BaseChunkData.txt");
		try {
			Files.createDirectories(dirPath);
			String data = basepos.toString() + System.lineSeparator();
			Files.write(filePath, data.getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	private boolean filterBlocks(Block block) {
		return isNaturalLagCausingBlock(block);
	}
	private boolean isNaturalLagCausingBlock(Block block) {
		return  block instanceof Block &&
				!(block ==Blocks.AIR) &&
				!(block ==Blocks.STONE) &&
				!(block ==Blocks.DIRT) &&
				!(block ==Blocks.GRASS_BLOCK) &&
				!(block ==Blocks.SAND) &&
				!(block ==Blocks.GRAVEL) &&
				!(block ==Blocks.DEEPSLATE) &&
				!(block ==Blocks.WATER) &&
				!(block ==Blocks.NETHERRACK) &&
				!(block ==Blocks.LAVA);
	}
	private void removeChunksOutsideRenderDistance() {
		int topY = Math.round(renderHeightY.get());
		int bottomY = Math.round(renderHeightYBottom.get());
		int midpoint = (topY + bottomY) / 2;
		BlockPos playerPos = new BlockPos(MC.player.getBlockX(), midpoint, MC.player.getBlockZ());
		double renderDistanceBlocks = renderDistance.get() * 16;

		removeChunksOutsideRenderDistance(baseChunks, playerPos, renderDistanceBlocks, midpoint);
	}
	private void removeChunksOutsideRenderDistance(Set<ChunkPos> chunkSet, BlockPos playerPos, double renderDistanceBlocks, int midpoint) {
		chunkSet.removeIf(c -> !playerPos.isWithinDistance(new BlockPos(c.getCenterX(), midpoint, c.getCenterZ()), renderDistanceBlocks));
	}
}
