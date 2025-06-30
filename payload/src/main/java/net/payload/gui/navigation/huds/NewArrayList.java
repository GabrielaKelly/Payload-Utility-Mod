package net.payload.gui.navigation.huds;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import net.payload.gui.ArrayListGradientMode;
import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.HudWindow;
import net.payload.module.Module;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static net.payload.gui.GuiManager.*;

public class NewArrayList extends HudWindow {
    private static final int MODULE_SPACING = 20;
    private static final int VERTICAL_PADDING = 10;
    private static final long DIMENSION_UPDATE_INTERVAL = 5;

    private List<Module> cachedEnabledModules;
    private long lastDimensionUpdate;
    private float cachedWidth;
    private float currentScale = 1.0f;
    private static final Color startColor = new Color(182, 220, 255, 255);
    private static final Color endColor = new Color(185, 182, 229, 255);
    private static final Color riasstart = new Color(210, 41, 44, 255);
    private static final Color riasend = new Color(86, 24, 27, 255);
    private static final Color leonstart = new Color(147, 90, 160, 255);
    private static final Color leonend = new Color(137, 166, 210, 255);

    public NewArrayList(int x, int y) {
        super("NewArrayList", x, y);
        resizeMode = ResizeMode.None;
        SettingManager.registerSetting(newsortingMode);
        SettingManager.registerSetting(GuiManager.newmoduleArraySizeMode);
        SettingManager.registerSetting(GuiManager.newarraylistcolor);
        SettingManager.registerSetting(GuiManager.newarraylistSecondColor);
        SettingManager.registerSetting(arraylistgradientmode);
        SettingManager.registerSetting(GuiManager.guistyle);
        updateSize();
        updateDimensions(true);
    }

    private void updateSize() {
        switch (newmoduleArraySizeMode.getValue()) {
            case SMALL -> currentScale = 0.5f;
            case LARGE -> currentScale = 2.0f;
            case HUGE -> currentScale = 3.0f;
            default -> currentScale = 1.0f;
        }
    }

    private void updateDimensions(boolean force) {
        long currentTime = System.currentTimeMillis();
        if (!force && currentTime - lastDimensionUpdate < DIMENSION_UPDATE_INTERVAL) {
            return;
        }

        updateSize();
        float horizontalPadding = 4.0f;

        cachedEnabledModules = PAYLOAD.moduleManager.modules.stream()
                .filter(mod -> mod.state.getValue())
                .collect(Collectors.toList());

        cachedWidth = cachedEnabledModules.stream()
                .map(mod -> Render2D.getStringWidth(mod.getName()))
                .max(Float::compare)
                .orElse(0);

        float totalWidth = (cachedWidth + (horizontalPadding * 2)) * currentScale * 2;
        float totalHeight = (cachedEnabledModules.size() * MODULE_SPACING + VERTICAL_PADDING) * currentScale + 10;

        position.setValue(new Rectangle(
                position.getValue().getX(),
                position.getValue().getY(),
                totalWidth,
                totalHeight
        ));

        this.setWidth(totalWidth);
        this.setHeight(totalHeight);
        this.minWidth = totalWidth;
        this.maxWidth = totalWidth;
        this.minHeight = totalHeight;
        this.maxHeight = totalHeight;

        lastDimensionUpdate = currentTime;
    }

    @Override
    public void update() {
        super.update();
        updateDimensions(false);
    }

    private List<Module> getSortedModules() {
        if (cachedEnabledModules == null) {
            updateDimensions(true);
        }

        List<Module> sortedModules = cachedEnabledModules;

        switch (newsortingMode.getValue()) {
            case ALPHABET:
                sortedModules.sort(Comparator.comparing(Module::getName));
                break;
            case LENGTH:
                sortedModules.sort((m1, m2) -> Float.compare(
                        Render2D.getStringWidth(m2.getName()),
                        Render2D.getStringWidth(m1.getName())
                ));
                break;
        }

        return sortedModules;
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible()) {
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

            float scaledX = pos.getX() / currentScale;
            float scaledY = pos.getY() / currentScale;

            drawModuleList(drawContext, scaledX, scaledY);
        } finally {
            matrixStack.pop();
        }

        super.draw(drawContext, partialTicks);
    }

    private void drawModuleList(DrawContext drawContext, float x, float y) {
        AtomicInteger index = new AtomicInteger(0);

        getSortedModules().forEach(module -> {
            float yPos = y + VERTICAL_PADDING + (index.getAndIncrement() * MODULE_SPACING);

            int firstColor, secondColor;

            if (arraylistgradientmode.getValue() == ArrayListGradientMode.ON) {
                firstColor = GuiManager.newarraylistcolor.getValue().getColorAsInt();
                secondColor = GuiManager.newarraylistSecondColor.getValue().getColorAsInt();
            } else {
                switch (GuiManager.guistyle.getValue()) {
                    case Atsu:
                        firstColor = startColor.getColorAsInt();
                        secondColor = endColor.getColorAsInt();
                        break;
                    case Rias:
                        firstColor = riasstart.getColorAsInt();
                        secondColor = riasend.getColorAsInt();
                        break;
                    case Captain:
                        firstColor = leonstart.getColorAsInt();
                        secondColor = leonend.getColorAsInt();
                        break;
                    default:
                        firstColor = startColor.getColorAsInt();
                        secondColor = endColor.getColorAsInt();
                        break;
                }
            }

            Render2D.drawGradientString(
                    drawContext,
                    module.getName(),
                    x,
                    yPos,
                    firstColor,
                    secondColor
            );
        });
    }

    private static int interpolateColors(int color1, int color2, float factor) {
        if (factor <= 0) return color1;
        if (factor >= 1) return color2;

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int r = (int) Math.round(r1 + factor * (r2 - r1));
        int g = (int) Math.round(g1 + factor * (g2 - g1));
        int b = (int) Math.round(b1 + factor * (b2 - b1));
        int a = (int) Math.round(a1 + factor * (a2 - a1));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}