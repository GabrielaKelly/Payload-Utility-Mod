
package net.payload.module.modules.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.payload.Payload;
import net.payload.event.events.DeathEvent;
import net.payload.event.events.GameLeftEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.DeathListener;
import net.payload.event.listeners.GameLeftListener;
import net.payload.event.listeners.SendPacketListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;

import java.util.Optional;

public class MovieMode extends Module implements TickListener, GameLeftListener {

    public MovieMode() {
        super("MovieMode");
        this.setCategory(Category.of("Client"));
        this.setDescription("Fixes your camera like a movie");
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(GameLeftListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
        toggleThirdPerson();
    }

    @Override
    public void onToggle() {

    }

    public boolean movieModeCheck() {
        if (nullCheck()) return false;

        if (!MC.player.isAlive()) return false;

        return true;
    }

    public static void toggleThirdPerson() {
        if (MC.options.getPerspective() == Perspective.FIRST_PERSON) {
            MC.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        toggleThirdPerson();
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onGameLeft(GameLeftEvent event) {
        if (this.state.get()) {
            this.toggle();
        }
    }
}