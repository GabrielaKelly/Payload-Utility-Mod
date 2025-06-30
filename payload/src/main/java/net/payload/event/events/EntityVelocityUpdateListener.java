package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;


public interface EntityVelocityUpdateListener extends AbstractListener {
    public abstract void onUpdateVel(EntityVelocityUpdateEvent event);
}
