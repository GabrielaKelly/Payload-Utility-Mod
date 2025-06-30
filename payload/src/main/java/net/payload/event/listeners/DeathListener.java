package net.payload.event.listeners;

import net.payload.event.events.DeathEvent;


public interface DeathListener extends AbstractListener {
    public abstract void onDeath(DeathEvent event);
}
