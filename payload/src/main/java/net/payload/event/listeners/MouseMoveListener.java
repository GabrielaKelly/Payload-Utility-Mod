

package net.payload.event.listeners;

import net.payload.event.events.MouseMoveEvent;

public interface MouseMoveListener extends AbstractListener {
    public abstract void onMouseMove(MouseMoveEvent mouseMoveEvent);
}
