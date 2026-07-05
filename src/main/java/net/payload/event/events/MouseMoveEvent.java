

package net.payload.event.events;

import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.MouseMoveListener;

import java.util.ArrayList;
import java.util.List;

public class MouseMoveEvent extends AbstractEvent {
    private double x;
    private double y;
    private double deltaX;
    private double deltaY;
    
    public MouseMoveEvent(double x, double y, double deltaX, double deltaY) {
        super();
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public double getDeltaX() {
    	return this.deltaX;
    }
    
    public double getDeltaY() {
    	return this.deltaY;
    }
    
    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            MouseMoveListener mouseMoveListener = (MouseMoveListener) listener;
            mouseMoveListener.onMouseMove(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<MouseMoveListener> GetListenerClassType() {
        return MouseMoveListener.class;
    }
}