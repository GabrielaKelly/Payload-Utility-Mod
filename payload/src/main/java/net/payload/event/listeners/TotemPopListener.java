package net.payload.event.listeners;


import net.payload.event.events.TotemPopEvent;

public interface TotemPopListener extends AbstractListener {
    public abstract void onTotemPop(TotemPopEvent event);
}
