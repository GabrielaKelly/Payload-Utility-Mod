
package net.payload.module.modules.render;

import net.payload.module.Category;
import net.payload.module.Module;

public class Fullbright extends Module {

	// private double previousValue = 0.0;
	public Fullbright() {
		super("Fullbright");
		this.setCategory(Category.of("Render"));
		this.setDescription("Maxes out the brightness");
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
