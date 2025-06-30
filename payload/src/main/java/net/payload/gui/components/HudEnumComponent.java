package net.payload.gui.components;



import net.minecraft.client.util.math.MatrixStack;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.events.MouseMoveEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Rectangle;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.settings.types.EnumSetting;
import net.payload.utils.input.CursorStyle;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

import static net.payload.utils.render.TextureBank.leftarrow;
import static net.payload.utils.render.TextureBank.rightarrow;

public class HudEnumComponent<T extends Enum<T>> extends Component {
    private EnumSetting<T> enumSetting;

    private boolean hoveringLeftButton;
    private boolean hoveringRightButton;

    public HudEnumComponent(EnumSetting<T> enumSetting) {
        super();
        this.enumSetting = enumSetting;
        this.header = enumSetting.displayName;
        this.setMargin(new Margin(8f, 2f, 8f, 2f));
    }

    @Override
    public void measure(Size availableSize) {
        preferredSize = new Size(availableSize.getWidth(), 30.0f);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        super.draw(drawContext, partialTicks);
        MatrixStack matrixStack = drawContext.getMatrices();
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        float actualX = actualSize.getX();
        float actualY = actualSize.getY();
        float actualWidth = actualSize.getWidth();
        float actualHeight = actualSize.getHeight();
        String enumValue = this.enumSetting.getValue().toString();
        float stringWidth = Render2D.getStringWidth(enumValue);
        Color baseColor = new Color(22, 22, 22, 100);
        Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6, actualWidth + 8, 31, baseColor, baseColor);
        Render2D.drawString(drawContext, enumSetting.displayName, actualX + 1, Math.round(actualY + 13), 0xFFFFFF);
        Render2D.drawString(drawContext, enumValue, actualX + (actualWidth / 2.0f) - stringWidth + 61, Math.round(actualY + 13), 0x9B9B9B);
    }
    
    @Override
    public void onMouseClick(MouseClickEvent event) {
        super.onMouseClick(event);

        if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
            if (hovered) {
                T currentValue = enumSetting.getValue();
                T[] enumConstants = currentValue.getDeclaringClass().getEnumConstants();
                int currentIndex = java.util.Arrays.asList(enumConstants).indexOf(currentValue);
                int enumCount = enumConstants.length;

                float actualX = actualSize.getX();
                float actualY = actualSize.getY();
                float actualWidth = actualSize.getWidth();
                float actualHeight = actualSize.getHeight();


                Rectangle leftArrowHitbox = new Rectangle(actualX + actualWidth - 240.0f, actualY+2, 240.0f, actualHeight+7);


                if (leftArrowHitbox.intersects((float) event.mouseX, (float) event.mouseY))
                    currentIndex = (currentIndex - 1 + enumCount) % enumCount;

                enumSetting.setValue(enumConstants[currentIndex]);
                event.cancel();
            }
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        super.onMouseMove(event);

        float actualX = actualSize.getX();
        float actualY = actualSize.getY();
        float actualWidth = actualSize.getWidth();
        float actualHeight = actualSize.getHeight();

        Rectangle leftArrowHitbox = new Rectangle(actualX + actualWidth - 240.0f, actualY+2, 240.0f, actualHeight+7);

        boolean wasHoveringLeftButton = hoveringLeftButton;
        boolean wasHoveringRightButton = hoveringRightButton;
        hoveringLeftButton = leftArrowHitbox.intersects((float) event.getX(), (float) event.getY());


        if (hoveringLeftButton || hoveringRightButton)
            GuiManager.setCursor(CursorStyle.Click);
    }
}

