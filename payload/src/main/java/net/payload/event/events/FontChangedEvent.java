package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.FontChangedListener;

import java.util.ArrayList;
import java.util.List;

public class FontChangedEvent extends AbstractEvent {
    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            FontChangedListener fontChangeListener = (FontChangedListener) listener;
            fontChangeListener.onFontChanged(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<FontChangedListener> GetListenerClassType() {
        return FontChangedListener.class;
    }
}