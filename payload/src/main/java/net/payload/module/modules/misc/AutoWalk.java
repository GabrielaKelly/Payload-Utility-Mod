

/**
 * AutoWalk Module
 */
package net.payload.module.modules.misc;

import net.payload.Payload;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;

public class AutoWalk extends Module implements TickListener {
	public AutoWalk() {
		super("AutoWalk");

		this.setCategory(Category.of("Misc"));
		this.setDescription("Automatically walks for you");
	}

	@Override
	public void onDisable() {
		MC.options.forwardKey.setPressed(false);
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {

	}

	@Override
	public void onTick(Post event) {
		MC.options.forwardKey.setPressed(true);
	}
}
