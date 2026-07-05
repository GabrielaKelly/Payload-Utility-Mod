package net.payload.event.listeners;


import net.payload.event.events.SendMessageEvent;

public interface SendMessageListener extends AbstractListener {
    public abstract void onSendMessage(SendMessageEvent sendMessageEvent);
}
