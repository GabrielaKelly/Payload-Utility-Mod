package net.payload.event.events;

import net.minecraft.entity.player.PlayerEntity;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.TravelListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class TravelEvent {
    private final PlayerEntity entity;

    public TravelEvent(PlayerEntity entity) {
        this.entity = entity;
    }

    public PlayerEntity getEntity() {
        return entity;
    }

    public static class Pre extends AbstractEvent {

        private final PlayerEntity entity;

        public Pre(PlayerEntity entity) {
            this.entity = entity;
        }

        @Override
        public void Fire(ArrayList<? extends AbstractListener> listeners) {
            for (AbstractListener listener : List.copyOf(listeners)) {
                TravelListener travelListener = (TravelListener) listener;
                travelListener.onTravelPre(this);
            }
        }

        @Override
        public Class<TravelListener> GetListenerClassType() {
            return TravelListener.class;
        }
    }

    public static class Post extends AbstractEvent {

        private final PlayerEntity entity;

        public Post(PlayerEntity entity) {
            this.entity = entity;
        }

        @Override
        public void Fire(ArrayList<? extends AbstractListener> listeners) {
            for (AbstractListener listener : List.copyOf(listeners)) {
                TravelListener travelListener = (TravelListener) listener;
                travelListener.onTravelPost(this);
            }
        }

        @Override
        public Class<TravelListener> GetListenerClassType() {
            return TravelListener.class;
        }
    }
}
