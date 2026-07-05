

package net.payload.event.listeners;

import net.payload.event.events.SendPacketEvent;

public interface SendPacketListener extends AbstractListener {
    public abstract void onSendPacket(SendPacketEvent event);
}
