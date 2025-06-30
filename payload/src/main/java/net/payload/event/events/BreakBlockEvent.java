package net.payload.event.events;

import net.minecraft.util.math.BlockPos;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.AttackCooldownListener;
import net.payload.event.listeners.BreakBlockListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class BreakBlockEvent extends AbstractEvent {

    public BlockPos blockPos;

    public BreakBlockEvent(BlockPos blockPos) {
        isCancelled = false;
        this.blockPos = blockPos;
    }


    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            BreakBlockListener breakBlockListener = (BreakBlockListener) listener;
            breakBlockListener.onBreak(this);
        }
    }

    @Override
    public Class<BreakBlockListener> GetListenerClassType() {
        return BreakBlockListener.class;
    }
}
