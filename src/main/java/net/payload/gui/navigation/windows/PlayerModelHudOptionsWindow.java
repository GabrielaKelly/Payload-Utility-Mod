package net.payload.gui.navigation.windows;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.DrawContext;
import net.payload.Payload;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.components.*;
import net.payload.gui.navigation.Window;

import java.util.Arrays;
import java.util.List;

import static net.payload.gui.GuiManager.*;

public class PlayerModelHudOptionsWindow extends Window {

    public PlayerModelHudOptionsWindow() {
        super("PlayerModelHudOptions", 500, 250);
        this.minHeight = 125.0f;
        this.minWidth = 250.0f;
        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));
        List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
        LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));
        stackPanel.addChild(new StringComponent("PlayerModelHud", GuiManager.foregroundColor.getValue(), false));
        HudEnumComponent size = new HudEnumComponent(playerModelSizeMode);
        stackPanel.addChild(size);
        HudEnumComponent centerStuff = new HudEnumComponent(centerOrientation);
        stackPanel.addChild(centerStuff);
        HudCheckboxComponent copyyaw = new HudCheckboxComponent(copyYaw);
        stackPanel.addChild(copyyaw);
        HudSliderComponent customyaw = new HudSliderComponent(customYaw);
        stackPanel.addChild(customyaw);
        HudCheckboxComponent copypitch = new HudCheckboxComponent(copyPitch);
        stackPanel.addChild(copypitch);
        HudSliderComponent custompitch = new HudSliderComponent(customPitch);
        stackPanel.addChild(custompitch);
        this.addChild(stackPanel);
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
    }
}
