package net.payload.event.listeners;

import net.payload.event.events.KeyUpEvent;

public interface KeyUpListener extends AbstractListener {
    public abstract void onKeyUp(KeyUpEvent event);
}