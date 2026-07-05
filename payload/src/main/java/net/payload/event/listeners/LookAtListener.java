package net.payload.event.listeners;

import net.payload.event.events.AttackCooldownEvent;
import net.payload.event.events.LookAtEvent;


public interface LookAtListener extends AbstractListener {
    public abstract void onLook(LookAtEvent event);
}
