

package net.payload.gui.components;

import java.util.List;

import org.joml.Matrix4f;

import net.payload.event.events.MouseClickEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Size;
import net.payload.gui.UIElement;
import net.payload.gui.colors.Color;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class ButtonComponent extends Component {

	private Runnable onClick;

	public ButtonComponent(Runnable onClick) {
		super();

		this.setMargin(new Margin(8f, 4f, 8f, 4f));

		this.onClick = onClick;
	}

	@Override
	public void measure(Size availableSize) {
		if (!isVisible()) {
			preferredSize = Size.ZERO;
			return;
		}

		if (initialized) {
			float finalWidth = 0;
			float finalHeight = 0;

			List<UIElement> children = getChildren();
			for (UIElement element : children) {
				if (!element.isVisible())
					continue;

				element.measure(availableSize);
				Size resultingSize = element.getPreferredSize();

				if (resultingSize.getWidth() > finalWidth)
					finalWidth = resultingSize.getWidth();

				if (resultingSize.getHeight() > finalHeight)
					finalHeight = resultingSize.getHeight();
			}

			if (margin != null) {

				Float marginLeft = margin.getLeft();
				Float marginTop = margin.getTop();
				Float marginRight = margin.getRight();
				Float marginBottom = margin.getBottom();

				if (marginLeft != null)
					finalWidth += marginLeft;

				if (marginRight != null)
					finalWidth += marginRight;

				if (marginTop != null)
					finalHeight += marginTop;

				if (marginBottom != null)
					finalHeight += marginBottom;
			}

			if (minWidth != null && finalWidth < minWidth) {
				finalWidth = minWidth;
			} else if (maxWidth != null && finalWidth > maxWidth) {
				finalWidth = maxWidth;
			}

			if (minHeight != null && finalHeight < minHeight) {
				finalHeight = minHeight;
			} else if (maxHeight != null && finalHeight > maxHeight) {
				finalHeight = maxHeight;
			}

			preferredSize = new Size(finalWidth, finalHeight);
		}
	}

	/**
	 * Sets the OnClick delegate of the button.
	 *
	 * @param onClick Delegate to set.
	 */
	public void setOnClick(Runnable onClick) {
		this.onClick = onClick;
	}

	/**
	 * Draws the button to the screen.
	 *
	 * @param drawContext  The current draw context of the game.
	 * @param partialTicks The partial ticks used for interpolation.
	 */
	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		MatrixStack matrixStack = drawContext.getMatrices();
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		float actualHeight = this.getActualSize().getHeight();

// Define the gradient colors
		Color startColor = new Color(182, 220, 255, 255); // Start color of gradient
		Color endColor = new Color(185, 182, 229, 255); // End color of gradient


// Draw the button with a gradient background
		Render2D.drawHorizontalGradient(matrix4f, actualX, actualY, actualWidth, actualHeight, startColor, endColor);

// Optionally, draw the outline around the button (with rounded corners)
		Render2D.drawBoxOutline(matrix4f, actualX, actualY, actualWidth, actualHeight,
				GuiManager.borderColor.getValue());


		super.draw(drawContext, partialTicks);
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);
		if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
			if (this.hovered) {
				if (onClick != null)
					onClick.run();
				event.cancel();
			}
		}
	}
}
