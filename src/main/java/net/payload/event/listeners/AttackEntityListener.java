package net.payload.event.listeners;

import net.payload.event.events.AttackCooldownEvent;
import net.payload.event.events.AttackEntityEvent;


public interface AttackEntityListener extends AbstractListener {
    public abstract void onAttack(AttackEntityEvent event);
}
