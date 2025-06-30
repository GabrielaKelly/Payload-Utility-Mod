package net.payload.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.payload.event.events.MouseClickEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Rectangle;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import org.joml.Matrix4f;

import static net.payload.utils.render.TextureBank.*;

public class CollapsiblePanelComponent extends Component {
    private boolean isExpanded = false;
    private final String headerText;
    private final StackPanelComponent contentPanel;
    private float collapsedHeight = 29f;
    private float expandedHeight;
    private float animationProgress = 0f;
    private final float ANIMATION_SPEED = 0.2f;
    private long lastUpdateTime = System.currentTimeMillis();

    public CollapsiblePanelComponent(String header) {
        this.headerText = header;
        this.contentPanel = new StackPanelComponent();
        this.contentPanel.setVisible(false);
        super.addChild(contentPanel);
    }

    public void addChild(Component child) {
        contentPanel.addChild(child);
    }

    @Override
    public void measure(Size availableSize) {
        this.setMargin(new Margin(0f, 4f, 0f, 4f));
        contentPanel.measure(availableSize);
        expandedHeight = collapsedHeight + contentPanel.getPreferredSize().getHeight();
        preferredSize = new Size(availableSize.getWidth(), isExpanded ? expandedHeight : collapsedHeight
        );
    }

    @Override
    public void arrange(Rectangle finalSize) {
        super.arrange(finalSize);
        if (isExpanded) {
            Rectangle contentBounds = new Rectangle(
                    finalSize.getX(),
                    finalSize.getY() + collapsedHeight,
                    finalSize.getWidth(),
                    expandedHeight - collapsedHeight
            );
            contentPanel.arrange(contentBounds);
        }
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        updateAnimation();
        MatrixStack matrixStack = drawContext.getMatrices();

        float x = getActualSize().getX();
        float y = getActualSize().getY();

        float actualWidth = this.getActualSize().getWidth();
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

        Color startColor = new Color(22, 22, 22, 100);
        Color endColor = new Color(22, 22, 22, 100);
        float actualX = this.getActualSize().getX();
        float actualY = this.getActualSize().getY();

        Render2D.drawHorizontalGradient(matrix4f, actualX + 3, actualY + 4, actualWidth - 8, 30, startColor, endColor);
        Render2D.drawString(drawContext, headerText, x + 8, y + 13, 0xFFFFFF);

        matrixStack.push();
        float baseArrowSize = 16;
        float scaleFactor = 3.0f;
        float arrowSize = baseArrowSize * scaleFactor;
        float arrowX = x + actualWidth - (baseArrowSize * scaleFactor) - 8;
        float arrowY = y + (collapsedHeight - (baseArrowSize * scaleFactor)) / 2 + 5;

        float centerX = arrowX + (arrowSize / 2);
        float centerY = arrowY + (arrowSize / 2);

        matrixStack.translate(centerX, centerY, 0);
        matrixStack.multiply(new org.joml.Quaternionf().rotateZ((float) Math.toRadians(animationProgress * 90f)));
        matrixStack.scale(scaleFactor, scaleFactor, 1.0f);
        matrixStack.translate(-centerX/scaleFactor, -centerY/scaleFactor, 0);
        switch (GuiManager.guistyle.getValue()) {
            case Atsu:
                Render2D.drawTexturedQuad(
                        matrixStack.peek().getPositionMatrix(),
                        ARROW_TEXTURE,
                        arrowX / scaleFactor,
                        arrowY / scaleFactor,
                        baseArrowSize,
                        baseArrowSize,
                        new Color(255, 255, 255, 255)
                );
                break;
            case Rias:
                Render2D.drawTexturedQuad(
                        matrixStack.peek().getPositionMatrix(),
                        ARROW_TEXTURE1,
                        arrowX / scaleFactor,
                        arrowY / scaleFactor,
                        baseArrowSize,
                        baseArrowSize,
                        new Color(255, 255, 255, 255)
                );
                break;
            case Captain:
                Render2D.drawTexturedQuad(
                        matrixStack.peek().getPositionMatrix(),
                        ARROW_TEXTURE2,
                        arrowX / scaleFactor,
                        arrowY / scaleFactor,
                        baseArrowSize,
                        baseArrowSize,
                        new Color(255, 255, 255, 255)
                );
                break;
        }

        matrixStack.pop();

        if (isExpanded) {
            contentPanel.setVisible(true);
            contentPanel.draw(drawContext, partialTicks);
        }
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        float targetProgress = isExpanded ? 1f : 0f;

        if (animationProgress != targetProgress) {
            if (isExpanded) {
                animationProgress = Math.min(1f, animationProgress + ANIMATION_SPEED * deltaTime * 60f);
            } else {
                animationProgress = Math.max(0f, animationProgress - ANIMATION_SPEED * deltaTime * 60f);
            }
        }
    }

    @Override
    public void onMouseClick(MouseClickEvent event) {
        float mouseX = (float) event.mouseX;
        float mouseY = (float) event.mouseY;

        if (mouseY >= getActualSize().getY() &&
                mouseY <= getActualSize().getY() + collapsedHeight &&
                mouseX >= getActualSize().getX() &&
                mouseX <= getActualSize().getX() + getActualSize().getWidth()) {

            if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
                isExpanded = !isExpanded;
                contentPanel.setVisible(isExpanded);
                invalidateMeasure();
                event.cancel();
            }
        }

        if (isExpanded) {
            super.onMouseClick(event);
        }
    }
}