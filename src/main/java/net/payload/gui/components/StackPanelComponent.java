

package net.payload.gui.components;

import java.util.List;

import net.payload.gui.Rectangle;
import net.payload.gui.Size;
import net.payload.gui.UIElement;

public class StackPanelComponent extends Component {
	public enum StackType {
		Horizontal, Vertical
	}

	protected StackType stackType = StackType.Vertical;

	public StackPanelComponent() {
		super();
	}

	@Override
	public void measure(Size availableSize) {
		Size newSize = new Size(availableSize.getWidth(), 0.0f);
		List<UIElement> children = getChildren();
		if (children.size() > 0) {
			for (UIElement element : children) {
				if (element == null || !element.isVisible())
					continue;

				element.measure(availableSize);
				Size resultingSize = element.getPreferredSize();
				newSize.setHeight(newSize.getHeight() + resultingSize.getHeight());
			}
		}
		preferredSize = newSize;
	}

	@Override
	public void arrange(Rectangle finalSize) {
		if (this.parent != null) {
			setActualSize(finalSize);
		}

		float y = 0;
		List<UIElement> children = getChildren();
		for (UIElement element : children) {
			if (element == null || !element.isVisible())
				continue;

			Size preferredSize = element.getPreferredSize();
			element.arrange(new Rectangle(finalSize.getX(), finalSize.getY() + y, finalSize.getWidth(),
					preferredSize.getHeight()));
			y += preferredSize.getHeight();
		}
	}
}
