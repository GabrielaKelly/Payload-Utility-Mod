

/**
 * A class to represent a Color that iterates.
 */
package net.payload.gui.colors;

import net.payload.event.events.TickEvent;

public class RainbowColor extends AnimatedColor  {
    public RainbowColor() {
        super();
    }

    @Override
    public void onTick(TickEvent.Post event) {
    	this.setHue(((this.getHue() + 1f) % 360));
    }
}
