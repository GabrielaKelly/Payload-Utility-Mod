package net.payload.event.events;

import net.minecraft.text.Text;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.BossBarListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BossBarEvent extends AbstractEvent {

    public String name;

    public BossBarEvent(Text name) {
        this.name = name.getString();
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            BossBarListener bossBarListener = (BossBarListener) listener;
            bossBarListener.onRender(this);
        }
    }

    @Override
    public Class<BossBarListener> GetListenerClassType() {
        return BossBarListener.class;
    }
}
