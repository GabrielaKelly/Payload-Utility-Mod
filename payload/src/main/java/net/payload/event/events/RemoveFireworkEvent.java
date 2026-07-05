package net.payload.event.events;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.text.Text;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.AttackCooldownListener;
import net.payload.event.listeners.PlaySoundListener;
import net.payload.event.listeners.RemoveFireworkListener;

import java.util.ArrayList;
import java.util.List;

import static net.payload.PayloadClient.MC;

@Cancelable
public class RemoveFireworkEvent extends AbstractEvent {
    private final FireworkRocketEntity rocketEntity;

    public RemoveFireworkEvent(FireworkRocketEntity rocketEntity) {
        this.rocketEntity = rocketEntity;
    }

    public FireworkRocketEntity getRocketEntity() {
        return this.rocketEntity;
    }


    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            RemoveFireworkListener removeFireworkListener = (RemoveFireworkListener) listener;
            removeFireworkListener.OnRemoveFirework(this);
        }
    }

    @Override
    public Class<RemoveFireworkListener> GetListenerClassType() {
        return RemoveFireworkListener.class;
    }
}

