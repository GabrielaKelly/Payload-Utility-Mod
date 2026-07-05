

/**
 * Sneak Module
 */
package net.payload.module.modules.movement;

import net.payload.Payload;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class Sneak extends Module implements TickListener {

	private final MinecraftClient MC = MinecraftClient.getInstance();

	public Sneak() {
		super("Sneak");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Force the player to sneak");
	}

	@Override
	public void onDisable() {
		ClientPlayerEntity player = MC.player;
		if (player != null) {
			MC.options.sneakKey.setPressed(false);
		}
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
		ClientPlayerEntity player = MC.player;
		if (player != null) {
			MC.options.sneakKey.setPressed(true);
		}
	}

	@Override
	public void onTick(Post event) {

	}
}