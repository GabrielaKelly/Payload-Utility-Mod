package net.payload.module.modules.render;

import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.payload.Payload;
import net.payload.event.events.HeldItemRendererEvent;
import net.payload.event.listeners.HeldItemRendererListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.PageGroup;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;

public class ItemViewModel extends Module implements HeldItemRendererListener {

    private final FloatSetting scaleMainX = FloatSetting.builder()
            .id("ivm_scale_main_x")
            .displayName("X-Scale")
            .description("Adjusts the X-axis scale of mainhand items")
            .defaultValue(1.0f)
            .minValue(0.1f)
            .maxValue(5.0f)
            .step(0.01f)
            .build();

    private final FloatSetting scaleMainY = FloatSetting.builder()
            .id("ivm_scale_main_y")
            .displayName("Y-Scale")
            .description("Adjusts the Y-axis scale of mainhand items")
            .defaultValue(1.0f)
            .minValue(0.1f)
            .maxValue(5.0f)
            .step(0.01f)
            .build();

    private final FloatSetting scaleMainZ = FloatSetting.builder()
            .id("ivm_scale_main_z")
            .displayName("Z-Scale")
            .description("Adjusts the Z-axis scale of mainhand items")
            .defaultValue(1.0f)
            .minValue(0.1f)
            .maxValue(5.0f)
            .step(0.01f)
            .build();

    private final FloatSetting positionMainX = FloatSetting.builder()
            .id("ivm_position_main_x")
            .displayName("X-Pos")
            .description("Adjusts the X-axis position of mainhand items")
            .defaultValue(0.0f)
            .minValue(-3.0f)
            .maxValue(3.0f)
            .step(0.01f)
            .build();

    private final FloatSetting positionMainY = FloatSetting.builder()
            .id("ivm_position_main_y")
            .displayName("Y-Pos")
            .description("Adjusts the Y-axis position of mainhand items")
            .defaultValue(0.0f)
            .minValue(-3.0f)
            .maxValue(3.0f)
            .step(0.01f)
            .build();

    private final FloatSetting positionMainZ = FloatSetting.builder()
            .id("ivm_position_main_z")
            .displayName("Z-Pos")
            .description("Adjusts the Z-axis position of mainhand items")
            .defaultValue(0.0f)
            .minValue(-3.0f)
            .maxValue(3.0f)
            .step(0.01f)
            .build();

    private final FloatSetting rotationMainX = FloatSetting.builder()
            .id("ivm_rotation_main_x")
            .displayName("X-Rotation")
            .description("Adjusts the X-axis rotation of mainhand items")
            .defaultValue(0.0f)
            .minValue(-180.0f)
            .maxValue(180.0f)
            .step(0.01f)
            .build();

    private final FloatSetting rotationMainY = FloatSetting.builder()
            .id("ivm_rotation_main_y")
            .displayName("Y-Rotation")
            .description("Adjusts the Y-axis rotation of mainhand items")
            .defaultValue(0.0f)
            .minValue(-180.0f)
            .maxValue(180.0f)
            .step(0.01f)
            .build();

    private final FloatSetting rotationMainZ = FloatSetting.builder()
            .id("ivm_rotation_main_z")
            .displayName("Z-Rotation")
            .description("Adjusts the Z-axis rotation of mainhand items")
            .defaultValue(0.0f)
            .minValue(-180.0f)
            .maxValue(180.0f)
            .step(0.01f)
            .build();

    private final FloatSetting scaleOffX = FloatSetting.builder()
            .id("ivm_scale_off_x")
            .displayName("X-Scale")
            .description("Adjusts the X-axis scale of offhand items")
            .defaultValue(1.0f)
            .minValue(0.1f)
            .maxValue(5.0f)
            .step(0.01f)
            .build();

    private final FloatSetting scaleOffY = FloatSetting.builder()
            .id("ivm_scale_off_y")
            .displayName("Y-Scale")
            .description("Adjusts the Y-axis scale of offhand items")
            .defaultValue(1.0f)
            .minValue(0.1f)
            .maxValue(5.0f)
            .step(0.01f)
            .build();

    private final FloatSetting scaleOffZ = FloatSetting.builder()
            .id("ivm_scale_off_z")
            .displayName("Z-Scale")
            .description("Adjusts the Z-axis scale of offhand items")
            .defaultValue(1.0f)
            .minValue(0.1f)
            .maxValue(5.0f)
            .step(0.01f)
            .build();

    private final FloatSetting positionOffX = FloatSetting.builder()
            .id("ivm_position_off_x")
            .displayName("X-Pos")
            .description("Adjusts the X-axis position of offhand items")
            .defaultValue(0.0f)
            .minValue(-3.0f)
            .maxValue(3.0f)
            .step(0.01f)
            .build();

    private final FloatSetting positionOffY = FloatSetting.builder()
            .id("ivm_position_off_y")
            .displayName("Y-Pos")
            .description("Adjusts the Y-axis position of offhand items")
            .defaultValue(0.0f)
            .minValue(-3.0f)
            .maxValue(3.0f)
            .step(0.01f)
            .build();

    private final FloatSetting positionOffZ = FloatSetting.builder()
            .id("ivm_position_off_z")
            .displayName("Z-Pos")
            .description("Adjusts the Z-axis position of offhand items")
            .defaultValue(0.0f)
            .minValue(-3.0f)
            .maxValue(3.0f)
            .step(0.01f)
            .build();

    private final FloatSetting rotationOffX = FloatSetting.builder()
            .id("ivm_rotation_off_x")
            .displayName("X-Rotation")
            .description("Adjusts the X-axis rotation of offhand items")
            .defaultValue(0.0f)
            .minValue(-180.0f)
            .maxValue(180.0f)
            .step(0.01f)
            .build();

    private final FloatSetting rotationOffY = FloatSetting.builder()
            .id("ivm_rotation_off_y")
            .displayName("Y-Rotation")
            .description("Adjusts the Y-axis rotation of offhand items")
            .defaultValue(0.0f)
            .minValue(-180.0f)
            .maxValue(180.0f)
            .step(0.01f)
            .build();

    private final FloatSetting rotationOffZ = FloatSetting.builder()
            .id("ivm_rotation_off_z")
            .displayName("Z-Rotation")
            .description("Adjusts the Z-axis rotation of offhand items")
            .defaultValue(0.0f)
            .minValue(-180.0f)
            .maxValue(180.0f)
            .step(0.01f)
            .build();

    private final BooleanSetting resetSettings = BooleanSetting.builder()
            .id("ivm_reset_settings")
            .displayName("Reset Settings")
            .description("Resets all settings to their default values")
            .defaultValue(false)
            .build();

    public ItemViewModel() {
        super("ItemViewModel");
        this.setCategory(Category.of("Render"));
        this.setDescription("Allows you to modify how items are rendered in your hands");

        PageGroup settingsPages = PageGroup.Builder.builder()
                .id("itemview_pages")
                .description("ItemViewModel settings pages")
                .build();

        PageGroup.Page mainhandPage = new PageGroup.Page("Mainhand");

        SettingGroup mainhandScale = SettingGroup.Builder.builder()
                .displayName("Scale")
                .id("scale_")
                .description("Scale Settings")
                .build();
        mainhandScale.addSetting(scaleMainX);
        mainhandScale.addSetting(scaleMainY);
        mainhandScale.addSetting(scaleMainZ);

        SettingGroup mainhandPosition = SettingGroup.Builder.builder()
                .displayName("Position")
                .id("position_")
                .description("Position Settings")
                .build();
        mainhandPosition.addSetting(positionMainX);
        mainhandPosition.addSetting(positionMainY);
        mainhandPosition.addSetting(positionMainZ);

        SettingGroup mainhandRotation = SettingGroup.Builder.builder()
                .displayName("Rotation")
                .id("rotation_")
                .description("Rotation Settings")
                .build();
        mainhandRotation.addSetting(rotationMainX);
        mainhandRotation.addSetting(rotationMainY);
        mainhandRotation.addSetting(rotationMainZ);

        mainhandPage.addSetting(mainhandScale);
        mainhandPage.addSetting(mainhandPosition);
        mainhandPage.addSetting(mainhandRotation);

        PageGroup.Page offhandPage = new PageGroup.Page("Offhand");

        SettingGroup offhandScale = SettingGroup.Builder.builder()
                .displayName("Scale")
                .id("scale")
                .description("Scale Settings")
                .build();
        offhandScale.addSetting(scaleOffX);
        offhandScale.addSetting(scaleOffY);
        offhandScale.addSetting(scaleOffZ);

        SettingGroup offhandPosition = SettingGroup.Builder.builder()
                .displayName("Position")
                .id("position")
                .description("Position Settings")
                .build();
        offhandPosition.addSetting(positionOffX);
        offhandPosition.addSetting(positionOffY);
        offhandPosition.addSetting(positionOffZ);

        SettingGroup offhandRotation = SettingGroup.Builder.builder()
                .displayName("Rotation")
                .id("rotation")
                .description("Rotation Settings")
                .build();
        offhandRotation.addSetting(rotationOffX);
        offhandRotation.addSetting(rotationOffY);
        offhandRotation.addSetting(rotationOffZ);

        offhandPage.addSetting(offhandScale);
        offhandPage.addSetting(offhandPosition);
        offhandPage.addSetting(offhandRotation);

        settingsPages.addPage(mainhandPage);
        settingsPages.addPage(offhandPage);
        offhandPage.addSetting(resetSettings);
        mainhandPage.addSetting(resetSettings);

        this.addSetting(settingsPages);

        registerSettings();
    }

    private void registerSettings() {
        SettingManager.registerSetting(scaleMainX);
        SettingManager.registerSetting(scaleMainY);
        SettingManager.registerSetting(scaleMainZ);
        SettingManager.registerSetting(positionMainX);
        SettingManager.registerSetting(positionMainY);
        SettingManager.registerSetting(positionMainZ);
        SettingManager.registerSetting(rotationMainX);
        SettingManager.registerSetting(rotationMainY);
        SettingManager.registerSetting(rotationMainZ);
        SettingManager.registerSetting(scaleOffX);
        SettingManager.registerSetting(scaleOffY);
        SettingManager.registerSetting(scaleOffZ);
        SettingManager.registerSetting(positionOffX);
        SettingManager.registerSetting(positionOffY);
        SettingManager.registerSetting(positionOffZ);
        SettingManager.registerSetting(rotationOffX);
        SettingManager.registerSetting(rotationOffY);
        SettingManager.registerSetting(rotationOffZ);
        SettingManager.registerSetting(resetSettings);
    }


    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(HeldItemRendererListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(HeldItemRendererListener.class, this);
    }

    @Override
    public void onToggle() {
    }
    private void resetAllSettings() {

        scaleMainX.setValue(scaleMainX.getDefaultValue());
        scaleMainY.setValue(scaleMainY.getDefaultValue());
        scaleMainZ.setValue(scaleMainZ.getDefaultValue());

        positionMainX.setValue(positionMainX.getDefaultValue());
        positionMainY.setValue(positionMainY.getDefaultValue());
        positionMainZ.setValue(positionMainZ.getDefaultValue());

        rotationMainX.setValue(rotationMainX.getDefaultValue());
        rotationMainY.setValue(rotationMainY.getDefaultValue());
        rotationMainZ.setValue(rotationMainZ.getDefaultValue());

        scaleOffX.setValue(scaleOffX.getDefaultValue());
        scaleOffY.setValue(scaleOffY.getDefaultValue());
        scaleOffZ.setValue(scaleOffZ.getDefaultValue());

        positionOffX.setValue(positionOffX.getDefaultValue());
        positionOffY.setValue(positionOffY.getDefaultValue());
        positionOffZ.setValue(positionOffZ.getDefaultValue());

        rotationOffX.setValue(rotationOffX.getDefaultValue());
        rotationOffY.setValue(rotationOffY.getDefaultValue());
        rotationOffZ.setValue(rotationOffZ.getDefaultValue());

    }

    @Override
    public void onRenderHeld(HeldItemRendererEvent event) {
        if (event.getHand() == Hand.MAIN_HAND) {
            event.getStack().translate(positionMainX.getValue(), positionMainY.getValue(), positionMainZ.getValue());
            event.getStack().scale(scaleMainX.getValue(), scaleMainY.getValue(), scaleMainZ.getValue());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationMainX.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationMainY.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationMainZ.getValue()));
        } else {
            event.getStack().translate(positionOffX.getValue(), positionOffY.getValue(), positionOffZ.getValue());
            event.getStack().scale(scaleOffX.getValue(), scaleOffY.getValue(), scaleOffZ.getValue());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationOffX.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationOffY.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationOffZ.getValue()));
        }

        if (resetSettings.getValue()) {
            System.out.println("Reset button pressed!");
            resetAllSettings();
            resetSettings.setValue(false);
            System.out.println("Reset button state set to false");
        }
    }
}