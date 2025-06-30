package net.payload.event.listeners;

import net.payload.event.events.AttackCooldownEvent;
import net.payload.event.events.UpdateVelocityEvent;


public interface UpdateVelocityListener extends AbstractListener {
    public abstract void onUpdateVel(UpdateVelocityEvent event);
}
