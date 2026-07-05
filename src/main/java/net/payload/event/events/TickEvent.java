package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.TickListener;
import java.util.ArrayList;
import java.util.List;

public class TickEvent {
    public static class Pre extends AbstractEvent {
        @Override
        public void Fire(ArrayList<? extends AbstractListener> listeners) {
            for (AbstractListener listener : List.copyOf(listeners)) {
                TickListener tickListener = (TickListener) listener;
                tickListener.onTick(this);
            }
        }

        @Override
        public Class<TickListener> GetListenerClassType() {
            return TickListener.class;
        }
    }

    public static class Post extends AbstractEvent {
        @Override
        public void Fire(ArrayList<? extends AbstractListener> listeners) {
            for (AbstractListener listener : List.copyOf(listeners)) {
                TickListener tickListener = (TickListener) listener;
                tickListener.onTick(this);
            }
        }

        @Override
        public Class<TickListener> GetListenerClassType() {
            return TickListener.class;
        }
    }

}
