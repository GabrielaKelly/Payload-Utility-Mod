package net.payload.gui;

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

public class DubCountOptionsWindow extends Window {

    public DubCountOptionsWindow() {
        super("HUD Options", 500, 250);

        this.minWidth = 250.0f;
        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));
        List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
        LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));


        stackPanel.addChild(new StringComponent("DubCount HUD", GuiManager.foregroundColor.getValue(), false));
        this.addChild(stackPanel);
        HudEnumComponent dubSize = new HudEnumComponent(GuiManager.dubSize);
        stackPanel.addChild(dubSize);
        HudEnumComponent dubtexts = new HudEnumComponent(dubCountTextStyle);
        stackPanel.addChild(dubtexts);
        stackPanel.addChild(new HudColorPickerComponent(dubLabelColor));
        stackPanel.addChild(new HudColorPickerComponent(dubCountColor));
        stackPanel.addChild(new StringComponent("HUD Font", GuiManager.foregroundColor.getValue(), true));
        ListComponent listComponent = new ListComponent(fontNames, Payload.getInstance().fontManager.fontSetting);
        stackPanel.addChild(listComponent);

    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
    }
}
