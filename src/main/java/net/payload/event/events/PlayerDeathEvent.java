

package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.PlayerDeathListener;

import java.util.ArrayList;
import java.util.List;

public class PlayerDeathEvent extends AbstractEvent {
    public PlayerDeathEvent() {
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            PlayerDeathListener playerDeathListener = (PlayerDeathListener) listener;
            playerDeathListener.onPlayerDeath(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<PlayerDeathListener> GetListenerClassType() {
        return PlayerDeathListener.class;
    }
}