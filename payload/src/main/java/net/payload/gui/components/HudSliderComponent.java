package net.payload.gui.components;

import net.payload.Payload;
import org.joml.Matrix4f;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.events.MouseMoveEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.gui.colors.Colors;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class HudSliderComponent extends Component {
    private static final int MAX_TEXT_LENGTH = 100;
    private static final float ANIMATION_SPEED = 12f;
    private static final float SCROLL_SPEED = 30.0f;
    private static final float SCROLL_PADDING = 120.0f;
    private float currentSliderPosition = 0.4f;
    private float minValue;
    private float maxValue;
    private float value;
    private boolean isSliding = false;
    private final FloatSetting floatSetting;
    private final String originalText;
    private final String truncatedText;
    private final boolean shouldScroll;
    private float textScrollPosition = 0.0f;
    private long lastUpdateTime;

    public HudSliderComponent(FloatSetting floatSetting) {
        super();
        this.floatSetting = floatSetting;
        this.lastUpdateTime = System.currentTimeMillis();

        minValue = floatSetting.min_value;
        maxValue = floatSetting.max_value;
        this.originalText = floatSetting.displayName;
        this.shouldScroll = originalText.length() > MAX_TEXT_LENGTH;
        this.truncatedText = shouldScroll
                ? originalText.substring(0, MAX_TEXT_LENGTH) + "..."
                : originalText;
        value = floatSetting.getValue();
        currentSliderPosition = (float) ((value - minValue) / (maxValue - minValue));

        floatSetting.addOnUpdate(f -> {
            value = f;
            currentSliderPosition = (float) Math.min(Math.max((value - minValue) / (maxValue - minValue), 0f), 1f);
        });

        this.setMargin(new Margin(8f, 2f, 8f, 2f));
    }

    @Override
    public void measure(Size availableSize) {
        preferredSize = new Size(availableSize.getWidth(), 29.0f);
    }

    @Override
    public void onMouseClick(MouseClickEvent event) {
        super.onMouseClick(event);
        if (event.button == MouseButton.LEFT) {
            if (event.action == MouseAction.DOWN) {
                if (hovered) {
                    isSliding = true;
                    event.cancel();
                }
            } else if (event.action == MouseAction.UP) {
                isSliding = false;
            }
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        super.onMouseMove(event);

        if (Payload.getInstance().guiManager.isClickGuiOpen() && this.isSliding) {
            double mouseX = event.getX();
            float actualX = this.getActualSize().getX();
            float actualWidth = this.getActualSize().getWidth();

            float targetPosition = (float) Math.min(((mouseX - actualX) / actualWidth), 1f);
            targetPosition = Math.max(0f, targetPosition);

            currentSliderPosition = targetPosition;
            value = (currentSliderPosition * (maxValue - minValue)) + minValue;

            if (floatSetting != null)
                floatSetting.setValue(value);
        }
    }

    @Override
    protected void onHoverStateChanged(boolean hovered) {
        if (!hovered) {
            textScrollPosition = 0;
        }
    }

    @Override
    public void update() {
        super.update();

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        if (shouldScroll && isHovered()) {
            float fullTextWidth = Render2D.getStringWidth(originalText);
            float spacing = SCROLL_PADDING;
            float totalWidth = fullTextWidth + spacing;
            textScrollPosition += SCROLL_SPEED * deltaTime;
        } else {
            textScrollPosition = 0;
        }
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (this.floatSetting == null) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        MatrixStack matrixStack = drawContext.getMatrices();
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

        float actualX = this.getActualSize().getX();
        float actualY = this.getActualSize().getY();
        float actualWidth = this.getActualSize().getWidth();
        float filledLength = actualWidth * currentSliderPosition;
        Render2D.drawBox(matrix4f, actualX + filledLength - 1, actualY + 6,
                (actualWidth - filledLength) + 4, 30, new Color(22, 22, 22, 100));

        // Draw slider fill based on style
        switch (GuiManager.guistyle.getValue()) {
            case Atsu:
                Color startColor = new Color(182, 220, 255, 155);
                Color endColor = new Color(185, 182, 229, 155);
                Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6,
                        filledLength + 8, 30, startColor, endColor);
                break;
            case Captain:
                Color leonstart = new Color(147, 90, 160, 155);
                Color leonend = new Color(137, 166, 210, 155);
                Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6,
                        filledLength + 8, 30, leonstart, leonend);
                break;
            case Rias:
                Color riasstart = new Color(210, 41, 44, 155);
                Color riasend = new Color(86, 24, 27, 155);
                Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6,
                        filledLength + 8, 30, riasstart, riasend);
                break;
        }

        // Draw scrolling or static text
        if (shouldScroll && isHovered()) {
            float fullTextWidth = Render2D.getStringWidth(originalText);
            float visibleWidth = actualWidth;
            float spacing = SCROLL_PADDING;
            float totalWidth = fullTextWidth + spacing;
            int copies = (int)Math.ceil(visibleWidth / totalWidth) + 2;

            drawContext.enableScissor(
                    (int)(actualX - 2),
                    (int)(actualY + 6),
                    (int)(actualX + actualWidth - 35),
                    (int)(actualY + 36)
            );

            for (int i = 0; i < copies; i++) {
                float xPos = actualX + i * totalWidth - (textScrollPosition % totalWidth);
                Render2D.drawString(
                        drawContext,
                        originalText,
                        xPos,
                        Math.round(actualY + 13),
                        0xFFFFFF
                );
            }

            drawContext.disableScissor();
        } else {
            Render2D.drawString(
                    drawContext,
                    truncatedText,
                    actualX + 1,
                    Math.round(actualY + 13),
                    0xFFFFFF
            );
        }

        // Draw value text
        String valueText = String.format("%.1f", value);
        int textWidth = mc.textRenderer.getWidth(valueText);
        int textSize = textWidth + 12;

        int integerPart = (int) value;
        int numDigits = String.valueOf(Math.abs(integerPart)).length();
        int xAdjustment = (numDigits >= 4) ? 11 : 0;

        float xPos = actualX + actualWidth - 9 - textSize + xAdjustment;
        Render2D.drawString(drawContext, valueText, xPos, Math.round(actualY + 13), 0xFFFFFF);

        super.draw(drawContext, partialTicks);
    }

    public float getSliderPosition() {
        return this.currentSliderPosition;
    }

    public void setSliderPosition(float pos) {
        this.currentSliderPosition = pos;
    }
}