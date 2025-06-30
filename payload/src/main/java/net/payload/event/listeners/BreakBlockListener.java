package net.payload.event.listeners;

import net.payload.event.events.BreakBlockEvent;


public interface BreakBlockListener extends AbstractListener {
    public abstract void onBreak(BreakBlockEvent event);
}
