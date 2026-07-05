package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.SendMessageListener;

import java.util.ArrayList;
import java.util.List;

public class SendMessageEvent extends AbstractEvent {
    private String message;

    public SendMessageEvent(String message) {
        this.message = message;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            SendMessageListener sendMessageListener = (SendMessageListener) listener;
            sendMessageListener.onSendMessage(this);
        }
    }

    @Override
    public Class<SendMessageListener> GetListenerClassType() {
        return SendMessageListener.class;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
