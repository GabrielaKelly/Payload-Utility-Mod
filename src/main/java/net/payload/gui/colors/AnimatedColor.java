package net.payload.gui.colors;

import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;

public abstract class AnimatedColor extends Color implements TickListener {
    public AnimatedColor() {
        super(255, 0, 0);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onTick(TickEvent.Pre event) { }
    
    @Override
    public abstract void onTick(TickEvent.Post event);
}
