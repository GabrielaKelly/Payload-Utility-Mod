package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.PlaySoundListener;
import net.minecraft.client.sound.SoundInstance;

import java.util.ArrayList;
import java.util.List;

public class PlaySoundEvent extends AbstractEvent {
    private final SoundInstance soundInstance;

    public PlaySoundEvent(SoundInstance soundInstance) {
        this.soundInstance = soundInstance;
    }

    public SoundInstance getSoundInstance() {
        return soundInstance;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            PlaySoundListener playSoundListener = (PlaySoundListener) listener;
            playSoundListener.onPlaySound(this);
        }
    }

    @Override
    public Class<PlaySoundListener> GetListenerClassType() {
        return PlaySoundListener.class;
    }
}
