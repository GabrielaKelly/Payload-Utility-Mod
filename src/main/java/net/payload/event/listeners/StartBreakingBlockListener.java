package net.payload.event.listeners;

import net.payload.event.events.StartBreakingBlockEvent;


public interface StartBreakingBlockListener extends AbstractListener {
    public abstract void onBreak(StartBreakingBlockEvent even);
}
