

package net.payload.event.listeners;

import net.payload.event.events.KeyDownEvent;

public interface KeyDownListener extends AbstractListener {
    public abstract void onKeyDown(KeyDownEvent event);
}
