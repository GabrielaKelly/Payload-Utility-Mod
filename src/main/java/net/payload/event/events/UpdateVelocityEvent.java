package net.payload.event.events;

import net.minecraft.util.math.Vec3d;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.UpdateVelocityListener;

import java.util.ArrayList;
import java.util.List;

public class UpdateVelocityEvent extends AbstractEvent {
    Vec3d movementInput;
    float speed;
    float yaw;
    Vec3d velocity;

    public UpdateVelocityEvent(Vec3d movementInput, float speed, float yaw, Vec3d velocity) {
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }

    public Vec3d getMovementInput() {
        return this.movementInput;
    }

    public float getSpeed() {
        return this.speed;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vec3d velocity) {
        this.velocity = velocity;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            UpdateVelocityListener updateVelocityListener = (UpdateVelocityListener) listener;
            updateVelocityListener.onUpdateVel(this);
        }
    }

    @Override
    public Class<UpdateVelocityListener> GetListenerClassType() {
        return UpdateVelocityListener.class;
    }
}