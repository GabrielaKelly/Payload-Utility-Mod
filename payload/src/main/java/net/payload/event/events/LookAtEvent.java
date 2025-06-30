package net.payload.event.events;


import net.minecraft.util.math.Vec3d;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.LookAtListener;


import java.util.ArrayList;
import java.util.List;

public class LookAtEvent extends AbstractEvent {
    private Vec3d target;
    private float yaw;
    private float pitch;
    private boolean rotation;
    private float speed;
    public float priority = 0;
    public LookAtEvent() {

    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            LookAtListener lookAtListener = (LookAtListener) listener;
            lookAtListener.onLook(this);
        }
    }

    @Override
    public Class<LookAtListener> GetListenerClassType() {
        return LookAtListener.class;
    }

    public Vec3d getTarget() {
        return target;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean getRotation() {
        return rotation;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setTarget(Vec3d target, float speed, float priority) {
        if (priority >= this.priority) {
            this.rotation = false;
            this.priority = priority;
            this.target = target;
            this.speed = speed;
        }
    }

    public void setRotation(float yaw, float pitch, float speed, float priority) {
        if (priority >= this.priority) {
            this.rotation = true;
            this.priority = priority;
            this.yaw = yaw;
            this.pitch = pitch;
            this.speed = speed;
        }
    }
}
