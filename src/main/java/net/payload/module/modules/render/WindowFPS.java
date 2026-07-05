package net.payload.module.modules.render;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;

public class WindowFPS extends Module {
	private FloatSetting fps = FloatSetting.builder().id("focusfps_fps").displayName("FPS")
			.description("The FPS for when the window is not in focus.").defaultValue(20f).minValue(1f).maxValue(60f)
			.step(10f).build();

	public WindowFPS() {
		super("WindowFPS");
		this.setCategory(Category.of("Render"));
		this.setDescription("Limits the FPS of the game when it is not focused");
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

	public Float getFps() {
		return this.fps.getValue();
	}
}
