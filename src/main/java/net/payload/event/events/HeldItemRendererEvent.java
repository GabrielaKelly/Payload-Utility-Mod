package net.payload.event.events;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.AttackCooldownListener;
import net.payload.event.listeners.HeldItemRendererListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class HeldItemRendererEvent extends AbstractEvent {

    private final Hand hand;
    private final ItemStack item;
    private final float ep;
    private final MatrixStack stack;

    public HeldItemRendererEvent(Hand hand, ItemStack item, float equipProgress, MatrixStack stack) {
        this.hand = hand;
        this.item = item;
        this.ep = equipProgress;
        this.stack = stack;
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getItem() {
        return item;
    }

    public float getEp() {
        return ep;
    }

    public MatrixStack getStack() {
        return stack;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            HeldItemRendererListener heldItemRendererListener = (HeldItemRendererListener) listener;
            heldItemRendererListener.onRenderHeld(this);
        }
    }

    @Override
    public Class<HeldItemRendererListener> GetListenerClassType() {
        return HeldItemRendererListener.class;
    }
}
