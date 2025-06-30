package net.payload.event.events;

import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.AttackCooldownListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class AttackCooldownEvent extends AbstractEvent {


    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            AttackCooldownListener attackCooldownListener = (AttackCooldownListener) listener;
            attackCooldownListener.OnAttack(this);
        }
    }

    @Override
    public Class<AttackCooldownListener> GetListenerClassType() {
        return AttackCooldownListener.class;
    }
}
