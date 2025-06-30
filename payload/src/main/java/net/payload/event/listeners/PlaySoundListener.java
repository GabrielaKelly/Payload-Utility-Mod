package net.payload.event.listeners;


import net.payload.event.events.PlaySoundEvent;

public interface PlaySoundListener extends AbstractListener {
    public abstract void onPlaySound(PlaySoundEvent playSoundEvent);
}
