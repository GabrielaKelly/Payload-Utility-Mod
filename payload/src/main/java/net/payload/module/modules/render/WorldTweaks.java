
/**
 * Trajectory Module
 */
package net.payload.module.modules.render;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.event.listeners.SendMovementPacketListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;

public class WorldTweaks extends Module implements TickListener, ReceivePacketListener {

    private final BooleanSetting ctime = BooleanSetting.builder()
            .id("worldtweaks_time")
            .displayName("Change Time")
            .defaultValue(true)
            .build();

    private final FloatSetting ctimeval = FloatSetting.builder()
            .id("worldtweaks_time_float")
            .displayName("Time")
            .defaultValue(21f)
            .minValue(-20f)
            .maxValue(20f)
            .step(1f)
            .build();

    public final BooleanSetting cskycolor = BooleanSetting.builder()
            .id("worldtweaks_togglefogcolor")
            .displayName("Change Sky")
            .defaultValue(false)
            .build();

    public final ColorSetting skycolor = ColorSetting.builder()
            .id("worldtweaks_fogcolor")
            .displayName("Sky Color")
            .defaultValue(new Color(255f, 255f, 255f, 255f))
            .build();

    long oldTime;

    public WorldTweaks() {
        super("WorldTweaks");
        this.setCategory(Category.of("Render"));
        this.setDescription("Changes how the world is rendered");

        this.addSetting(ctime);
        this.addSetting(ctimeval);
        this.addSetting(cskycolor);
        this.addSetting(skycolor);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);

        MC.world.getLevelProperties().setTimeOfDay(oldTime);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);

        oldTime = MC.world.getTime();
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        if (readPacketEvent.getPacket() instanceof WorldTimeUpdateS2CPacket && ctime.getValue()) {
            oldTime = ((WorldTimeUpdateS2CPacket) readPacketEvent.getPacket()).timeOfDay();
            readPacketEvent.cancel();
        }
    }

    @Override
    public void onTick(TickEvent.Pre event) {

    }

    @Override
    public void onTick(TickEvent.Post event) {
        if (ctime.getValue()) {
            MC.world.getLevelProperties().setTimeOfDay(ctimeval.get().longValue() * 1000);
        }
    }
}