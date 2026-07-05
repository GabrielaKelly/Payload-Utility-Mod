

package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;

import java.util.ArrayList;

public abstract class AbstractEvent {
    boolean isCancelled;

    public AbstractEvent() {
        isCancelled = false;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancel() {
        this.isCancelled = true;
    }

    public abstract void Fire(ArrayList<? extends AbstractListener> listeners);

    public abstract <T extends AbstractListener> Class<T> GetListenerClassType();
}
