

package net.payload.gui.components;

import net.payload.gui.Margin;
import net.payload.gui.UIElement;

public abstract class Component extends UIElement {
	public String header = null;

	public Component() {
		super();
		this.margin = new Margin();
	}

	@Override
	public void onVisibilityChanged() {
		super.onVisibilityChanged();
		hovered = false;
	}


	public void setHovered(boolean hovered) {
		if (this.hovered != hovered) {
			this.hovered = hovered;
			onHoverStateChanged(hovered);
		}
	}

	public boolean isHovered() {
		return hovered;
	}

	protected void onHoverStateChanged(boolean hovered) {

	}

	public boolean isPointInside(double x, double y) {
		return x >= getActualSize().getX() &&
				x <= getActualSize().getX() + getActualSize().getWidth() &&
				y >= getActualSize().getY() &&
				y <= getActualSize().getY() + getActualSize().getHeight();
	}
}
