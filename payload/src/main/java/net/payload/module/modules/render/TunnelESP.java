
/**
 * Trajectory Module
 */
package net.payload.module.modules.render;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.Dir;
import net.payload.utils.render.Render3D;

public class TunnelESP extends Module implements Render3DListener {
	private static final BlockPos.Mutable BP = new BlockPos.Mutable();
	private static final Direction[] DIRECTIONS = { Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST };

	private FloatSetting height = FloatSetting.builder()
			.id("tunnel_esp_height")
			.displayName("Height")
			.description("Height of the rendered box")
			.defaultValue(2f)
			.minValue(0.0f)
			.maxValue(2.0f)
			.step(0.1f)
			.build();

	private BooleanSetting connected = BooleanSetting.builder()
			.id("tunnel_esp_connected")
			.displayName("Connected")
			.description("If neighbouring holes should be connected")
			.defaultValue(true)
			.build();

	private ColorSetting sideColor = ColorSetting.builder()
			.id("tunnel_esp_sidecolor")
			.displayName("Side Color")
			.description("The side color")
			.defaultValue(new Color(91,0,255,50))
			.build();

	public FloatSetting lineThickness = FloatSetting.builder()
			.id("tunnel_esp_thickness")
			.displayName("Line Thickness")
			.defaultValue(0f)
			.minValue(0f)
			.maxValue(5f)
			.step(0.1f)
			.build();

	private final Long2ObjectMap<TChunk> chunks = new Long2ObjectOpenHashMap<>();

	public TunnelESP() {
		super("TunnelESP");
		this.setCategory(Category.of("Render"));
		this.setDescription("Highlights 2x2 tunnels, useful for basehunting");

		// Add settings
		this.addSetting(height);
		this.addSetting(connected);
		this.addSetting(sideColor);
		this.addSetting(lineThickness);
	}


	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	private static int pack(int x, int y, int z) {
		return ((x & 0xFF) << 24) | ((y & 0xFFFF) << 8) | (z & 0xFF);
	}

	private static byte getPackedX(int p) {
		return (byte) (p >> 24 & 0xFF);
	}

	private static short getPackedY(int p) {
		return (short) (p >> 8 & 0xFFFF);
	}

	private static byte getPackedZ(int p) {
		return (byte) (p & 0xFF);
	}

	private void searchChunk(Chunk chunk, TChunk tChunk) {
		// Prepare variables
		Context ctx = new Context();
		IntSet set = new IntOpenHashSet();

		int startX = chunk.getPos().getStartX();
		int startZ = chunk.getPos().getStartZ();

		int endX = chunk.getPos().getEndX();
		int endZ = chunk.getPos().getEndZ();

		// Search for first set of tunnels
		for (int x = startX; x <= endX; x++) {
			for (int z = startZ; z <= endZ; z++) {
				int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x - startX, z - startZ);

				for (short y = (short) MC.world.getBottomY(); y < height; y++) {
					if (isTunnel(ctx, x, y, z)) set.add(pack(x - startX, y, z - startZ));
				}
			}
		}

		// Remove tunnels which are 1 block long
		IntSet positions = new IntOpenHashSet();

		for (IntIterator it = set.iterator(); it.hasNext();) {
			int packed = it.nextInt();

			byte x = getPackedX(packed);
			short y = getPackedY(packed);
			byte z = getPackedZ(packed);

			if (x == 0 || x == 15 || z == 0 || z == 15) positions.add(packed);
			else {
				boolean has = false;

				for (Direction dir : DIRECTIONS) {
					if (set.contains(pack(x + dir.getOffsetX(), y, z + dir.getOffsetZ()))) {
						has = true;
						break;
					}
				}

				if (has) positions.add(packed);
			}
		}

		tChunk.positions = positions;
	}

	private boolean isTunnel(Context ctx, int x, int y, int z) {
		if (!canWalkIn(ctx, x, y, z)) return false;

		TunnelSide s1 = getTunnelSide(ctx, x + 1, y, z);
		if (s1 == TunnelSide.PartiallyBlocked) return false;

		TunnelSide s2 = getTunnelSide(ctx, x - 1, y, z);
		if (s2 == TunnelSide.PartiallyBlocked) return false;

		TunnelSide s3 = getTunnelSide(ctx, x, y, z + 1);
		if (s3 == TunnelSide.PartiallyBlocked) return false;

		TunnelSide s4 = getTunnelSide(ctx, x, y, z - 1);
		if (s4 == TunnelSide.PartiallyBlocked) return false;

		return (s1 == TunnelSide.Walkable && s2 == TunnelSide.Walkable && s3 == TunnelSide.FullyBlocked && s4 == TunnelSide.FullyBlocked) || (s1 == TunnelSide.FullyBlocked && s2 == TunnelSide.FullyBlocked && s3 == TunnelSide.Walkable && s4 == TunnelSide.Walkable);
	}

	private TunnelSide getTunnelSide(Context ctx, int x, int y, int z) {
		if (canWalkIn(ctx, x, y, z)) return TunnelSide.Walkable;
		if (!canWalkThrough(ctx, x, y, z) && !canWalkThrough(ctx, x, y + 1, z)) return TunnelSide.FullyBlocked;
		return TunnelSide.PartiallyBlocked;
	}

	private boolean canWalkOn(Context ctx, int x, int y, int z) {
		BlockState state = ctx.get(x, y, z);

		if (state.isAir()) return false;
		if (!state.getFluidState().isEmpty()) return false;

		return !state.getCollisionShape(MC.world, BP.set(x, y, z)).isEmpty();
	}

	private boolean canWalkThrough(Context ctx, int x, int y, int z) {
		BlockState state = ctx.get(x, y, z);

		if (state.isAir()) return true;
		if (!state.getFluidState().isEmpty()) return false;

		return state.getCollisionShape(MC.world, BP.set(x, y, z)).isEmpty();
	}

	private boolean canWalkIn(Context ctx, int x, int y, int z) {
		if (!canWalkOn(ctx, x, y - 1, z)) return false;
		if (!canWalkThrough(ctx, x, y, z)) return false;
		if (canWalkThrough(ctx, x, y + 2, z)) return false;
		return canWalkThrough(ctx, x, y + 1, z);
	}


	private boolean chunkContains(TChunk chunk, int x, int y, int z) {
		int key;

		if (x == -1) {
			chunk = chunks.get(ChunkPos.toLong(chunk.x - 1, chunk.z));
			key = pack(15, y, z);
		}
		else if (x == 16) {
			chunk = chunks.get(ChunkPos.toLong(chunk.x + 1, chunk.z));
			key = pack(0, y, z);
		}
		else if (z == -1) {
			chunk = chunks.get(ChunkPos.toLong(chunk.x, chunk.z - 1));
			key = pack(x, y, 15);
		}
		else if (z == 16) {
			chunk = chunks.get(ChunkPos.toLong(chunk.x, chunk.z + 1));
			key = pack(x, y, 0);
		}
		else key = pack(x, y, z);

		return chunk != null && chunk.positions != null && chunk.positions.contains(key);
	}

	@Override
	public void onRender(Render3DEvent event) {
		// Don't render if world is null
		if (MC.world == null) return;

		// Update chunks if needed
		int viewDistance = MC.options.getViewDistance().getValue();
		int playerChunkX = (int) (MC.player.getX() / 16);
		int playerChunkZ = (int) (MC.player.getZ() / 16);

		// Clear old chunks and mark existing ones
		chunks.values().forEach(chunk -> chunk.marked = false);

		// Search new chunks and update existing ones
		for (int x = -viewDistance; x <= viewDistance; x++) {
			for (int z = -viewDistance; z <= viewDistance; z++) {
				int chunkX = playerChunkX + x;
				int chunkZ = playerChunkZ + z;
				long key = ChunkPos.toLong(chunkX, chunkZ);

				TChunk chunk = chunks.computeIfAbsent(key, k -> new TChunk(chunkX, chunkZ));
				chunk.marked = true;

				// If chunk doesn't have positions calculated yet, search it
				if (chunk.positions == null) {
					Chunk mcChunk = MC.world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
					if (mcChunk != null) {
						searchChunk(mcChunk, chunk);
					}
				}
			}
		}

		// Remove old chunks
		chunks.values().removeIf(chunk -> !chunk.marked);

		// Render all chunks
		for (TChunk chunk : chunks.values()) {
			renderChunk(event, chunk);
		}
	}

	private void renderChunk(Render3DEvent event, TChunk chunk) {
		if (chunk.positions == null || chunk.positions.isEmpty()) return;

		// Iterate through all positions in the chunk
		for (IntIterator it = chunk.positions.iterator(); it.hasNext();) {
			int pos = it.nextInt();

			int x = getPackedX(pos);
			int y = getPackedY(pos);
			int z = getPackedZ(pos);

			int excludeDir = 0;

			// Handle connected tunnels if enabled
			if (connected.getValue()) {
				for (Direction dir : DIRECTIONS) {
					if (chunkContains(chunk, x + dir.getOffsetX(), y, z + dir.getOffsetZ())) {
						excludeDir |= Dir.get(dir);
					}
				}
			}

			// Convert chunk-local coordinates to world coordinates
			int worldX = (chunk.x * 16) + x;
			int worldZ = (chunk.z * 16) + z;

			// Create box with specified height
			Box box = new Box(
					worldX,
					y,
					worldZ,
					worldX + 1,
					y + height.getValue(),
					worldZ + 1
			);

			// Render the box
			Render3D.draw3DBox(
					event.GetMatrix(), event.getCamera(),
					box,
					sideColor.getValue(),
					lineThickness.getValue()
			);
		}
	}

	private class TChunk {
		private final int x, z;
		public IntSet positions;

		public boolean marked;

		public TChunk(int x, int z) {
			this.x = x;
			this.z = z;
			this.marked = true;
		}

		public long getKey() {
			return ChunkPos.toLong(x, z);
		}
	}

	private static class Context {
		private final World world;

		private Chunk lastChunk;

		public Context() {
			this.world = MC.world;
		}

		public BlockState get(int x, int y, int z) {
			if (world.isOutOfHeightLimit(y)) return Blocks.VOID_AIR.getDefaultState();

			int cx = x >> 4;
			int cz = z >> 4;

			Chunk chunk;

			if (lastChunk != null && lastChunk.getPos().x == cx && lastChunk.getPos().z == cz) chunk = lastChunk;
			else chunk = world.getChunk(cx, cz, ChunkStatus.FULL, false);

			if (chunk == null) return Blocks.VOID_AIR.getDefaultState();

			ChunkSection section = chunk.getSectionArray()[chunk.getSectionIndex(y)];
			if (section == null) return Blocks.VOID_AIR.getDefaultState();

			lastChunk = chunk;
			return section.getBlockState(x & 15, y & 15, z & 15);
		}
	}

	private enum TunnelSide {
		Walkable,
		PartiallyBlocked,
		FullyBlocked
	}
}