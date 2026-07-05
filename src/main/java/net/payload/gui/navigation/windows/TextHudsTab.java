package net.payload.gui.navigation.windows;
import net.payload.gui.navigation.huds.*;
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

import java.util.ArrayList;

// TextHudsTab.java
public class TextHudsTab extends Window {
   static int screenHeight =MC.getWindow().getHeight();
   static int screenWidth = MC.getWindow().getWidth();
   static int TextHudX = screenWidth / 2;
   static int TextHudY = screenHeight /2;
    public TextHudsTab(ArrayList<HudWindow> huds) {
        super("Text HUDs", TextHudX, TextHudY);

        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));

        stackPanel.addChild(new StringComponent("Text HUDs"));
        stackPanel.addChild(new SeparatorComponent());

        for (HudWindow hud : huds) {
            if (isTextHud(hud)) {
                HudComponent hudComponent = new HudComponent(hud.getID(), hud);
                stackPanel.addChild(hudComponent);
            }
        }
        addChild(stackPanel);
        this.setMinHeight(340.0f);
        this.setMinWidth(300.0f);
    }

    private boolean isTextHud(HudWindow hud) {
        return (hud instanceof WatermarkHud ||
                hud instanceof NewArrayList ||
                hud instanceof ModuleArrayListHud ||
                hud instanceof CoordsHud ||
                hud instanceof DirectionHud ||
                hud instanceof SpeedNumberHud ||
                hud instanceof FakeCoords ||
                hud instanceof ChestCountHud ||
                hud instanceof ShulkerCountHud);
    }
}

// ImageHudsTab.java
