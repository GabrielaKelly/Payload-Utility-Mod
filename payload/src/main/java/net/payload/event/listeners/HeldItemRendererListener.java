package net.payload.event.listeners;

import net.payload.event.events.AttackEntityEvent;
import net.payload.event.events.HeldItemRendererEvent;


public interface HeldItemRendererListener extends AbstractListener {
    public abstract void onRenderHeld(HeldItemRendererEvent event);
}
