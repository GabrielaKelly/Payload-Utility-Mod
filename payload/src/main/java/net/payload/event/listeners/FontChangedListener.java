package net.payload.event.listeners;

import net.payload.event.events.FontChangedEvent;

public interface FontChangedListener extends AbstractListener {
    public abstract void onFontChanged(FontChangedEvent event);
}
