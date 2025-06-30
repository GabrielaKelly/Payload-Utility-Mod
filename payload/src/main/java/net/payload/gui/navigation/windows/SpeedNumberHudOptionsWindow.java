package net.payload.gui.navigation.windows;

import java.util.Arrays;
import java.util.List;

import com.mojang.logging.LogUtils;

import net.payload.Payload;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.components.*;
import net.payload.gui.navigation.Window;
import net.minecraft.client.gui.DrawContext;
import net.payload.module.modules.movement.Speed;
import net.payload.settings.SettingManager;
import net.payload.settings.types.FloatSetting;

import static net.payload.gui.GuiManager.*;

public class SpeedNumberHudOptionsWindow extends Window {

    public SpeedNumberHudOptionsWindow() {
        super("HUD Options", 500, 250);

        this.minWidth = 250.0f;
        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));
        List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
        LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));


        stackPanel.addChild(new StringComponent("SpeedHUD", GuiManager.foregroundColor.getValue(), false));
        this.addChild(stackPanel);
        HudEnumComponent speedSize = new HudEnumComponent(GuiManager.speedSize);
        stackPanel.addChild(speedSize);
        HudEnumComponent speedUnit = new HudEnumComponent(GuiManager.speedUnit);
        stackPanel.addChild(speedUnit);
        stackPanel.addChild(new HudColorPickerComponent(GuiManager.speedhudcolor));
        HudSliderComponent updateslider = new HudSliderComponent(SpeedUpdateDelay);
        stackPanel.addChild(updateslider);
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
    }
}
