

package net.payload.gui.navigation.windows;

import java.util.ArrayList;

import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Size;
import net.payload.gui.components.HudComponent;
import net.payload.gui.components.SeparatorComponent;
import net.payload.gui.components.StackPanelComponent;
import net.payload.gui.components.StringComponent;
import net.payload.gui.navigation.HudWindow;
import net.payload.gui.navigation.Window;
import net.minecraft.client.gui.DrawContext;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;

public class ToggleHudsTab extends Window {
	public ToggleHudsTab(ArrayList<HudWindow> huds) {
		super("Toggle HUDs", 0, 0);

		StackPanelComponent stackPanel = new StackPanelComponent();
		stackPanel.setMargin(new Margin(null, 30f, null, null));

		stackPanel.addChild(new StringComponent("Toggle HUDs"));
		stackPanel.addChild(new SeparatorComponent());

		for (HudWindow hud : huds) {
			HudComponent hudComponent = new HudComponent(hud.getID(), hud);
			stackPanel.addChild(hudComponent);

		}
		addChild(stackPanel);
		this.setMinHeight(340.0f);
		this.setMinWidth(300.0f);
	}


	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		super.draw(drawContext, partialTicks);
	}


}
