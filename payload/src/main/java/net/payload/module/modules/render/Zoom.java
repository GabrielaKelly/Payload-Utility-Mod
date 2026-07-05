

package net.payload.module.modules.render;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;

public class Zoom extends Module {

	public FloatSetting zoomFactor = FloatSetting.builder().id("zoom_factor").displayName("Intensity")
			.description("The zoom factor to zoom in").defaultValue(50f).minValue(1f).maxValue(200f)
			.step(10f).build();

	public Zoom() {
		super("Zoom");
		this.setCategory(Category.of("Render"));
		this.setDescription("Zooms the players camera in like optifine used to do");
		this.addSetting(zoomFactor);
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
