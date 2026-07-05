

package net.payload.event.listeners;

import net.payload.event.events.Render2DEvent;

public interface Render2DListener extends AbstractListener {
	public abstract void onRender(Render2DEvent event);
}
