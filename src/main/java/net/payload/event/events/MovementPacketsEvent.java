package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.AttackCooldownListener;
import net.payload.event.listeners.MovementPacketsListener;

import java.util.ArrayList;
import java.util.List;

public class MovementPacketsEvent extends AbstractEvent {
    private float yaw;
    private float pitch;

    public MovementPacketsEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRotation(final float yaw, final float pitch) {
        this.setYaw(yaw);
        this.setPitch(pitch);
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            MovementPacketsListener movementPacketsListener = (MovementPacketsListener) listener;
            movementPacketsListener.onUpdate(this);
        }
    }

    @Override
    public Class<MovementPacketsListener> GetListenerClassType() {
        return MovementPacketsListener.class;
    }
}
