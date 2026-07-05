package net.payload.event.listeners;

import net.payload.event.events.AttackCooldownEvent;
import net.payload.event.events.RotateEvent;


public interface RotateListener extends AbstractListener {
    public abstract void onLastRotate(RotateEvent event);
}
