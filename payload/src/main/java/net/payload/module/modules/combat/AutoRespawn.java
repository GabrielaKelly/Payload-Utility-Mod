

/**
 * AutoRespawn Module
 */
package net.payload.module.modules.combat;

import net.payload.Payload;
import net.payload.event.events.PlayerDeathEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.PlayerDeathListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;

public class AutoRespawn extends Module implements PlayerDeathListener, TickListener {

    private FloatSetting respawnDelay;

    private int tick;

    public AutoRespawn() {
    	super("AutoRespawn");

        this.setCategory(Category.of("Combat"));
        this.setDescription("Automatically respawn when you die");

        respawnDelay = FloatSetting.builder()
        		.id("autorespawn_delay")
        		.displayName("Delay")
        		.description("The delay between dying and automatically respawning.")
        		.defaultValue(1.0f)
        		.minValue(1.0f)
        		.maxValue(100.0f)
        		.step(1.0f)
        		.build();
        		
        this.addSetting(respawnDelay);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(PlayerDeathListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(PlayerDeathListener.class, this);
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent readPacketEvent) {
        if (nullCheck()) return;

        if (respawnDelay.getValue() == 0.0f) {
            respawn();
        } else {
            tick = 0;
            Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        }
    }

    @Override
    public void onTick(TickEvent.Pre event) {

    }
    
    @Override
    public void onTick(TickEvent.Post event) {
        if (nullCheck()) return;

        if (tick < respawnDelay.getValue()) {
            tick++;
        } else {
            respawn();
        }
    }

    private void respawn() {
        if (nullCheck()) return;
        MC.player.requestRespawn();
        MC.setScreen(null);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }
}
