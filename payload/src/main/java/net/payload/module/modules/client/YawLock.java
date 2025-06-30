
package net.payload.module.modules.client;

import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.math.CacheTimer;

public class YawLock extends Module implements TickListener {

    public enum Mode {
        Simple, Smart, Alternate
    }

    private final CacheTimer alternateTimer = new CacheTimer();

    private final EnumSetting<net.payload.module.modules.client.YawLock.Mode> mode = EnumSetting.<net.payload.module.modules.client.YawLock.Mode>builder()
            .id("Mode Choice")
            .displayName("Mode")
            .description("Lock mode")
            .defaultValue(Mode.Simple)
            .build();

    private final FloatSetting yawAngle = FloatSetting.builder()
            .id("yawlock_yawangle")
            .displayName("Yaw angle")
            .description("Yaw angle in degrees.")
            .defaultValue(0f)
            .minValue(0f)
            .maxValue(360f)
            .step(5f)
            .build();

    private final FloatSetting pitchAngle = FloatSetting.builder()
            .id("yawlock_pitchangle")
            .displayName("Pitch angle")
            .description("Pitch angle in degrees.")
            .defaultValue(0f)
            .minValue(-90f)
            .maxValue(90f)
            .step(1f)
            .build();

    private final FloatSetting altYawAngle = FloatSetting.builder()
            .id("yawlock_altyawangle")
            .displayName("Alt Yaw angle")
            .description("Yaw angle in degrees.")
            .defaultValue(0f)
            .minValue(0f)
            .maxValue(360f)
            .step(5f)
            .build();

    private final FloatSetting altPitchAngle = FloatSetting.builder()
            .id("yawlock_altpitchangle")
            .displayName("Alt Pitch angle")
            .description("Pitch angle in degrees.")
            .defaultValue(0f)
            .minValue(-90f)
            .maxValue(90f)
            .step(1f)
            .build();

    private final FloatSetting alternateTime = FloatSetting.builder()
            .id("yawlock_alttime")
            .displayName("Alternation Time")
            .defaultValue(2f)
            .minValue(0.1f)
            .maxValue(10f)
            .step(0.1f)
            .build();

    private BooleanSetting autowalk = BooleanSetting.builder()
            .id("yawlock_autowalk")
            .displayName("Walk on Alternate")
            .defaultValue(false)
            .build();

    private BooleanSetting useAltYaw = BooleanSetting.builder()
            .id("yawlock_usealtyaw")
            .displayName("Use AltYaw")
            .defaultValue(true)
            .build();

    private boolean alternate = false;

    public YawLock() {
        super("YawLock");

        this.setCategory(Category.of("Client"));
        this.setDescription("Locks your Yaw and Pitch");

        this.addSetting(mode);
        this.addSetting(yawAngle);
        this.addSetting(pitchAngle);
        this.addSetting(alternateTime);
        this.addSetting(altYawAngle);
        this.addSetting(useAltYaw);
        this.addSetting(altPitchAngle);
        this.addSetting(autowalk);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        if (autowalk.get()) {
            MC.options.forwardKey.setPressed(false);
        }
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (!nullCheck()) {
                    switch (mode.getValue()) {
                        case Simple -> {
                            MC.player.setPitch(pitchAngle.getValue());
                            setYawAngle(yawAngle.getValue());
                        }
                        case Smart -> {
                            MC.player.setPitch(getSmartPitchDirection());
                            setYawAngle(getSmartYawDirection());
                        }
                        case Alternate -> {

                            if (alternateTimer.passed(alternateTime.getValue() * 1000)) {
                                alternate = !alternate; // Toggle between normal and alt angles
                                alternateTimer.reset(); // Reset the timer only when switching
                            }

                            if (alternate) {
                                if (useAltYaw.getValue()) {
                                    setYawAngle(altYawAngle.getValue());
                                }
                                else {
                                    setYawAngle(yawAngle.getValue());
                                }

                                MC.player.setPitch(altPitchAngle.getValue());
                            } else {
                                setYawAngle(yawAngle.getValue());
                                MC.player.setPitch(pitchAngle.getValue());
                            }

                            if (autowalk.getValue()) {
                                if (alternate) {
                                    MC.options.forwardKey.setPressed(true);
                                }
                                else {
                                    MC.options.forwardKey.setPressed(false);
                                }
                            }
                        }
                    }
        }
    }


    @Override
    public void onTick(TickEvent.Post event) {

    }

    private float getSmartYawDirection() {
        return Math.round((MC.player.getYaw() + 1f) / 45f) * 45f;
    }

    private float getSmartPitchDirection() {
        return Math.round((MC.player.getPitch() + 1f) / 30f) * 30f;
    }

    private void setYawAngle(float yawAngle) {
        MC.player.setYaw(yawAngle);
        MC.player.headYaw = yawAngle;
        MC.player.bodyYaw = yawAngle;
    }


}
