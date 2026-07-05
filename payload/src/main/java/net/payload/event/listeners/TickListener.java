

package net.payload.event.listeners;

import net.payload.event.events.TickEvent;

public interface TickListener extends AbstractListener {
    public abstract void onTick(TickEvent.Pre event);
    public abstract void onTick(TickEvent.Post event);
}