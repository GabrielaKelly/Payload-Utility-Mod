/**
 * XRay Module
 */
package net.payload.module.modules.render;

import java.util.HashSet;

import net.payload.gui.Size;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BlocksSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.InputUtil;

public class XRay extends Module {
	public BlocksSetting blocks = BlocksSetting.builder().id("xray_blocks").displayName("Visible Blocks")
			.description("Only these blocks will be visible when XRay is enabled")
			.defaultValue(new HashSet<Block>(Lists.newArrayList(
					Blocks.DIAMOND_ORE,
					Blocks.DEEPSLATE_DIAMOND_ORE,
					Blocks.ANCIENT_DEBRIS,
					Blocks.GOLD_ORE,
					Blocks.DEEPSLATE_GOLD_ORE,
					Blocks.IRON_ORE,
					Blocks.DEEPSLATE_IRON_ORE,
					Blocks.EMERALD_ORE,
					Blocks.DEEPSLATE_EMERALD_ORE,
					Blocks.REDSTONE_ORE,
					Blocks.DEEPSLATE_REDSTONE_ORE,
					Blocks.LAPIS_ORE,
					Blocks.DEEPSLATE_LAPIS_ORE,
					Blocks.CHEST,
					Blocks.TRAPPED_CHEST,
					Blocks.ENDER_CHEST)))
			.onUpdate(this::ReloadRenderer)
			.build();

	public XRay() {
		super("Xray", InputUtil.fromKeyCode(GLFW.GLFW_KEY_X, 0));
		this.setCategory(Category.of("Render"));
		this.setDescription("Makes only selected blocks visible");
		this.addSetting(blocks);
	}

	@Override
	public void onDisable() {
		MC.worldRenderer.reload();
	}

	@Override
	public void onEnable() {
		MC.worldRenderer.reload();
	}

	@Override
	public void onToggle() {
	}

	public boolean isXRayBlock(Block b) {
		return blocks.getValue().contains(b);
	}

	public void ReloadRenderer(HashSet<Block> block) {
		if (MC.worldRenderer != null && state.getValue()) {
			MC.worldRenderer.reload();
		}
	}
}