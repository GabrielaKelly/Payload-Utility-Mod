package net.payload.event.events;

import net.minecraft.entity.player.PlayerEntity;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.AttackCooldownListener;
import net.payload.event.listeners.DeathListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class DeathEvent extends AbstractEvent {

    private final PlayerEntity player;

    public DeathEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            DeathListener deathListener = (DeathListener) listener;
            deathListener.onDeath(this);
        }
    }

    @Override
    public Class<DeathListener> GetListenerClassType() {
        return DeathListener.class;
    }
}
