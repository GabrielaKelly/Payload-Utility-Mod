package net.payload.event.listeners;

import net.payload.event.events.SendMovementPacketEvent;

public interface SendMovementPacketListener extends AbstractListener {
    public abstract void onSendMovementPacket(SendMovementPacketEvent.Pre event);
    public abstract void onSendMovementPacket(SendMovementPacketEvent.Post event);
}
