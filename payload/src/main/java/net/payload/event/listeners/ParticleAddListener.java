package net.payload.event.listeners;

import net.payload.event.events.ParticleAddEvent;


public interface ParticleAddListener extends AbstractListener {
    public abstract void onParticleSpawn(ParticleAddEvent event);
}
