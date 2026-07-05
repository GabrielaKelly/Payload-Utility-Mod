package net.payload.event.listeners;


import net.payload.event.events.PlayerMoveEvent;

public interface PlayerMoveEventListener extends AbstractListener {
    public abstract void onMove(PlayerMoveEvent event);
}
