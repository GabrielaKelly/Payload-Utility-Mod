package net.payload.event.listeners;

import net.payload.event.events.GameLeftEvent;

public interface GameLeftListener extends AbstractListener {
    public abstract void onGameLeft(GameLeftEvent event);
}
