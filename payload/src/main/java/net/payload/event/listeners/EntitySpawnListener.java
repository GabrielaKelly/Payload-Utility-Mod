package net.payload.event.listeners;

import net.payload.event.events.EntitySpawnEvent;


public interface EntitySpawnListener extends AbstractListener {
    public abstract void onSpawn(EntitySpawnEvent readPacketEvent);
}
