package net.payload.api.event;

import org.apache.http.concurrent.Cancellable;

public class Event {
    private final boolean cancelable = getClass().isAnnotationPresent((Class) Cancellable.class);

    private boolean canceled;

    public boolean isCancelable() {
        return this.cancelable;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean cancel) {
        if (isCancelable())
            this.canceled = cancel;
    }

    public void cancel() {
        setCanceled(true);
    }
}
