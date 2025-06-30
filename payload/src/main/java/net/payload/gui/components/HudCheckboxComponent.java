package net.payload.gui.components;

import org.joml.Matrix4f;
import net.payload.event.events.MouseClickEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.settings.types.BooleanSetting;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static net.payload.utils.render.TextureBank.*;

public class HudCheckboxComponent extends Component {
    private String text;
    private BooleanSetting checkbox;
    private Runnable onClick;
    private static final float ANIMATION_SPEED = 12f; // Higher = faster animation
    private static final float SNAP_THRESHOLD = 0.001f;
    private long lastUpdateTime;
    private float fillCenter = 0.5f;
    // fillSpread is the normalized half-width of the fill effect.
    private float fillSpread = 0.0f;
    // Interpolation factor for smoothing (lower value = more frames, smoother animation)
    private float lerpFactor = 0.008f;

    public HudCheckboxComponent(BooleanSetting checkbox) {
        super();
        this.lastUpdateTime = System.currentTimeMillis();

        this.text = checkbox.displayName;
        this.checkbox = checkbox;
        this.setMargin(new Margin(8f, 2f, 8f, 2f));
        if (checkbox.getValue()) {
            fillCenter = 0.5f;
            fillSpread = 0.5f; // covers full width
        } else {
            fillSpread = 0.0f;
        }
    }

    @Override
    public void measure(Size availableSize) {
        preferredSize = new Size(availableSize.getWidth(), 29.0f);
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
        MatrixStack matrixStack = drawContext.getMatrices();
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

        float actualX = this.getActualSize().getX();
        float actualY = this.getActualSize().getY();
        float actualWidth = this.getActualSize().getWidth();

        Color baseColor = new Color(22, 22, 22, 100);
        Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6, actualWidth + 8, 30, baseColor, baseColor);

        if (fillSpread > 0) {
            float leftRel = Math.max(0f, fillCenter - fillSpread);
            float rightRel = Math.min(1f, fillCenter + fillSpread);
            float fillX = actualX + leftRel * actualWidth;
            float fillWidth = (rightRel - leftRel) * actualWidth;

            switch (GuiManager.guistyle.getValue()) {
                case Atsu:
                    Color startColor = new Color(182, 220, 255, 155);
                    Color endColor = new Color(185, 182, 229, 155);
                    Render2D.drawHorizontalGradient(matrix4f, fillX - 5, actualY + 6, fillWidth + 8, 30, startColor, endColor);
                    break;
                case Rias:
                    Color riasstart = new Color(210, 41, 44, 155);
                    Color riasend = new Color(86, 24, 27, 155);
                    Render2D.drawHorizontalGradient(matrix4f, fillX - 5, actualY + 6, fillWidth + 8, 30, riasstart, riasend);
                    break;
                case Captain:
                    Color leonstart = new Color(147, 90, 160, 155);
                    Color leonend = new Color(137, 166, 210, 155);
                    Render2D.drawHorizontalGradient(matrix4f, fillX - 5, actualY + 6, fillWidth + 8, 30, leonstart, leonend);
                    break;
            }
        }

        Render2D.drawString(drawContext, this.text, actualX + 1, Math.round(actualY + 13), 0xFFFFFF);
    }

    @Override
    public void update() {
        super.update();

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        float targetSpread = checkbox.getValue()
                ? Math.max(fillCenter, 1.0f - fillCenter)
                : 0.0f;

        float factor = 1 - (float) Math.exp(-ANIMATION_SPEED * deltaTime);
        fillSpread = lerp(fillSpread, targetSpread, factor);

        if (Math.abs(fillSpread - targetSpread) < SNAP_THRESHOLD) {
            fillSpread = targetSpread;
        }
    }
    /**
     * Linear interpolation helper method.
     * @param start The starting value.
     * @param end The target value.
     * @param factor The interpolation factor (0.0 to 1.0).
     * @return The interpolated value.
     */
    private float lerp(float start, float end, float factor) {
        return start + factor * (end - start);
    }

    @Override
    public void onMouseClick(MouseClickEvent event) {
        super.onMouseClick(event);
        if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
            if (hovered) {
                float clickX = (float) event.mouseX;
                float actualX = this.getActualSize().getX();
                float actualWidth = this.getActualSize().getWidth();
                // Calculate the relative click position (0 to 1)
                float relativeClick = (clickX - actualX) / actualWidth;
                relativeClick = Math.max(0f, Math.min(1f, relativeClick));

                // Set the fill starting point at the click position and reset the spread.
                fillCenter = relativeClick;
                fillSpread = 0.0f;

                // Toggle the checkbox state.
                checkbox.toggle();
                if (onClick != null)
                    onClick.run();
                event.cancel();
            }
        }
    }

    public void setChecked(boolean checked) {
        checkbox.setValue(checked);
        if (checked) {
            fillCenter = 0.5f;
            fillSpread = 0.0f;
        } else {
            fillSpread = 0.5f;
        }
    }

    public boolean isChecked() {
        return checkbox.getValue();
    }
}