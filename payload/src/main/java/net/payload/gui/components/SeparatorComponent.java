package net.payload.gui.components;

import net.payload.gui.GuiManager;
import net.payload.gui.Size;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;

public class SeparatorComponent extends Component {

	public SeparatorComponent() {
		super();
	}

	@Override
	public void measure(Size availableSize) {
		preferredSize = new Size(availableSize.getWidth(), 1.0f);
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		float actualHeight = this.getActualSize().getHeight();

		//Render2D.drawLine(drawContext.getMatrices().peek().getPositionMatrix(), actualX, actualY, actualX + actualWidth,
			//	actualY + actualHeight, GuiManager.borderColor.getValue());
	}
}
