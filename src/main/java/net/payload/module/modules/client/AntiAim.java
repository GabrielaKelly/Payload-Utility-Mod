
package net.payload.module.modules.client;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;

public class AntiAim extends Module implements TickListener {

    private FloatSetting spinSpeed = FloatSetting.builder().id("antiaim_spinspeed").displayName("Spin Speed")
            .id("antiaim_spinspeed")
            .defaultValue(1.0f)
            .minValue(0.0f).maxValue(60.0f).step(5.0f).build();

    private FloatSetting headPitch = FloatSetting.builder().id("antiaim_headPitch").displayName("Head Pitch")
            .id("antiaim_headPitch")
            .defaultValue(180.0f)
            .minValue(-90.0f).maxValue(180.0f).step(5.0f).build();

    private FloatSetting ticksReset = FloatSetting.builder().id("antiaim_tickreset").displayName("Ticks reset")
            .id("antiaim_ticksReset")
            .defaultValue(10.0f)
            .minValue(0.0f).maxValue(40.0f).step(5.0f).build();

    private BooleanSetting serverSide = BooleanSetting.builder()
            .id("antiaim_serverSide")
            .displayName("Server Side")
            .defaultValue(false)
            .build();

    float ss = 0;
    int ticks = 0;
    boolean firstEnable = false;

    public AntiAim() {
        super("AntiAim");
        this.setCategory(Category.of("Client"));
        this.setDescription("(ClientSide) Renders a funny Yaw/Pitch");

        this.addSetting(spinSpeed);
        this.addSetting(headPitch);
        this.addSetting(ticksReset);
        this.addSetting(serverSide);
    }

    public float getSpinSpeed() {
        return ss;
    }

    public float getHeadPitch() {
        return headPitch.getValue();
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        if (Payload.getInstance().moduleManager.freelook.state.getValue()) {
            Payload.getInstance().moduleManager.freelook.toggle();
        }
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        if (!firstEnable) {
            sendChatMessage("Warning: AntiAim is a clientside feature");
            firstEnable = true;
        }
        ticks = 0;
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onTick(TickEvent.Pre event) {
        ticks++;
        ss = ss + spinSpeed.getValue();

        if (ticks > ticksReset.getValue()) {
            ticks = 0;
            ss = 0;
        }

        if (serverSide.getValue()) {
            if (!Payload.getInstance().moduleManager.freelook.state.getValue()) {
                Payload.getInstance().moduleManager.freelook.toggle();
            }
            MC.player.setPitch(headPitch.getValue());
            MC.player.setYaw(ss);
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }
}