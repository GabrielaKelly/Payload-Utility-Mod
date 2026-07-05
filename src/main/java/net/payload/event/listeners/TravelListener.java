

package net.payload.event.listeners;

import net.payload.event.events.TravelEvent;

public interface TravelListener extends AbstractListener {
    public abstract void onTravelPre(TravelEvent.Pre event);
    public abstract void onTravelPost(TravelEvent.Post event);
}