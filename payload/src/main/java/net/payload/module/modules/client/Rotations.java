
package net.payload.module.modules.client;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;

public class Rotations extends Module {

    public BooleanSetting serverApply = BooleanSetting.builder()
            .id("rotations_serverapply")
            .displayName("Server Apply")
            .defaultValue(false)
            .build();

    public BooleanSetting serverYaw = BooleanSetting.builder()
            .id("rotations_serveryaw")
            .displayName("Server Yaw")
            .defaultValue(false)
            .build();

    public BooleanSetting serverPitch = BooleanSetting.builder()
            .id("rotations_serverpitch")
            .displayName("Server Pitch")
            .defaultValue(false)
            .build();

    public BooleanSetting renderYaw = BooleanSetting.builder()
            .id("rotations_renderyaw")
            .displayName("Render Yaw")
            .defaultValue(false)
            .build();

    public BooleanSetting renderPitch = BooleanSetting.builder()
            .id("rotations_renderpitch")
            .displayName("Render Pitch")
            .defaultValue(false)
            .build();

    public FloatSetting steps = FloatSetting.builder().id("rotations_steps").displayName("Steps")
            .id("antiaim_steps")
            .defaultValue(0.6f)
            .minValue(0.01f).maxValue(1f).step(0.01f).build();

    public Rotations() {
        super("Rotations");
        this.setCategory(Category.of("Client"));
        this.setDescription("Payload rotation settings");

        this.addSetting(steps);
        this.addSetting(renderYaw);
        this.addSetting(renderPitch);
        this.addSetting(serverApply);
        this.addSetting(serverYaw);
        this.addSetting(serverPitch);
    }

    @Override
    public void onDisable() {
        keepEnabled();
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onToggle() {

    }

}