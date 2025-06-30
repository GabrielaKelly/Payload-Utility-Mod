package net.payload.gui.navigation.windows;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.DrawContext;
import net.payload.Payload;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.components.ColorPickerComponent;
import net.payload.gui.components.StackPanelComponent;
import net.payload.gui.components.StringComponent;
import net.payload.gui.navigation.Window;

import java.util.Arrays;
import java.util.List;
/*
public class HudColorsWindow extends Window {
	public HudColorsWindow() {
		super("HUD Colors", 500, 250);

		this.minWidth = 250.0f;

		StackPanelComponent stackPanel = new StackPanelComponent();
		stackPanel.setMargin(new Margin(null, 30f, null, null));

		List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
		LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));
		stackPanel.addChild(new StringComponent("HUD Colors", GuiManager.foregroundColor.getValue(), false));
		//	stackPanel.addChild(new StringComponent("HUD Font", GuiManager.foregroundColor.getValue(), false));

	//	ListComponent listComponent = new ListComponent(fontNames, Payload.getInstance().fontManager.fontSetting);
	//	stackPanel.addChild(listComponent);

	// 	stackPanel.addChild(new StringComponent("HUD Colors", GuiManager.foregroundColor.getValue(), false));
		this.addChild(stackPanel);
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		super.draw(drawContext, partialTicks);
	}
}
*/