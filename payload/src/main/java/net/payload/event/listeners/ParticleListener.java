package net.payload.event.listeners;

import net.payload.event.events.ParticleEvent;

public interface ParticleListener extends AbstractListener {
    public abstract void onParticle(ParticleEvent particleEvent);
}
