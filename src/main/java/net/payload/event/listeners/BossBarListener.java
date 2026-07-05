package net.payload.event.listeners;

import net.payload.event.events.BossBarEvent;


public interface BossBarListener extends AbstractListener {
    public abstract void onRender(BossBarEvent renderEvent);
}
