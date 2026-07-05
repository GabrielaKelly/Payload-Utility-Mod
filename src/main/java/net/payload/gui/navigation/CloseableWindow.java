package net.payload.gui.navigation;

import net.payload.gui.GuiManager;
import org.joml.Matrix4f;

import net.payload.event.events.MouseClickEvent;
import net.payload.gui.Rectangle;
import net.payload.gui.colors.Color;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static net.payload.utils.render.TextureBank.close;

public class CloseableWindow extends Window {

	private Runnable onClose;

	public CloseableWindow(String ID, float x, float y) {
		super(ID, x, y);
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		// Check to see if the event is cancelled. If not, execute branch.
		if (!event.isCancelled()) {
			if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
				float mouseX = (float) event.mouseX;
				float mouseY = (float) event.mouseY;

				Rectangle pos = getActualSize();

				Rectangle closeHitbox = new Rectangle(pos.getX() + pos.getWidth() - 24, pos.getY() + 4, 0.0f, 0.0f);
				if (closeHitbox.intersects(mouseX, mouseY)) {
					if (onClose != null)
						onClose.run();

					parentPage.removeWindow(this);
					event.cancel();
					return;
				}
			}
		}

		// We want to perform mouse click actions FIRST
		super.onMouseClick(event);
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		super.draw(drawContext, partialTicks);

		MatrixStack matrixStack = drawContext.getMatrices();
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

		Rectangle pos = getActualSize();
		if (pos.isDrawable()) {
			float x = pos.getX().floatValue();
			float y = pos.getY().floatValue();
			float width = pos.getWidth().floatValue();
			//Render2D.drawTexturedQuad(matrixStack.peek().getPositionMatrix(), close, (x + 231), (y+3), 16, 16, GuiManager.gearwrench.getDefaultValue());
		}
	}

	public void setOnClose(Runnable runnable) {
		onClose = runnable;
	}
}
