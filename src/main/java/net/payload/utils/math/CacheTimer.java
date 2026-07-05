package net.payload.utils.math;

import net.payload.module.modules.exploit.Timer;

import java.util.concurrent.TimeUnit;

public class CacheTimer extends Timer {
    private long time = System.nanoTime();


    public boolean passedMs(long ms) {
        return passedNS(convertToNS(ms));
    }

    public boolean passedNS(long ns) {
        return System.nanoTime() - time >= ns;
    }

    public long convertToNS(long time) {
        return time * 1000000L;
    }

    public boolean passed(Number time) {
        if (time.longValue() <= 0L)
            return true;
        return (getElapsedTime() > time.longValue());
    }

    public boolean passed(Number time, TimeUnit unit) {
        return passed(Long.valueOf(unit.toMillis(time.longValue())));
    }

    public long getElapsedTime() {
        return toMillis(System.nanoTime() - this.time);
    }

    public void setElapsedTime(Number time) {
        this
                .time = (time.longValue() == -255L) ? 0L : (System.nanoTime() - time.longValue());
    }

    public long getElapsedTime(TimeUnit unit) {
        return unit.convert(getElapsedTime(), TimeUnit.MILLISECONDS);
    }

    public void reset() {
        this.time = System.nanoTime();
    }

    private long toMillis(long nanos) {
        return nanos / 1000000L;
    }
}
