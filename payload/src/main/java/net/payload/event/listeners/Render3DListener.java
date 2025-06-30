
package net.payload.event.listeners;

import net.payload.event.events.Render3DEvent;

public interface Render3DListener extends AbstractListener {
	public abstract void onRender(Render3DEvent event);
}
