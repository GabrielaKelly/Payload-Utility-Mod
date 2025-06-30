

/**
 * SpawnerESP Module
 */
package net.payload.module.modules.render;

import java.util.ArrayList;
import java.util.stream.Collectors;

import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.ModuleUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.Box;
import net.payload.utils.render.Render3D;

public class SpawnerESP extends Module implements Render3DListener {

	private ColorSetting color = ColorSetting.builder().id("spawneresp_color").displayName("Color").description("Color")
			.defaultValue(new Color(0f, 1f, 1f, 0.3f)).build();

	private FloatSetting lineThickness = FloatSetting.builder().id("spawneresp_linethickness")
			.displayName("Line Thickness").description("Adjust the thickness of the ESP box lines").defaultValue(2f)
			.minValue(0f).maxValue(5f).step(0.1f).build();

	public SpawnerESP() {
		super("SpawnerESP");
		this.setCategory(Category.of("Render"));
		this.setDescription("Allows the player to see spawners through walls, independent of whether they are activated or not");

		this.addSetting(color);
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

	@Override
	public void onRender(Render3DEvent event) {
		ArrayList<BlockEntity> blockEntities = ModuleUtils.getTileEntities()
				.collect(Collectors.toCollection(ArrayList::new));

		for (BlockEntity blockEntity : blockEntities) {
			if (blockEntity instanceof MobSpawnerBlockEntity) {
				Box box = new Box(blockEntity.getPos());
				Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), box, color.getValue(), lineThickness.getValue().floatValue());
			}
		}
	}
}