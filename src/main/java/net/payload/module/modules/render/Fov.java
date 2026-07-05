package net.payload.module.modules.render;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;

public class Fov extends Module {

	public final FloatSetting customFov = FloatSetting.builder()
			.id("fov_customfov")
			.displayName("Camera Fov")
			.description("Custom cam fov")
			.defaultValue(120f)
			.minValue(30f)
			.maxValue(175f)
			.step(5f)
			.build();

	public final FloatSetting itemFov = FloatSetting.builder()
			.id("fov_itemfov")
			.displayName("HeldItem Fov")
			.description("Custom item fov")
			.defaultValue(70f)
			.minValue(30f)
			.maxValue(175f)
			.step(5f)
			.build();

	public Fov() {
		super("FOV");
		this.setName("Custom FOV");
		this.setCategory(Category.of("Render"));
		this.setDescription("Changes your field of vision");

		this.addSetting(customFov);
		this.addSetting(itemFov);
	}

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onToggle() {

	}
}