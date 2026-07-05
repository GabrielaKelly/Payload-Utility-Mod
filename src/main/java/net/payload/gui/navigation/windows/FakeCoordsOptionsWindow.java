package net.payload.gui.navigation.windows;

import java.awt.*;
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

public class FakeCoordsOptionsWindow extends Window {

    public FakeCoordsOptionsWindow() {
        super("FakeCoordsOptions", 500, 250);

        this.minWidth = 250.0f;
        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));
        List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
        LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));


        stackPanel.addChild(new StringComponent("FakeCoords", GuiManager.foregroundColor.getValue(), false));
        this.addChild(stackPanel);
        HudEnumComponent fakecoords = new HudEnumComponent(fakecoordsizeMode);
        stackPanel.addChild(fakecoords);
        HudEnumComponent fakecoordssty = new HudEnumComponent(fakecoordStyles);
        stackPanel.addChild(fakecoordssty);
        stackPanel.addChild(new HudColorPickerComponent(GuiManager.fakeposcolor));
        stackPanel.addChild(new HudCheckboxComponent(GuiManager.obfuscatedpos));
        stackPanel.addChild(new HudCheckboxComponent(GuiManager.randomposition));
        HudSliderComponent slider = new HudSliderComponent(fakexvalue);
        stackPanel.addChild(slider);
        slider = new HudSliderComponent(fakezvalue);
        stackPanel.addChild(slider);
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
    }
}
