package net.payload.event.listeners;

import net.payload.event.events.RemoveFireworkEvent;


public interface RemoveFireworkListener extends AbstractListener {
    public abstract void OnRemoveFirework(RemoveFireworkEvent removeFireworkEvent);
}
