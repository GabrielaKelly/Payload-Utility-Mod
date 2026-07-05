package net.payload.event.events;

import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.ReceivePacketListener;
import net.minecraft.network.packet.Packet;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class ReceivePacketEvent extends AbstractEvent {

    private Packet<?> packet;

    public Packet<?> GetPacket() {
        return packet;
    }

    public <T extends Packet<?>> T getPacket() {
        return (T) packet;
    }

    public ReceivePacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            ReceivePacketListener readPacketListener = (ReceivePacketListener) listener;
            readPacketListener.onReceivePacket(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<ReceivePacketListener> GetListenerClassType() {
        return ReceivePacketListener.class;
    }
}
