package net.payload.event.listeners;

import net.payload.event.events.AttackCooldownEvent;
import net.payload.event.events.MovementPacketsEvent;


public interface MovementPacketsListener extends AbstractListener {
    public abstract void onUpdate(MovementPacketsEvent event);
}
