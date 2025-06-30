package net.payload.gui.components;

import org.joml.Matrix4f;

import net.payload.gui.GuiManager;
import net.payload.gui.Size;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ImageComponent extends Component {

	public Identifier image;

	public ImageComponent() {
		super();
	}

	public ImageComponent(Identifier image) {
		this();
		this.image = image;
	}

	@Override
	public void measure(Size availableSize) {
		preferredSize = new Size(0f, 0f);
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		if (image != null) {
			MatrixStack matrixStack = drawContext.getMatrices();
			Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

			float actualX = this.getActualSize().getX();
			float actualY = this.getActualSize().getY();
			float actualWidth = this.getActualSize().getWidth();
			float actualHeight = this.getActualSize().getHeight();

			Render2D.drawTexturedQuad(matrix4f, image, actualX, actualY, actualWidth, actualHeight,
					GuiManager.foregroundColor.getValue());
		}
		super.draw(drawContext, partialTicks);
	}
}
