package net.payload.event.listeners;

import net.payload.event.events.JumpEvent;

public interface JumpListener extends AbstractListener {
    public abstract void onJumpPre(JumpEvent.Pre event);
    public abstract void onJumpPost(JumpEvent.Post event);
}
