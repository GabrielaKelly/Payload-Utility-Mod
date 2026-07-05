

package net.payload.event.listeners;

import net.payload.event.events.PlayerDeathEvent;

public interface PlayerDeathListener extends AbstractListener {
    public abstract void onPlayerDeath(PlayerDeathEvent readPacketEvent);
}

