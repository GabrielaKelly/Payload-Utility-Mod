package net.payload.event.listeners;

import net.payload.event.events.EntityRemoveEvent;


public interface EntityRemoveListener extends AbstractListener {
    public abstract void onDelete(EntityRemoveEvent event);
}
