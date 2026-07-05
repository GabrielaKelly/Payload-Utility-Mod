

package net.payload.event.listeners;

import net.payload.event.events.MouseScrollEvent;

public interface MouseScrollListener extends AbstractListener {
    public abstract void onMouseScroll(MouseScrollEvent event);
}
