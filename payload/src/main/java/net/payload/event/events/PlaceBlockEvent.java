package net.payload.event.events;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.BreakBlockListener;
import net.payload.event.listeners.PlaceBlockListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class PlaceBlockEvent extends AbstractEvent {

    public BlockPos blockPos;
    public Block block;

    public PlaceBlockEvent(BlockPos blockPos, Block block) {
        isCancelled = false;
        this.blockPos = blockPos;
        this.block = block;
    }


    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            PlaceBlockListener placeBlockListener = (PlaceBlockListener) listener;
            placeBlockListener.onPlace(this);
        }
    }

    @Override
    public Class<PlaceBlockEvent> GetListenerClassType() {
        return PlaceBlockEvent.class;
    }
}
