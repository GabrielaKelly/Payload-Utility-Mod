

package net.payload.event.listeners;

import net.payload.event.events.ReceivePacketEvent;

public interface ReceivePacketListener extends AbstractListener {
    public abstract void onReceivePacket(ReceivePacketEvent readPacketEvent);
}
