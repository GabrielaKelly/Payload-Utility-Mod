package net.payload.event.listeners;

import net.payload.event.events.AttackCooldownEvent;


public interface AttackCooldownListener extends AbstractListener {
    public abstract void OnAttack(AttackCooldownEvent readPacketEvent);
}
