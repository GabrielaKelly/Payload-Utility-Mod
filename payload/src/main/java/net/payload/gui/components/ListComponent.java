

package net.payload.gui.components;

import java.util.List;

import net.minecraft.client.util.math.MatrixStack;
import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.listeners.MouseClickListener;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Rectangle;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.settings.types.StringSetting;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

import static net.payload.utils.render.TextureBank.leftarrow;
import static net.payload.utils.render.TextureBank.rightarrow;

public class ListComponent extends Component implements MouseClickListener {
	private StringSetting listSetting;

	private List<String> itemsSource;
	private int selectedIndex;

	public ListComponent(List<String> itemsSource) {
		super();
		this.setMargin(new Margin(2f, null, 2f, null));
		this.itemsSource = itemsSource;
	}

	public ListComponent(List<String> itemsSource, StringSetting listSetting) {
		super();
		this.listSetting = listSetting;
		this.setMargin(new Margin(2f, null, 2f, null));
		this.itemsSource = itemsSource;
	}

	@Override
	public void measure(Size availableSize) {
		preferredSize = new Size(availableSize.getWidth(), 30.0f);
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public String getSelectedItem() {
		if (itemsSource.size() > selectedIndex)
			return itemsSource.get(selectedIndex);
		else
			return null;
	}

	public List<String> getItemsSource() {
		return itemsSource;
	}

	public void setItemsSource(List<String> itemsSource) {
		this.itemsSource = itemsSource;
		setSelectedIndex(this.selectedIndex);
	}

	@Override
	public void onVisibilityChanged() {
		super.onVisibilityChanged();
		if (this.isVisible())
			Payload.getInstance().eventManager.AddListener(MouseClickListener.class, this);
		else
			Payload.getInstance().eventManager.RemoveListener(MouseClickListener.class, this);
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {

		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();

		if (listSetting != null) {
			float stringWidth = Payload.getInstance().fontManager.GetRenderer().getWidth(listSetting.getValue());
			Render2D.drawString(drawContext, listSetting.getValue(), actualX + (actualWidth / 2.0f) - stringWidth,
					actualY + 8, 0x9B9B9B);
		} else if (itemsSource.size() > 0) {
			float stringWidth = Payload.getInstance().fontManager.GetRenderer().getWidth(itemsSource.get(selectedIndex));
			Render2D.drawString(drawContext, itemsSource.get(selectedIndex),
					actualX + (actualWidth / 2.0f) - stringWidth, actualY + 8, 0x9B9B9B);
		}
		MatrixStack matrixStack = drawContext.getMatrices();
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
		Render2D.drawTexturedQuad(matrixStack.peek().getPositionMatrix(), rightarrow, (actualX + actualWidth - 20), (actualY+7), 16, 16, new Color(255, 255, 255, 255));
		Render2D.drawTexturedQuad(matrixStack.peek().getPositionMatrix(), leftarrow, (actualX + 8), (actualY+7), 16, 16, new Color(255, 255, 255, 255));
	}

	public void setSelectedIndex(int index) {
		selectedIndex = index;

		if (listSetting != null) {
			listSetting.setValue(itemsSource.get(selectedIndex));
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);

		Rectangle actualSize = this.getActualSize();
		if (actualSize != null && actualSize.isDrawable()) {
			if (event.button == MouseButton.LEFT) {
				if (this.getActualSize().getY() < event.mouseY
						&& event.mouseY < this.getActualSize().getY() + this.getActualSize().getHeight()) {

					float mouseX = (float) event.mouseX;
					float actualX = this.getActualSize().getX();
					float actualWidth = this.getActualSize().getWidth();

					if (mouseX > actualX && mouseX < (actualX + 32)) {
						setSelectedIndex(Math.max(selectedIndex - 1, 0));
					} else if (mouseX > (actualX + actualWidth - 32) && mouseX < (actualX + actualWidth))
						setSelectedIndex(Math.min(selectedIndex + 1, itemsSource.size() - 1));

					event.cancel();
				}
			}
		}
	}
}
