package net.payload.event.listeners;

import net.payload.event.events.MouseClickEvent;

public interface MouseClickListener extends AbstractListener {
    public abstract void onMouseClick(MouseClickEvent mouseClickEvent);
}
