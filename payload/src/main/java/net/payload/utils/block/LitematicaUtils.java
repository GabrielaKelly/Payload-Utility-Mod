package net.payload.utils.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.payload.Payload;
import net.payload.event.events.RotateEvent;
import net.payload.event.listeners.RotateListener;
import net.payload.module.modules.client.AntiCheat;
import net.payload.utils.rotation.RotationLitematica;
import net.payload.utils.rotation.RotationManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.payload.PayloadClient.MC;
import static net.payload.utils.block.BlockUtils.canPlace;

public class LitematicaUtils {

	public static boolean place(BlockPos blockPos, Direction direction, SlabType slabType, BlockHalf blockHalf,
								Direction blockHorizontalOrientation, Axis wantedAxies, boolean airPlace,
								boolean swingHand, boolean rotate, int range) {
		if (MC.player == null) return false;
		if (!canPlace(blockPos)) return false;

		Vec3d hitPos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
		BlockPos neighbour;
		Direction s = direction;

		if (direction == null) {
			if ((slabType != null && slabType != SlabType.DOUBLE || blockHalf != null ||
					blockHorizontalOrientation != null || wantedAxies != null) && !MC.player.isCreative()) return false;
			direction = Direction.UP;
			neighbour = blockPos;
		} else if(airPlace) {
			neighbour = blockPos;
		} else {
			neighbour = blockPos.offset(direction.getOpposite());
			hitPos = hitPos.add(direction.getOffsetX() * 0.5, direction.getOffsetY() * 0.5, direction.getOffsetZ() * 0.5);
		}

		if (rotate) {
			VoxelShape collisionShape = MC.world.getBlockState(neighbour).getCollisionShape(MC.world, neighbour);

			if(collisionShape.isEmpty()) {
				// Use RotationManager for rotation
				RotationManager rotationManager = Payload.getInstance().rotationManager;

				Vec3d finalHitPos = hitPos;
				Runnable placementAction = () -> place(new BlockHitResult(finalHitPos, s, neighbour, false), swingHand);

					rotationManager.rotationTo(hitPos);
					registerPlacementCallback(placementAction);

				return true;
			}

			Box aabb = collisionShape.getBoundingBox();

			for (double z = 0.1; z < 0.9; z+=0.2)
				for (double x = 0.1; x < 0.9; x+=0.2)
					for (Vec3d placementMultiplier : aabbSideMultipliers(direction.getOpposite())) {
						double placeX = neighbour.getX() + aabb.minX * x + aabb.maxX * (1 - x);
						if((slabType != null && slabType != SlabType.DOUBLE || blockHalf != null &&
								direction != Direction.UP && direction != Direction.DOWN) && !MC.player.isCreative()) {
							if (slabType == SlabType.BOTTOM || blockHalf == BlockHalf.BOTTOM) {
								if (placementMultiplier.y <= 0.5) continue;
							} else {
								if (placementMultiplier.y > 0.5) continue;
							}
						}
						double placeY = neighbour.getY() + aabb.minY * placementMultiplier.y + aabb.maxY * (1 - placementMultiplier.y);
						double placeZ = neighbour.getZ() + aabb.minZ * z + aabb.maxZ * (1 - z);

						Vec3d testHitPos = new Vec3d(placeX, placeY, placeZ);
						Vec3d playerHead = new Vec3d(MC.player.getX(), MC.player.getEyeY(), MC.player.getZ());

						RotationLitematica rot = RotationLitematica.RotationStuff.calcRotationFromVec3d(playerHead, testHitPos,
								new RotationLitematica(MC.player.getYaw(), MC.player.getPitch()));
						Direction testHorizontalDirection = getHorizontalDirectionFromYaw(rot.normalize().getYaw());

						if (blockHorizontalOrientation != null &&
								(testHorizontalDirection.getAxis() != blockHorizontalOrientation.getAxis())) continue;

						HitResult res = RotationLitematica.RotationStuff.rayTraceTowards(MC.player, rot, range);
						BlockHitResult blockHitRes = ((BlockHitResult) res);

						if(res == null || res.getType() != HitResult.Type.BLOCK ||
								!blockHitRes.getBlockPos().equals(neighbour) ||
								blockHitRes.getSide() != direction) continue;

						// Use RotationManager for rotation
						RotationManager rotationManager = Payload.getInstance().rotationManager;

						// Create a runnable for the placement action
						Runnable placementAction = () -> place(new BlockHitResult(testHitPos, s, neighbour, false), swingHand);

							rotationManager.rotationTo(testHitPos);
							registerPlacementCallback(placementAction);

						return true;
					}
		} else {
			place(new BlockHitResult(hitPos, s, neighbour, false), swingHand);
		}

		return true;
	}

	private static void place(BlockHitResult blockHitResult, boolean swing) {
		if (MC.player == null || MC.interactionManager == null || MC.getNetworkHandler() == null) return;
		boolean wasSneaking = MC.options.sneakKey.wasPressed();
		MC.player.setSneaking(false);

		ActionResult result = MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, blockHitResult);

		if (result.isAccepted()) {
			if (swing) MC.player.swingHand(Hand.MAIN_HAND);
			else MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
		}

		MC.player.setSneaking(wasSneaking);
	}

	public static boolean isBlockInLineOfSight(BlockPos placeAt, BlockState placeAtState) {
		Vec3d playerHead = new Vec3d(MC.player.getX(), MC.player.getEyeY(), MC.player.getZ());
		Vec3d placeAtVec = new Vec3d(placeAt.getX() + 0.5, placeAt.getY() + 0.5, placeAt.getZ() + 0.5);
		
		ShapeType type = ShapeType.COLLIDER;
		FluidHandling fluid = FluidHandling.NONE;
		
		RaycastContext context =
			new RaycastContext(playerHead, placeAtVec, type, fluid, MC.player);
		BlockHitResult bhr = MC.world.raycast(context);
			// check line of sight		
		return (bhr.getType() == HitResult.Type.MISS);
		
	}

	public static boolean isBlockSameAsPlaceDir(Block block) {
		return block instanceof HopperBlock;
	}

	public static boolean isBlockPlacementOppositeToPlacePos(Block block) {
		return block instanceof AmethystClusterBlock
				|| block instanceof EndRodBlock
				|| block instanceof LightningRodBlock
				|| block instanceof TrapdoorBlock 
				|| block instanceof ChainBlock
				|| block == Blocks.OAK_LOG 
				|| block == Blocks.SPRUCE_LOG 
				|| block == Blocks.BIRCH_LOG 
				|| block == Blocks.JUNGLE_LOG 
				|| block == Blocks.ACACIA_LOG 
				|| block == Blocks.DARK_OAK_LOG 
				|| block == Blocks.STRIPPED_SPRUCE_LOG 
				|| block == Blocks.STRIPPED_BIRCH_LOG 
				|| block == Blocks.STRIPPED_JUNGLE_LOG 
				|| block == Blocks.STRIPPED_ACACIA_LOG 
				|| block == Blocks.STRIPPED_DARK_OAK_LOG
				;
	}

	public static boolean isBlockLikeButton(Block block) {
		return block instanceof ButtonBlock 
				|| block instanceof BellBlock 
				|| block instanceof GrindstoneBlock
				|| block instanceof TrapdoorBlock
				;
	}

	public static boolean isFaceDesired(Block block, Direction blockHorizontalOrientation, Direction against) {
		return blockHorizontalOrientation == null || !(isBlockSameAsPlaceDir(block) || isBlockPlacementOppositeToPlacePos(block)) || (
				isBlockSameAsPlaceDir(block) && blockHorizontalOrientation == against  
				|| block instanceof TrapdoorBlock && against.getOpposite() == blockHorizontalOrientation
				|| !(block instanceof TrapdoorBlock) && (
        		isBlockPlacementOppositeToPlacePos(block) && blockHorizontalOrientation == against.getOpposite()
        		|| isBlockLikeButton(block) && against != Direction.UP && against != Direction.DOWN && blockHorizontalOrientation == against)
        		);
	}
	
	public static boolean isPlayerOrientationDesired(Block block, Direction blockHorizontalOrientation, Direction playerOrientation) {
		return blockHorizontalOrientation == null
				|| ( 	
				block instanceof StairsBlock && playerOrientation == blockHorizontalOrientation || 
				!(block instanceof StairsBlock) &&
				!isBlockPlacementOppositeToPlacePos(block) && !isBlockSameAsPlaceDir(block) && playerOrientation == blockHorizontalOrientation.getOpposite()
				
					);
	}

	public static Direction getPlaceSide(BlockPos blockPos, BlockState placeAtState, SlabType slabType, BlockHalf blockHalf, Direction blockHorizontalOrientation, Axis wantedAxies, Direction requiredDir) {
        for (Direction side : Direction.values()) {
        	
            BlockPos neighbor = blockPos.offset(side);
            Direction side2 = side.getOpposite();
            
        	if(wantedAxies != null && side.getAxis() != wantedAxies || blockHalf != null && (side == Direction.UP && blockHalf == BlockHalf.BOTTOM || side == Direction.DOWN && blockHalf == BlockHalf.TOP))
        		continue;
        	
        	
        	if((slabType != null && slabType != SlabType.DOUBLE || blockHalf != null) && !MC.player.isCreative()) {
				if (slabType == SlabType.BOTTOM || blockHalf == BlockHalf.BOTTOM) {
					if (side2 == Direction.DOWN) continue;
				} else {
					if (side2 == Direction.UP) continue;
				}
			}
            BlockState state = MC.world.getBlockState(neighbor);
            if (wantedAxies == null && !isFaceDesired(placeAtState.getBlock(), blockHorizontalOrientation, side) || wantedAxies != null && wantedAxies != side.getAxis()) continue;

            // Check if neighbour isn't empty
            if (state.isAir() || BlockUtils.isClickable(state.getBlock()) || state.contains(Properties.SLAB_TYPE) 
            		&& (state.get(Properties.SLAB_TYPE) == SlabType.DOUBLE 
            		|| side == Direction.UP && state.get(Properties.SLAB_TYPE) == SlabType.TOP 
            		|| side == Direction.DOWN && state.get(Properties.SLAB_TYPE) == SlabType.BOTTOM 
            		)) continue;

            // Check if neighbour is a fluid
            if (!state.getFluidState().isEmpty()) continue;
            
            Vec3d hitPos = new Vec3d(neighbor.getX(), neighbor.getY(), neighbor.getZ());
 	        Vec3d playerHead = new Vec3d(MC.player.getX(), MC.player.getEyeY(), MC.player.getZ());
 			RotationLitematica rot = RotationLitematica.RotationStuff.calcRotationFromVec3d(playerHead, hitPos, new RotationLitematica(MC.player.getYaw(), MC.player.getPitch()));
			
			Direction testHorizontalDirection = getHorizontalDirectionFromYaw(rot.normalize().getYaw());

			if (placeAtState.getBlock() instanceof TrapdoorBlock && !(side != Direction.DOWN && side != Direction.UP) && !isPlayerOrientationDesired(placeAtState.getBlock(), blockHorizontalOrientation, testHorizontalDirection)
					|| !(placeAtState.getBlock() instanceof TrapdoorBlock) && !isPlayerOrientationDesired(placeAtState.getBlock(), blockHorizontalOrientation, testHorizontalDirection)
					) continue;
            
            return side2;
        }

        return null;
    }

	private static Vec3d[] aabbSideMultipliers(Direction side) {
        switch (side) {
            case UP:
                return new Vec3d[]{new Vec3d(0.5, 1, 0.5), new Vec3d(0.1, 1, 0.5), new Vec3d(0.9, 1, 0.5), new Vec3d(0.5, 1, 0.1), new Vec3d(0.5, 1, 0.9)};
            case DOWN:
                return new Vec3d[]{new Vec3d(0.5, 0, 0.5), new Vec3d(0.1, 0, 0.5), new Vec3d(0.9, 0, 0.5), new Vec3d(0.5, 0, 0.1), new Vec3d(0.5, 0, 0.9)};
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                double x = side.getOffsetX() == 0 ? 0.5 : (1 + side.getOffsetX()) / 2D;
                double z = side.getOffsetZ() == 0 ? 0.5 : (1 + side.getOffsetZ()) / 2D;
                return new Vec3d[]{new Vec3d(x, 0.25, z), new Vec3d(x, 0.75, z)};
            default: // null
                throw new IllegalStateException();
        }
    }

	public static Direction getHorizontalDirectionFromYaw(float yaw) {
        yaw %= 360.0F;
        if (yaw < 0) {
            yaw += 360.0F;
        }

        if ((yaw >= 45 && yaw < 135) || (yaw >= -315 && yaw < -225)) {
            return Direction.WEST;
        } else if ((yaw >= 135 && yaw < 225) || (yaw >= -225 && yaw < -135)) {
            return Direction.NORTH;
        } else if ((yaw >= 225 && yaw < 315) || (yaw >= -135 && yaw < -45)) {
            return Direction.EAST;
        } else {
            return Direction.SOUTH;
        }
    }

	private static final Queue<Runnable> placementCallbacks = new ConcurrentLinkedQueue<>();
	private static boolean callbacksRegistered = false;

	private static void registerPlacementCallback(Runnable callback) {
		placementCallbacks.add(callback);

		if (!callbacksRegistered) {
			callbacksRegistered = true;
			Payload.getInstance().eventManager.AddListener(RotateListener.class, new RotateListener() {
				@Override
				public void onLastRotate(RotateEvent event) {
					// Check if rotation is complete
					if (Payload.getInstance().rotationManager.getDirVec() == null ||
							RotationManager.ROTATE_TIMER.passed((long) (AntiCheat.INSTANCE.rotateTime.getValue() * 1000))) {
						// Execute and remove the next callback if available
						Runnable callback = placementCallbacks.poll();
						if (callback != null) {
							callback.run();
						}

						// If no more callbacks, unregister the listener
						if (placementCallbacks.isEmpty()) {
							Payload.getInstance().eventManager.RemoveListener(RotateListener.class, this);
							callbacksRegistered = false;
						}
					}
				}
			});
		}
	}
}
