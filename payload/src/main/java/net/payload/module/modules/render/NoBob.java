
/**
 * Trajectory Module
 */
package net.payload.module.modules.render;

import net.payload.module.Category;
import net.payload.module.Module;

public class NoBob extends Module {

	public NoBob() {
		super("NoBob");
		this.setCategory(Category.of("Render"));
		this.setDescription("Deletes Bobbing");
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