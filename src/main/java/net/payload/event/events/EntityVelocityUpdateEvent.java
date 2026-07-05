package net.payload.event.events;

import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.AttackCooldownListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class EntityVelocityUpdateEvent extends AbstractEvent {


    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            EntityVelocityUpdateListener velocityUpdateListener = (EntityVelocityUpdateListener) listener;
            velocityUpdateListener.onUpdateVel(this);
        }
    }

    @Override
    public Class<EntityVelocityUpdateListener> GetListenerClassType() {
        return EntityVelocityUpdateListener.class;
    }
}
