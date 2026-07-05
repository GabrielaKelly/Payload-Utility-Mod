package net.payload.gui.navigation.huds;

import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.payload.settings.SettingManager;

import static net.payload.gui.DirectionStyles.FUTURE;
import static net.payload.gui.GuiManager.*;

public class DirectionHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final int BASE_WIDTH = 204;
    private static final int BASE_HEIGHT = 23;
    private static final int BASE_FUTURESTYLE_WIDTH = 233;
    private static final int BASE_FUTURESTYLE_HEIGHT = 21;

    private float currentScale = 1.0f;

    public DirectionHud(int x, int y) {
        super("DirectionHud", x, y, BASE_WIDTH, BASE_HEIGHT);
        resizeMode = ResizeMode.None;

        SettingManager.registerSetting(directionStyles);
        SettingManager.registerSetting(directioncolor);
        SettingManager.registerSetting(directionSize);

        updateSize();
    }

    private void updateSize() {
        float baseWidth = directionStyles.getValue() == FUTURE ?
                BASE_FUTURESTYLE_WIDTH : BASE_WIDTH;
        float baseHeight = directionStyles.getValue() == FUTURE ?
                BASE_FUTURESTYLE_HEIGHT : BASE_HEIGHT;

        switch (directionSize.getValue()) {
            case SMALL -> currentScale = 0.5f;
            case LARGE -> currentScale = 2.0f;
            case MASSIVE -> currentScale = 3.0f;
            default -> currentScale = 1.0f;
        }

        float scaledWidth = baseWidth * currentScale;
        float scaledHeight = baseHeight * currentScale;

        Rectangle currentPos = position.getValue();
        position.setValue(new Rectangle(
                currentPos.getX(),
                currentPos.getY(),
                scaledWidth,
                scaledHeight
        ));

        this.setWidth(scaledWidth);
        this.setHeight(scaledHeight);
        this.minWidth = scaledWidth;
        this.maxWidth = scaledWidth;
        this.minHeight = scaledHeight;
        this.maxHeight = scaledHeight;
    }

    private String getFacingUpperCase() {
        float yaw = MC.player.getYaw();
        while(yaw < 0) yaw += 360;
        while(yaw > 360) yaw -= 360;

        if (yaw > 337.5 || yaw <= 22.5) return "SOUTH";
        if (yaw > 22.5 && yaw <= 67.5) return "SOUTH WEST";
        if (yaw > 67.5 && yaw <= 112.5) return "WEST";
        if (yaw > 112.5 && yaw <= 157.5) return "NORTH WEST";
        if (yaw > 157.5 && yaw <= 202.5) return "NORTH";
        if (yaw > 202.5 && yaw <= 247.5) return "NORTH EAST";
        if (yaw > 247.5 && yaw <= 292.5) return "EAST";
        if (yaw > 292.5 && yaw <= 337.5) return "SOUTH EAST";
        return "INVALID";
    }

    private String getAxisIndicatorUpperCase(String direction) {
        return switch (direction) {
            case "NORTH" -> "-Z";
            case "SOUTH" -> "+Z";
            case "EAST" -> "+X";
            case "WEST" -> "-X";
            case "NORTH EAST" -> "+X -Z";
            case "NORTH WEST" -> "-X -Z";
            case "SOUTH EAST" -> "+X +Z";
            case "SOUTH WEST" -> "-X +Z";
            default -> "?";
        };
    }

    @Override
    public void update() {
        super.update();
        updateSize();
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible()) return;

        Rectangle pos = position.getValue();
        if (!pos.isDrawable()) return;

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        try {
            matrixStack.scale(currentScale, currentScale, currentScale);

            float scaledX = pos.getX() / currentScale;
            float scaledY = pos.getY() / currentScale;

            switch (directionStyles.getValue()) {
                case FUTURE:
                    String mainDirection = getFacingUpperCase();
                    String axisIndicator = getAxisIndicatorUpperCase(mainDirection);
                    String directionText = String.format("\u00a77\u00a7l%s \u00a77\u00a7l[\u00a7f\u00a7l%s\u00a77\u00a7l]",
                            mainDirection, axisIndicator);
                    Render2D.drawString(drawContext, directionText,
                            Math.round(scaledX + 2), Math.round(scaledY + 6),
                            GuiManager.directioncolor.getValue().getColorAsInt());
                    break;

                case PAYLOAD:
                    String properDirection = getFacingUpperCase();
                    directionText = String.format("(%s) [%s]",
                            properDirection,
                            getAxisIndicatorUpperCase(properDirection));
                    Render2D.drawString(drawContext, directionText,
                            Math.round(scaledX + 2), Math.round(scaledY + 3),
                            GuiManager.directioncolor.getValue().getColorAsInt());
                    break;
            }
        } finally {
            matrixStack.pop();
        }

        super.draw(drawContext, partialTicks);
    }
}