package net.payload.gui.navigation.huds;

import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.HudWindow;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class AccelerationHud extends HudWindow {
    private static final float BASE_WIDTH = 100f;
    private static final float BASE_HEIGHT = 50f;
    private static final int GRAPH_POINTS = 100;
    private static final double SMOOTHING_FACTOR = 0.5;

    private float currentScale = 1.0f;

    private record AccelDataPoint(long timestamp, double acceleration) {}
    private final List<AccelDataPoint> accelHistory;

    private String accelText = null;
    private double lastSpeed = 0.0;
    private double currentAcceleration = 0.0;
    private int tickCounter = 0;
    private long lastUpdateTime;

    public AccelerationHud(int x, int y) {
        super("AccelerationHUD", x, y, BASE_WIDTH, BASE_HEIGHT);
        resizeMode = ResizeMode.None;

        accelHistory = new ArrayList<>();
        lastUpdateTime = System.currentTimeMillis();

        SettingManager.registerSetting(GuiManager.accelSizeMode);
        SettingManager.registerSetting(GuiManager.AccelUpdateDelay);
        SettingManager.registerSetting(GuiManager.accelShowText);
        SettingManager.registerSetting(GuiManager.accelBackgroundColor);
        SettingManager.registerSetting(GuiManager.accelhudcolor);

        updateSize();
    }

    private void updateSize() {
        switch (GuiManager.accelSizeMode.getValue()) {
            case SMALL -> currentScale = 1.0f;
            case LARGE -> currentScale = 3.0f;
            case MASSIVE -> currentScale = 4.0f;
            case HUGE -> currentScale = 5.0f;
            default -> currentScale = 2.0f;
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

    private void updateAcceleration() {
        PlayerEntity player = MC.player;
        if (player != null) {
            long currentTime = System.currentTimeMillis();
            double dx = player.getX() - player.prevX;
            double dz = player.getZ() - player.prevZ;
            double dy = player.getY() - player.prevY;

            double currentSpeed = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20;

            double timeDelta = (currentTime - lastUpdateTime) / 1000.0;
            if (timeDelta > 0) {
                double velocityDelta = currentSpeed - lastSpeed;
                double rawAcceleration = velocityDelta / timeDelta;
                if (Math.abs(rawAcceleration) < 0.01) {
                    rawAcceleration = 0;
                }

                currentAcceleration = (rawAcceleration * (1.0 - SMOOTHING_FACTOR)) +
                        (currentAcceleration * SMOOTHING_FACTOR);

                currentAcceleration = Math.max(-50.0, Math.min(50.0, currentAcceleration));
            }

            accelHistory.add(new AccelDataPoint(currentTime, currentAcceleration));

            while (accelHistory.size() > GRAPH_POINTS) {
                accelHistory.remove(0);
            }

            lastSpeed = currentSpeed;
            lastUpdateTime = currentTime;

            accelText = String.format("Accel: %.2f m/s²", currentAcceleration);
        } else {
            accelText = null;
            lastSpeed = 0.0;
            currentAcceleration = 0.0;
        }
    }

    private void drawAccelerationGraph(DrawContext drawContext, Rectangle pos) {
        if (accelHistory.isEmpty()) return;

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
                    GuiManager.accelBackgroundColor.getValue()
            );

            double maxAccel = accelHistory.stream()
                    .mapToDouble(AccelDataPoint::acceleration)
                    .map(Math::abs)
                    .max()
                    .orElse(1.0);

            float zeroY = graphY + (scaledHeight / 2);
            Render2D.drawLine(
                    matrixStack.peek().getPositionMatrix(),
                    graphX,
                    zeroY,
                    graphX + scaledWidth,
                    zeroY,
                    new Color(128, 128, 128, 128)
            );

            int points = Math.min(GRAPH_POINTS, accelHistory.size());
            float pointWidth = scaledWidth / (points - 1);

            for (int i = 0; i < points - 1; i++) {
                AccelDataPoint current = accelHistory.get(accelHistory.size() - points + i);
                AccelDataPoint next = accelHistory.get(accelHistory.size() - points + i + 1);

                float x1 = graphX + (i * pointWidth);
                float x2 = graphX + ((i + 1) * pointWidth);

                float y1 = zeroY - (float)((current.acceleration() / maxAccel) * (scaledHeight / 2));
                float y2 = zeroY - (float)((next.acceleration() / maxAccel) * (scaledHeight / 2));

                Render2D.drawLine(
                        matrixStack.peek().getPositionMatrix(),
                        x1,
                        y1,
                        x2,
                        y2,
                        GuiManager.accelhudcolor.getValue()
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

        float updateInterval = Math.max(1, (int)(GuiManager.AccelUpdateDelay.getValue() * 20));
        if (++tickCounter >= updateInterval) {
            tickCounter = 0;
            updateAcceleration();
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
            MatrixStack matrixStack = drawContext.getMatrices();

            // Draw the graph
            drawAccelerationGraph(drawContext, pos);

            // Draw the text overlay if enabled
            if (GuiManager.accelShowText.getValue() && accelText != null) {
                matrixStack.push();
                try {
                    matrixStack.scale(currentScale * 0.30f, currentScale * 0.30f, currentScale);

                    float textY = pos.getY() + (BASE_HEIGHT * currentScale) - (29 * currentScale);

                    Render2D.drawString(
                            drawContext,
                            accelText,
                            pos.getX() / (currentScale * 0.30f) + 6,
                            textY / (currentScale * 0.30f) - 65,
                            GuiManager.accelhudcolor.getValue().getColorAsInt()
                    );
                } finally {
                    matrixStack.pop();
                }
            }
        }

        super.draw(drawContext, partialTicks);
    }

    public double getCurrentAcceleration() {
        return currentAcceleration;
    }
}