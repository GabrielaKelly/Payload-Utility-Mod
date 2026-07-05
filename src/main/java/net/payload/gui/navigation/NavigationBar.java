package net.payload.gui.navigation;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Identifier;
import net.payload.Payload;
import net.payload.PayloadClient;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.listeners.MouseClickListener;
import net.payload.gui.GuiManager;
import net.payload.gui.colors.Color;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

import static net.payload.utils.render.TextureBank.*;

public class NavigationBar implements MouseClickListener {
    MinecraftClient mc = MinecraftClient.getInstance();

    private List<Page> options;
    private int selectedIndex;
    private float currentSelectionX;
    private float targetSelectionX;
    private final float animationSpeed = 0.1f;

    public NavigationBar() {
        options = new ArrayList<>();
        Payload.getInstance().eventManager.AddListener(MouseClickListener.class, this);
        currentSelectionX = 0;
        targetSelectionX = 0;
    }

    public void addPane(Page pane) {
        options.add(pane);
    }

    public List<Page> getPanes() {
        return this.options;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public Page getSelectedPage() {
        return options.get(selectedIndex);
    }

    public void setSelectedIndex(int index) {
        if (index < this.options.size()) {
            this.options.get(selectedIndex).setVisible(false);
            this.selectedIndex = index;
            this.options.get(selectedIndex).setVisible(true);
            targetSelectionX = index * 100;
        }
    }

    public void update() {
        if (options.size() > 0) {
            options.get(selectedIndex).update();
        }
    }

    public void draw(DrawContext drawContext, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Window window = mc.getWindow();
        int width = 40;
        int height = 40 * options.size();
        int leftX = 5;
        MatrixStack matrixStack = drawContext.getMatrices();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        int centerY = (window.getHeight() / 2) - (height / 2); // Center the navigation bar vertically
        switch (GuiManager.guistyle.getValue()) {
            case Atsu:
                currentSelectionX += (targetSelectionX - currentSelectionX) * animationSpeed * partialTicks * 10;
                for (int i = 0; i < options.size(); i++) {
                    Page pane = options.get(i);
                    Identifier texture = (i % 2 == 0) ? hudicon : modicon;
                    Color startColor;
                    Color endColor;

                    if (i % 2 == 0) {
                        startColor = new Color(50, 50, 255, 255);
                        endColor = new Color(100, 100, 255, 255);
                    } else {
                        startColor = new Color(50, 50, 255, 255);
                        endColor = new Color(100, 100, 255, 255);
                    }
                    Render2D.drawHorizontalGradient(matrix, leftX, centerY + (40 * i), width, 40, startColor, endColor);
                    Render2D.drawRoundedBoxOutline(matrix, leftX, centerY + (40 * i), width, 40, GuiManager.roundingRadius.getValue(), GuiManager.borderColor.getValue());
                    Render2D.drawString(drawContext, pane.getTitle(), leftX + (width / 2) - (Render2D.getStringWidth(pane.getTitle()) / 2) - 8, centerY + (40 * i) + 10, GuiManager.foregroundColor.getValue());
                    Render2D.drawTexturedQuad(matrix, texture, leftX + width - 35, centerY + (40 * i) + 5, 32, 32, GuiManager.foregroundColor.getDefaultValue());
                    if (i == selectedIndex) {
                        Render2D.drawRoundedBox(matrix, leftX, centerY + (40 * i), width, 40, GuiManager.roundingRadius.getValue() - 1, new Color(150, 150, 150, 100));
                    }

                    if (i == selectedIndex) {
                        pane.render(drawContext, partialTicks);
                    }
                }
                break;
            case Captain:
                currentSelectionX += (targetSelectionX - currentSelectionX) * animationSpeed * partialTicks * 10;
                for (int i = 0; i < options.size(); i++) {
                    Page pane = options.get(i);
                    Identifier texture = (i % 2 == 0) ? hudicon : modicon;
                    Color startColor;
                    Color endColor;

                    if (i % 2 == 0) {
                        startColor = new Color(147, 90, 160,  255);
                        endColor = new Color(137, 166, 210,255);
                    } else {
                        startColor = new Color(147, 90, 160, 255);
                        endColor = new Color(137, 166, 210,255);
                    }
                    Render2D.drawHorizontalGradient(matrix, leftX, centerY + (40 * i), width, 40, startColor, endColor);
                    Render2D.drawRoundedBoxOutline(matrix, leftX, centerY + (40 * i), width, 40, GuiManager.roundingRadius.getValue(), GuiManager.borderColor.getValue());
                    Render2D.drawString(drawContext, pane.getTitle(), leftX + (width / 2) - (Render2D.getStringWidth(pane.getTitle()) / 2) - 8, centerY + (40 * i) + 10, GuiManager.foregroundColor.getValue());
                    Render2D.drawTexturedQuad(matrix, texture, leftX + width - 35, centerY + (40 * i) + 5, 32, 32, GuiManager.foregroundColor.getDefaultValue());
                    if (i == selectedIndex) {
                        Render2D.drawRoundedBox(matrix, leftX, centerY + (40 * i), width, 40, GuiManager.roundingRadius.getValue() - 1, new Color(150, 150, 150, 100));
                    }

                    if (i == selectedIndex) {
                        pane.render(drawContext, partialTicks);
                    }
                }
                break;
            case Rias:
                currentSelectionX += (targetSelectionX - currentSelectionX) * animationSpeed * partialTicks * 10;
                for (int i = 0; i < options.size(); i++) {
                    Page pane = options.get(i);
                    Identifier texture = (i % 2 == 0) ? hudicon : modicon;
                    Color startColor;
                    Color endColor;

                    if (i % 2 == 0) {
                        startColor = new Color(210, 41, 44, 255);
                        endColor = new Color(86, 24, 27, 255);
                    } else {
                        startColor = new Color(210, 41, 44, 255);
                        endColor = new Color(86, 24, 27, 255);
                    }
                    Render2D.drawHorizontalGradient(matrix, leftX, centerY + (40 * i), width, 40, startColor, endColor);
                    Render2D.drawRoundedBoxOutline(matrix, leftX, centerY + (40 * i), width, 40, GuiManager.roundingRadius.getValue(), GuiManager.borderColor.getValue());
                    Render2D.drawString(drawContext, pane.getTitle(), leftX + (width / 2) - (Render2D.getStringWidth(pane.getTitle()) / 2) - 8, centerY + (40 * i) + 10, GuiManager.foregroundColor.getValue());
                    Render2D.drawTexturedQuad(matrix, texture, leftX + width - 35, centerY + (40 * i) + 5, 32, 32, GuiManager.foregroundColor.getDefaultValue());
                    if (i == selectedIndex) {
                        Render2D.drawRoundedBox(matrix, leftX, centerY + (40 * i), width, 40, GuiManager.roundingRadius.getValue() - 1, new Color(150, 150, 150, 100));
                    }

                    if (i == selectedIndex) {
                        pane.render(drawContext, partialTicks);
                    }
                }
                break;
        }
        RenderSystem.disableBlend();
    }
    @Override
    public void onMouseClick(MouseClickEvent event) {
        if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
            PayloadClient payload = Payload.getInstance();
            Window window = mc.getWindow();

            double mouseX = event.mouseX;
            double mouseY = event.mouseY;
            int width = 40;
            int leftX = 5;
            int centerY = (window.getHeight() / 2) - ((40 * options.size()) / 2);
            if (payload.guiManager.isClickGuiOpen()) {
                if (mouseX >= leftX && mouseX <= (leftX + width)) {
                    for (int i = 0; i < options.size(); i++) {
                        if (mouseY >= centerY + (40 * i) && mouseY <= centerY + (40 * (i + 1))) {
                            this.setSelectedIndex(i);
                            event.cancel();
                            break;
                        }
                    }
                }
            }
        }
    }
}
