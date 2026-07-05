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
import net.payload.settings.SettingManager;

import net.payload.settings.types.FloatSetting;

import static net.payload.gui.GuiManager.*;

public class WatermarkHudOptionsWindow extends Window {

    public WatermarkHudOptionsWindow() {
        super("HUD Options", 500, 250);

        this.minWidth = 250.0f;
        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));
        List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
        LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));


        stackPanel.addChild(new StringComponent("Watermark", GuiManager.foregroundColor.getValue(), false));
        this.addChild(stackPanel);
        HudEnumComponent aeeelignment = new HudEnumComponent(watermarkStyleEnumSetting);
        stackPanel.addChild(aeeelignment);
        HudEnumComponent sssss = new HudEnumComponent(sizeMode);
        stackPanel.addChild(sssss);
        stackPanel.addChild(new HudColorPickerComponent(watermarkcolors));
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
    }
}
