

package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.FoodLevelListener;

import java.util.ArrayList;
import java.util.List;

public class FoodLevelEvent extends AbstractEvent {
    private float foodLevel;

    public FoodLevelEvent(float foodLevel) {
        this.foodLevel = foodLevel;
    }

    public float getFoodLevel() {
        return foodLevel;
    }


    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            FoodLevelListener foodLevelListener = (FoodLevelListener) listener;
            foodLevelListener.onFoodLevelChanged(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<FoodLevelListener> GetListenerClassType() {
        return FoodLevelListener.class;
    }
}