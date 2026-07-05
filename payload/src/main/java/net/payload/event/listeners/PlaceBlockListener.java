package net.payload.event.listeners;

import net.payload.event.events.PlaceBlockEvent;

public interface PlaceBlockListener extends AbstractListener {
    public abstract void onPlace(PlaceBlockEvent event);
}
