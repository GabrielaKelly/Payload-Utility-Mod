

/**
 * Jesus Module
 */
package net.payload.module.modules.movement;

import net.payload.Payload;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

public class Jesus extends Module implements TickListener {

	public BooleanSetting legit = BooleanSetting.builder().id("jesus_legit").displayName("Legit")
			.description("Whether or not the player will swim as close to the surface as possible.").defaultValue(true)
			.build();

	public Jesus() {
		super("Jesus");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Allows the player to walk on water");
		this.addSetting(legit);
	}

	@Override
	public void onDisable() {
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
		// If Legit is enabled, simply swim.
		if (this.legit.getValue()) {
			if (MC.player.isInLava() || MC.player.isTouchingWater()) {
				MC.options.jumpKey.setPressed(true);
			}
		}
	}

	@Override
	public void onTick(Post event) {

	}
}
