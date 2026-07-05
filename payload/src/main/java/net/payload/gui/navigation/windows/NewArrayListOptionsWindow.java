package net.payload.gui.navigation.windows;

import java.util.Arrays;
import java.util.List;

import com.mojang.logging.LogUtils;

import net.payload.Payload;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Size;
import net.payload.gui.components.*;
import net.payload.gui.navigation.Window;
import net.minecraft.client.gui.DrawContext;
import net.payload.settings.SettingManager;

import net.payload.settings.types.FloatSetting;

import static net.payload.gui.GuiManager.*;

public class NewArrayListOptionsWindow extends Window {

    public NewArrayListOptionsWindow() {
        super("NewArrayListOptionsWindow", 500, 250);
        this.minHeight = 205.0f;
        this.minWidth = 250.0f;

        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, 10f));
        List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
        LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));

        stackPanel.addChild(new StringComponent("Arraylist", GuiManager.foregroundColor.getValue(), false));

        // Create the page component
        PageComponent pageComponent = new PageComponent("Options");

        // Create Colors page
        PageComponent.Page colorsPage = new PageComponent.Page("Custom Colors");
        colorsPage.addComponent(new HudColorPickerComponent(GuiManager.newarraylistcolor));
        colorsPage.addComponent(new HudColorPickerComponent(newarraylistSecondColor));

        // Create Settings page
        PageComponent.Page settingsPage = new PageComponent.Page("Settings");
        settingsPage.addComponent(new HudEnumComponent(newmoduleArraySizeMode));
        settingsPage.addComponent(new HudEnumComponent(newsortingMode));
        settingsPage.addComponent(new HudEnumComponent(arraylistgradientmode));

        // Add pages to the page component
        pageComponent.addPage(colorsPage);
        pageComponent.addPage(settingsPage);

        // Add components to the stack panel
        stackPanel.addChild(pageComponent);
        this.addChild(stackPanel);
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
    }
}