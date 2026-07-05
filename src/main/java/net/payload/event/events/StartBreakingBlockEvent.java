package net.payload.event.events;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.StartBreakingBlockListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class StartBreakingBlockEvent extends AbstractEvent {


    public BlockPos blockPos;
    public Direction direction;
    public BlockState state;

    public StartBreakingBlockEvent(BlockPos blockPos, BlockState bstate, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
        this.state = bstate;
    }

    public BlockPos getPos() {
        return this.blockPos;
    }

    public BlockState getState() {
        return this.state;
    }

    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            StartBreakingBlockListener startBreakingBlockListener = (StartBreakingBlockListener) listener;
            startBreakingBlockListener.onBreak(this);
        }
    }

    @Override
    public Class<StartBreakingBlockListener> GetListenerClassType() {
        return StartBreakingBlockListener.class;
    }
}
