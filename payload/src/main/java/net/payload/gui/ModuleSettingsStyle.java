package net.payload.gui;

import java.util.ArrayList;
import java.util.List;

public enum ModuleSettingsStyle {
    Popout,
    Collapsed;

    private static final List<StyleChangeListener> listeners = new ArrayList<>();

    public static void addListener(StyleChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(StyleChangeListener listener) {
        listeners.remove(listener);
    }

    public static void notifyStyleChange(ModuleSettingsStyle newStyle) {
        for (StyleChangeListener listener : listeners) {
            listener.onStyleChange(newStyle);
        }
    }

    // Interface for style change callbacks
    public interface StyleChangeListener {
        void onStyleChange(ModuleSettingsStyle newStyle);
    }
}