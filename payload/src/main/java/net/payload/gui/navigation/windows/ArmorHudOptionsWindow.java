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

import static net.payload.gui.GuiManager.ArmorScale;
import static net.payload.gui.GuiManager.armorPosition;

public class ArmorHudOptionsWindow extends Window {

    public ArmorHudOptionsWindow() {
        super("ArmorHUD Options", 500, 250);
        this.minHeight = 125.0f;
        this.minWidth = 250.0f;
        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));
        List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
        LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));


        stackPanel.addChild(new StringComponent("ArmorHUD", GuiManager.foregroundColor.getValue(), false));
        HudEnumComponent armorSizing = new HudEnumComponent(GuiManager.armorSizing);
        stackPanel.addChild(armorSizing);
        HudEnumComponent armorPosition = new HudEnumComponent(GuiManager.armorPosition);
        stackPanel.addChild(armorPosition);
        HudEnumComponent armorEnum = new HudEnumComponent(GuiManager.armorEnum);
        stackPanel.addChild(armorEnum);


        this.addChild(stackPanel);

    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
    }
}
