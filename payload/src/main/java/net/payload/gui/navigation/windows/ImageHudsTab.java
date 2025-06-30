package net.payload.gui.navigation.windows;

import net.payload.gui.Margin;
import net.payload.gui.components.HudComponent;
import net.payload.gui.components.SeparatorComponent;
import net.payload.gui.components.StackPanelComponent;
import net.payload.gui.components.StringComponent;
import net.payload.gui.navigation.HudWindow;
import net.payload.gui.navigation.Window;
import net.payload.gui.navigation.huds.*;


import java.util.ArrayList;

public class ImageHudsTab extends Window {
    static int screenHeight =MC.getWindow().getHeight();
    static int screenWidth = MC.getWindow().getWidth();
    static int ImageHudX = (screenWidth / 2) + 303;
    static int ImageHudY = screenHeight /2;
    public ImageHudsTab(ArrayList<HudWindow> huds) {
        super("Image HUDs", ImageHudX, ImageHudY);

        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));

        stackPanel.addChild(new StringComponent("Graphical HUDs"));
        stackPanel.addChild(new SeparatorComponent());

        for (HudWindow hud : huds) {
            if (isImageHud(hud)) {
                HudComponent hudComponent = new HudComponent(hud.getID(), hud);
                stackPanel.addChild(hudComponent);
            }
        }
        addChild(stackPanel);
        this.setMinHeight(340.0f);
        this.setMinWidth(300.0f);
    }

    private boolean isImageHud(HudWindow hud) {
        return (hud instanceof ArmorHud ||
                hud instanceof Sheep ||
                hud instanceof SentPacketHud ||
                hud instanceof IncomingPacketHud ||
                hud instanceof AccelerationHud ||
                hud instanceof SpeedHUD ||
                hud instanceof DVDLogo ||
                hud instanceof PlayerModelHud ||
                hud instanceof HaloHud);
    }
}