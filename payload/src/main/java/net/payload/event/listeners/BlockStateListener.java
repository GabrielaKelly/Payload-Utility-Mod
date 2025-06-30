package net.payload.event.listeners;

import net.payload.event.events.BlockStateEvent;

public interface BlockStateListener extends AbstractListener {
    public abstract void onBlockStateChanged(BlockStateEvent event);
}
