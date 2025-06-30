package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.JumpListener;

import java.util.ArrayList;
import java.util.List;

public class JumpEvent {
    public static class Pre extends AbstractEvent {

        @Override
        public void Fire(ArrayList<? extends AbstractListener> listeners) {
            for (AbstractListener listener : List.copyOf(listeners)) {
                JumpListener jumpListener = (JumpListener) listener;
                jumpListener.onJumpPre(this);
            }
        }

        @Override
        public Class<JumpListener> GetListenerClassType() {
            return JumpListener.class;
        }
    }

    public static class Post extends AbstractEvent {
        @Override
        public void Fire(ArrayList<? extends AbstractListener> listeners) {
            for (AbstractListener listener : List.copyOf(listeners)) {
                JumpListener jumpListener = (JumpListener) listener;
                jumpListener.onJumpPost(this);
            }
        }

        @Override
        public Class<JumpListener> GetListenerClassType() {
            return JumpListener.class;
        }
    }

}
