

package net.payload.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.payload.Payload;
import net.payload.utils.player.combat.CombatUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.payload.PayloadClient.MC;

@Mixin(World.class)
public abstract class WorldMixin {
	
	@Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
	public void blockStateHook(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		if (pos == null) {
			cir.setReturnValue(Blocks.VOID_AIR.getDefaultState());
			return;
		}
		if (MC.world != null && MC.world.isInBuildLimit(pos)) {
			if (CombatUtil.terrainIgnore || CombatUtil.modifyPos != null) {
				WorldChunk worldChunk = MC.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

				BlockState tempState = worldChunk.getBlockState(pos);

				if (CombatUtil.modifyPos != null) {
					if (pos.equals(CombatUtil.modifyPos)) {
						cir.setReturnValue(CombatUtil.modifyBlockState);
						return;
					}
				}

				if (CombatUtil.terrainIgnore) {
					if (tempState.getBlock() == Blocks.OBSIDIAN
							|| tempState.getBlock() == Blocks.BEDROCK
							|| tempState.getBlock() == Blocks.ENDER_CHEST
							|| tempState.getBlock() == Blocks.RESPAWN_ANCHOR
							|| tempState.getBlock() == Blocks.NETHERITE_BLOCK
					) return;
					cir.setReturnValue(Blocks.AIR.getDefaultState());
				}
			} else if (Payload.getInstance().moduleManager.interactTweaks.state.getValue()) {
				WorldChunk worldChunk = MC.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

				BlockState tempState = worldChunk.getBlockState(pos);
				if (tempState.getBlock() == Blocks.BEDROCK
				) {
					cir.setReturnValue(Blocks.AIR.getDefaultState());
				}
			}
		}
	}
	
}
