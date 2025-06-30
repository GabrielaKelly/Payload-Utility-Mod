

package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.PlayerHealthListener;
import net.minecraft.entity.damage.DamageSource;

import java.util.ArrayList;
import java.util.List;

public class PlayerHealthEvent extends AbstractEvent {
    private float health;
    private DamageSource source;

    public PlayerHealthEvent(DamageSource source, float health) {
        this.source = source;
        this.health = health;
    }

    public float getHealth() {
        return health;
    }

    public DamageSource getDamageSource() {
        return source;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            PlayerHealthListener playerHealthListener = (PlayerHealthListener) listener;
            playerHealthListener.onHealthChanged(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<PlayerHealthListener> GetListenerClassType() {
        return PlayerHealthListener.class;
    }
}