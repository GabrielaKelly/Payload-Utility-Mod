package net.payload.module.modules.movement;

import net.payload.module.Category;
import net.payload.module.Module;

public class Safewalk extends Module {

	public Safewalk() {
		super("Safewalk");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Prevents the player from falling off blocks while not sneaking");
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
