

/**
 * AntiCactus Module
 */
package net.payload.module.modules.movement;

import net.payload.module.Category;
import net.payload.module.Module;

public class NoWaterCollision extends Module {

	public NoWaterCollision() {
		super("NoWaterCollision");

		this.setCategory(Category.of("Movement"));
		this.setDescription("Deletes the effect water has on movement entirely");
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