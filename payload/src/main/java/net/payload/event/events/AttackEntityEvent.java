package net.payload.event.events;

import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.AttackCooldownListener;
import net.payload.event.listeners.AttackEntityListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class AttackEntityEvent extends AbstractEvent {


    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            AttackEntityListener attackEntityListener = (AttackEntityListener) listener;
            attackEntityListener.onAttack(this);
        }
    }

    @Override
    public Class<AttackEntityListener> GetListenerClassType() {
        return AttackEntityListener.class;
    }
}
