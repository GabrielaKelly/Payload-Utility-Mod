/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.payload.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.payload.Payload;
import net.payload.event.events.BreakBlockEvent;
import net.payload.event.events.PlaceBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Shadow
    protected abstract BlockState getPlacementState(ItemPlacementContext context);

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (!context.getWorld().isClient) return;

        PlaceBlockEvent pbe = new PlaceBlockEvent(context.getBlockPos(), state.getBlock());
        Payload.getInstance().eventManager.Fire(pbe);

        if (pbe.isCancelled()) {
            info.setReturnValue(true);
        }
    }

    @ModifyVariable(
        method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
        ordinal = 1,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
        )
    )
    private BlockState modifyState(BlockState state, ItemPlacementContext context) {
        if (Payload.getInstance().moduleManager.noGhostBlocks.state.getValue() && Payload.getInstance().moduleManager.noGhostBlocks.placing.getValue()) {
            return getPlacementState(context);
        }
        return state;
    }
}
