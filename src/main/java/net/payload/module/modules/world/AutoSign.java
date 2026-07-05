

/**
 * AutoSign Module
 */
package net.payload.module.modules.world;

import net.payload.cmd.CommandManager;
import net.payload.module.Category;
import net.payload.module.Module;

public class AutoSign extends Module {
	String[] text;

	public AutoSign() {
		super("AutoSign");
		this.setCategory(Category.of("World"));
		this.setDescription("Automatically places sign with predefined text");
	}

	public void setText(String[] text) {
		this.text = text;
	}

	public String[] getText() {
		return this.text;
	}

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		CommandManager.sendChatMessage("Place down and edit a sign to set its text");
		this.text = null;
	}

	@Override
	public void onToggle() {
	}
}
