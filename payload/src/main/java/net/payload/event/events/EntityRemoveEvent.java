package net.payload.event.events;

import net.minecraft.entity.Entity;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.EntityRemoveListener;
import java.util.ArrayList;
import java.util.List;

public class EntityRemoveEvent extends AbstractEvent {

    private Entity entity;
    private final Entity.RemovalReason removalReason;

    public EntityRemoveEvent(Entity entityById, Entity.RemovalReason removalReason) {
        this.entity = entityById;
        this.removalReason = removalReason;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Entity.RemovalReason getRemovalReason() {
        return this.removalReason;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            EntityRemoveListener entityRemoveListener = (EntityRemoveListener) listener;
            entityRemoveListener.onDelete(this);
        }
    }

    @Override
    public Class<EntityRemoveListener> GetListenerClassType() {
        return EntityRemoveListener.class;
    }
}
