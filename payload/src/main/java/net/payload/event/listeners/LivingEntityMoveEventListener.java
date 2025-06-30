package net.payload.event.listeners;


import net.payload.event.events.LivingEntityMoveEvent;

public interface LivingEntityMoveEventListener extends AbstractListener {
    public abstract void onEntityMove(LivingEntityMoveEvent event);
}
