package net.payload.event.events;

import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.ParticleAddListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class ParticleAddEvent extends AbstractEvent {

    public Particle particle;

    public ParticleAddEvent(Particle particle) {
        this.particle = particle;
    }

    public ParticleEffect emmiter;

    public ParticleAddEvent(ParticleEffect emmiter){
        this.emmiter = emmiter;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            ParticleAddListener particleAddListener = (ParticleAddListener) listener;
            particleAddListener.onParticleSpawn(this);
        }
    }

    @Override
    public Class<ParticleAddListener> GetListenerClassType() {
        return ParticleAddListener.class;
    }
}
