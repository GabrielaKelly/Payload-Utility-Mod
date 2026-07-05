

package net.payload.event.listeners;

import net.payload.event.events.FoodLevelEvent;

public interface FoodLevelListener extends AbstractListener {
    public abstract void onFoodLevelChanged(FoodLevelEvent readPacketEvent);
}
