package net.payload.gui.navigation.huds;

import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.utils.Formatting;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static net.payload.gui.GuiManager.*;
import static net.payload.gui.FakeCoordStyles.*;

public class FakeCoords extends HudWindow {
    private static final float BASE_HEIGHT = 24f;
    private static final float BASE_PADDING = 8f;

    private boolean hasRandomized = false;
    private double randomFactorX = 1.0;
    private double randomFactorZ = 1.0;
    private float currentScale = 1.0f;
    private float currentWidth = 50f;
    private float currentHeight = BASE_HEIGHT;

    public FakeCoords(int x, int y) {
        super("FakeCoords", x, y, 50, BASE_HEIGHT);
        resizeMode = ResizeMode.None;
        updateDimensions();
    }

    private String getCoordinatesText() {
        if (MC.player == null) return "";

        float xScale = fakexvalue.getValue();
        float zScale = fakezvalue.getValue();
        boolean randomPosition = randomposition.getValue();
        boolean obfuscatedPos = obfuscatedpos.getValue();

        double oX = MC.player.getX() * xScale;
        double oY = MC.player.getY();
        double oZ = MC.player.getZ() * zScale;
        double nX = (MC.player.getX() / 8.0) * xScale;
        double nZ = (MC.player.getZ() / 8.0) * zScale;

        if (randomPosition) {
            if (!hasRandomized) {
                randomFactorX = Math.random();
                randomFactorZ = Math.random();
                hasRandomized = true;
            }
            oX *= randomFactorX;
            oZ *= randomFactorZ;
            nX *= randomFactorX;
            nZ *= randomFactorZ;
        } else {
            hasRandomized = false;
        }

        if (obfuscatedPos) {
            return String.format(
                    "(%s, %s, %s) [%s, %s]",
                    obfuscate(oX), obfuscate(oY), obfuscate(oZ),
                    obfuscate(nX), obfuscate(nZ)
            );
        } else {
            return String.format(
                    "(%.0f, %.0f, %.0f) [%.0f, %.0f]",
                    oX, oY, oZ, nX, nZ
            );
        }
    }

    private float calculateWidth() {
        if (MC.player == null || MC.textRenderer == null) return 50f;
        String coordsText = getCoordinatesText();
        return MC.textRenderer.getWidth(coordsText) + BASE_PADDING;
    }

    private void updateDimensions() {
        float baseWidth = calculateWidth();

        switch (fakecoordsizeMode.getValue()) {
            case SMALL -> currentScale = 0.5f;
            case LARGE -> currentScale = 2.0f;
            case MASSIVE -> currentScale = 3.0f;
            default -> currentScale = 1.0f;
        }

        float finalWidth = baseWidth * currentScale * 1.92f;
        float finalHeight = BASE_HEIGHT * currentScale;

        Rectangle currentPos = position.getValue();
        position.setValue(new Rectangle(
                currentPos.getX(),
                currentPos.getY(),
                finalWidth,
                finalHeight
        ));

        this.setWidth(finalWidth);
        this.setHeight(finalHeight);
        this.minWidth = this.maxWidth = finalWidth;
        this.minHeight = this.maxHeight = finalHeight;

        this.currentWidth = finalWidth;
        this.currentHeight = finalHeight;
    }

    @Override
    public void update() {
        super.update();
        if (MC.player != null && MC.textRenderer != null) {
            updateDimensions();
        }
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible() || MC.player == null) {
            super.draw(drawContext, partialTicks);
            return;
        }

        Rectangle pos = position.getValue();
        if (!pos.isDrawable()) {
            super.draw(drawContext, partialTicks);
            return;
        }

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        try {
            matrixStack.scale(currentScale, currentScale, currentScale);

            float renderX = pos.getX() / currentScale;
            float renderY = pos.getY() / currentScale;

            String coordsText = getCoordinatesText();
            Render2D.drawString(
                    drawContext,
                    coordsText,
                    Math.round(renderX + (4 / currentScale)),
                    Math.round(renderY + 3),
                    fakeposcolor.getValue().getColorAsInt()
            );
        } finally {
            matrixStack.pop();
        }

        super.draw(drawContext, partialTicks);
    }

    private String obfuscate(double value) {
        return Formatting.OBFUSCATED + String.format("%.0f", value) + Formatting.RESET;
    }
}