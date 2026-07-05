package net.payload.module.modules.render;

import net.minecraft.util.math.MathHelper;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;

public class Freelook extends Module implements TickListener {

    public Freelook() {
        super("Freelook");
        this.setCategory(Category.of("Render"));
        this.setDescription("Lets you look around regardless of where the player is looking");
    }

    public float cameraYaw;
    public float cameraPitch;

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        cameraYaw = MC.player.getYaw();
        cameraPitch = MC.player.getPitch();
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onTick(TickEvent.Pre event) {
    }


    @Override
    public void onTick(TickEvent.Post event) {
        MC.player.setPitch(MathHelper.clamp(MC.player.getPitch(), -90, 90));
        cameraPitch = MathHelper.clamp(cameraPitch, -90, 90);
    }
}
