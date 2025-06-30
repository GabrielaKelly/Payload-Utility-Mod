

package net.payload.event.listeners;

import net.payload.event.events.PlayerHealthEvent;

public interface PlayerHealthListener extends AbstractListener {
    public abstract void onHealthChanged(PlayerHealthEvent readPacketEvent);
}
