package net.payload.gui.navigation.huds;

import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.SpeedUnit;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.HudWindow;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class SpeedHUD extends HudWindow {
    private static final float BASE_WIDTH = 100f;
    private static final float BASE_HEIGHT = 50f;
    private static final int GRAPH_POINTS = 100;
    private static final double SMOOTHING_FACTOR = 0.5;

    private float currentScale = 1.0f;

    private record SpeedDataPoint(long timestamp, double speed) {}
    private final List<SpeedDataPoint> speedHistory;

    private String speedText = null;
    private double currentSpeed = 0.0;
    private int tickCounter = 0;
    private long lastUpdateTime;

    public SpeedHUD(int x, int y) {
        super("SpeedHUD", x, y, BASE_WIDTH, BASE_HEIGHT);
        resizeMode = ResizeMode.None;

        speedHistory = new ArrayList<>();
        lastUpdateTime = System.currentTimeMillis();

        SettingManager.registerSetting(GuiManager.speedSizeMode);
        SettingManager.registerSetting(GuiManager.SpeedGraphUpdateDelay);
        SettingManager.registerSetting(GuiManager.speedGraphShowText);
        SettingManager.registerSetting(GuiManager.speedGraphBackgroundColor);
        SettingManager.registerSetting(GuiManager.speedGraphColor);

        updateSize();
    }

    private void updateSize() {
        switch (GuiManager.speedSizeMode.getValue()) {
            case SMALL -> currentScale = 1.0f;
            case LARGE -> currentScale = 3.0f;
            case MASSIVE -> currentScale = 4.0f;
            case HUGE -> currentScale = 5.0f;
            case NORMAL -> currentScale = 2.0f;
        }

        float scaledWidth = BASE_WIDTH * currentScale;
        float scaledHeight = BASE_HEIGHT * currentScale;

        position.setValue(new Rectangle(
                position.getValue().getX(),
                position.getValue().getY(),
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

    private void updateSpeed() {
        PlayerEntity player = MC.player;
        if (player != null) {
            long currentTime = System.currentTimeMillis();
            double dx = player.getX() - player.prevX;
            double dz = player.getZ() - player.prevZ;
            double dy = player.getY() - player.prevY;

            double speed = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20;

            if (GuiManager.speedUnit.getValue() == SpeedUnit.KMPH) {
                speed *= 3.6;
            }

            currentSpeed = (speed * (1.0 - SMOOTHING_FACTOR)) +
                    (currentSpeed * SMOOTHING_FACTOR);

            speedHistory.add(new SpeedDataPoint(currentTime, currentSpeed));

            while (speedHistory.size() > GRAPH_POINTS) {
                speedHistory.remove(0);
            }

            lastUpdateTime = currentTime;

            String unit = GuiManager.speedUnit.getValue().getDisplay();
            speedText = String.format("Speed: %.1f %s", currentSpeed, unit);
        } else {
            speedText = null;
            currentSpeed = 0.0;
        }
    }

    private void drawSpeedGraph(DrawContext drawContext, Rectangle pos) {
        if (speedHistory.isEmpty()) return;

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        try {
            float graphX = pos.getX();
            float graphY = pos.getY();
            float scaledWidth = BASE_WIDTH * currentScale;
            float scaledHeight = BASE_HEIGHT * currentScale;

            Render2D.drawBox(
                    matrixStack.peek().getPositionMatrix(),
                    graphX,
                    graphY,
                    scaledWidth,
                    scaledHeight,
                    GuiManager.speedGraphBackgroundColor.getValue()
            );

            double maxSpeed = speedHistory.stream()
                    .mapToDouble(SpeedDataPoint::speed)
                    .max()
                    .orElse(1.0);

            int points = Math.min(GRAPH_POINTS, speedHistory.size());
            float pointWidth = scaledWidth / (points - 1);

            for (int i = 0; i < points - 1; i++) {
                SpeedDataPoint current = speedHistory.get(speedHistory.size() - points + i);
                SpeedDataPoint next = speedHistory.get(speedHistory.size() - points + i + 1);

                float x1 = graphX + (i * pointWidth);
                float x2 = graphX + ((i + 1) * pointWidth);

                float y1 = graphY + scaledHeight - (float)((current.speed() / maxSpeed) * scaledHeight);
                float y2 = graphY + scaledHeight - (float)((next.speed() / maxSpeed) * scaledHeight);

                Render2D.drawLine(
                        matrixStack.peek().getPositionMatrix(),
                        x1,
                        y1,
                        x2,
                        y2,
                        GuiManager.speedGraphColor.getValue()
                );
            }
        } finally {
            matrixStack.pop();
        }
    }

    @Override
    public void update() {
        super.update();
        updateSize();

        float updateInterval = Math.max(1, (int)(GuiManager.SpeedGraphUpdateDelay.getValue() * 20));
        if (++tickCounter >= updateInterval) {
            tickCounter = 0;
            updateSpeed();
        }
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible()) {
            super.draw(drawContext, partialTicks);
            return;
        }

        Rectangle pos = position.getValue();
        if (pos.isDrawable()) {
            drawSpeedGraph(drawContext, pos);

            if (GuiManager.speedGraphShowText.getValue() && speedText != null) {
                MatrixStack matrixStack = drawContext.getMatrices();
                matrixStack.push();
                try {
                    matrixStack.scale(currentScale * 0.30f, currentScale * 0.30f, currentScale);

                    float textY = pos.getY() + (BASE_HEIGHT * currentScale) - (29 * currentScale);

                    Render2D.drawString(
                            drawContext,
                            speedText,
                            pos.getX() / (currentScale * 0.30f) + 6,
                            textY / (currentScale * 0.30f) - 65,
                            GuiManager.speedGraphColor.getValue().getColorAsInt()
                    );
                } finally {
                    matrixStack.pop();
                }
            }
        }

        super.draw(drawContext, partialTicks);
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }
}