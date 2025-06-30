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

public class IncomingPacketHudOptionsWindow extends Window {
    public IncomingPacketHudOptionsWindow() {
        super("Packet Graph Settings", 50, 50);
        this.minWidth = 250.0f;
        StackPanelComponent stackPanel = new StackPanelComponent();
        stackPanel.setMargin(new Margin(null, 30f, null, null));
        List<String> fontNames = Payload.getInstance().fontManager.fontRenderers.keySet().stream().toList();
        LogUtils.getLogger().info(Arrays.toString(fontNames.toArray()));
        stackPanel.addChild(new StringComponent("Incoming Packets", GuiManager.foregroundColor.getValue(), false));
        HudEnumComponent sss44ss = new HudEnumComponent(incpacketsizeMode);
        stackPanel.addChild(sss44ss);
        HudSliderComponent erer = new HudSliderComponent(PacketUpdateDelay);
        stackPanel.addChild(erer);
        stackPanel.addChild(new HudCheckboxComponent(packetShowText));
        stackPanel.addChild(new HudColorPickerComponent(packethudcolor));
        stackPanel.addChild(new HudColorPickerComponent(packetBackgroundColor));


        this.addChild(stackPanel);
    }
@Override
public void draw(DrawContext drawContext, float partialTicks) {
    super.draw(drawContext, partialTicks);
}
}
