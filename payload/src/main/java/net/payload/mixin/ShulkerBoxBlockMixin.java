package net.payload.mixin;

import java.util.List;

import net.payload.Payload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.payload.module.modules.render.Tooltips;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {
	@Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
	private void onAppendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options,
			CallbackInfo info) {
		Tooltips tooltips = (Tooltips) Payload.getInstance().moduleManager.tooltips;

		if (tooltips.getStorage() && tooltips.state.getValue())
			info.cancel();
	}
}