package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.TotemPopListener;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class TotemPopEvent extends AbstractEvent {
    private final PlayerEntity entity;

    public TotemPopEvent(PlayerEntity entity) {
        this.entity = entity;
    }

    public PlayerEntity getEntity() {
        return this.entity;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            TotemPopListener totemPopListener = (TotemPopListener) listener;
            totemPopListener.onTotemPop(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<TotemPopListener> GetListenerClassType() {
        return TotemPopListener.class;
    }
}
