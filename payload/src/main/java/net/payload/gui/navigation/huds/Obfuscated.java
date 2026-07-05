package net.payload.gui.navigation.huds;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import net.payload.event.events.MouseClickEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.TextAlign;
import net.payload.gui.navigation.HudWindow;
import net.payload.module.Module;
import net.payload.settings.SettingManager;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.payload.utils.Formatting;

import static net.payload.gui.GuiManager.FakeArrayScale;

public class Obfuscated extends HudWindow {
    private static final int MODULE_SPACING = 20;
    private static final int VERTICAL_OFFSET = 10;

    private final EnumSetting<TextAlign> textAlign;
    private float cachedWidth;
    private long lastWidthUpdate;
    private static final long WIDTH_UPDATE_INTERVAL = 1000;

    public Obfuscated(int x, int y, FloatSetting scale) {
        super("Obfuscated", x, y);
        resizeMode = ResizeMode.None;

        this.textAlign = EnumSetting.<TextAlign>builder()
                .id("ModuleArrayListHudText_TextAlign")
                .displayName("Text Align")
                .description("Text Alignment")
                .defaultValue(TextAlign.Left)
                .build();

        SettingManager.registerSetting(textAlign);

        updateMaxWidth();
    }

    private void updateMaxWidth() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWidthUpdate > WIDTH_UPDATE_INTERVAL) {
            cachedWidth = PAYLOAD.moduleManager.modules.stream()
                    .map(mod -> Render2D.getStringWidth(mod.getName()))
                    .max(Float::compare)
                    .orElse(0);

            this.setWidth(cachedWidth);
            lastWidthUpdate = currentTime;
        }
    }

    @Override
    public void onMouseClick(MouseClickEvent event) {
        super.onMouseClick(event);

        if (this.hovered && event.button == MouseButton.RIGHT && event.action == MouseAction.DOWN) {
            cycleTextAlignment();
        }
    }

    private void cycleTextAlignment() {
        TextAlign[] alignments = TextAlign.values();
        int currentIndex = java.util.Arrays.asList(alignments).indexOf(textAlign.getValue());
        textAlign.setValue(alignments[(currentIndex + 1) % alignments.length]);
    }

    @Override
    public void update() {
        super.update();
        updateMaxWidth();

        int totalHeight = (int) PAYLOAD.moduleManager.modules.stream()
                .filter(mod -> mod.state.getValue())
                .count() * MODULE_SPACING;
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

        float scaleValue = FakeArrayScale.getValue();

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        try {
            matrixStack.scale(scaleValue, scaleValue, scaleValue);

            float scaledX = pos.getX() / scaleValue;
            float scaledY = pos.getY() / scaleValue;

            drawModuleList(drawContext, scaledX, scaledY);
        } finally {
            matrixStack.pop();
        }

        super.draw(drawContext, partialTicks);
    }

    private void drawModuleList(DrawContext drawContext, float x, float y) {
        AtomicInteger iteration = new AtomicInteger(0);
        Stream<Module> moduleStream = PAYLOAD.moduleManager.modules.stream()
                .filter(s -> s.state.getValue())
                .sorted(Comparator.comparing(Module::getName));

        if (textAlign.getValue() == TextAlign.Left) {
            moduleStream.forEachOrdered(mod -> {
                String text = Formatting.OBFUSCATED.toString() + mod.getName();
                float yPosition = y + VERTICAL_OFFSET + (iteration.get() * MODULE_SPACING);

                Render2D.drawString(
                        drawContext,
                        text,
                        x,
                        yPosition,
                        GuiManager.obfarraylistcolor.getValue().getColorAsInt()
                );

                iteration.incrementAndGet();
            });
        }
    }
}