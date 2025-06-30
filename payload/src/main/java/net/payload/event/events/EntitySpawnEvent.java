package net.payload.event.events;

import net.minecraft.entity.Entity;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.EntitySpawnListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class EntitySpawnEvent extends AbstractEvent {

    private Entity entity;

    public EntitySpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            EntitySpawnListener entitySpawnListener = (EntitySpawnListener) listener;
            entitySpawnListener.onSpawn(this);
        }
    }

    @Override
    public Class<EntitySpawnListener> GetListenerClassType() {
        return EntitySpawnListener.class;
    }
}
