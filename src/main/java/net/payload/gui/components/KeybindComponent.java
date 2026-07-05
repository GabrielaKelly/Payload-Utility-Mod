package net.payload.gui.components;

import net.payload.Payload;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import net.payload.event.events.KeyDownEvent;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.listeners.KeyDownListener;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.settings.types.KeybindSetting;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;

public class KeybindComponent extends Component implements KeyDownListener {
	private boolean listeningForKey;
	private KeybindSetting keyBind;

	public KeybindComponent(KeybindSetting keyBind) {
		super();
		this.setMargin(new Margin(8f, 2f, 8f, 2f));
		this.keyBind = keyBind;
	}

	@Override
	public void onVisibilityChanged() {
		super.onVisibilityChanged();
		if (this.isVisible()) {
			Payload.getInstance().eventManager.AddListener(KeyDownListener.class, this);
		} else {
			Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
		}
	}

	@Override
	public void measure(Size availableSize) {
		preferredSize = new Size(availableSize.getWidth(), 45.0f);
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

		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		float actualHeight = this.getActualSize().getHeight();
		switch (GuiManager.modulesettingsstyle.getValue()) {
			case Popout:
				// Background and base text
				Color baseColor = new Color(22, 22, 22, 100);
				Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6, actualWidth + 8, 31, baseColor, baseColor);
				Render2D.drawString(drawContext, "Keybind", actualX + 1, Math.round(actualY + 13), 0xFFFFFF);

				String keyBindText = this.keyBind.getValue().getLocalizedText().getString();
				if (keyBindText.equals("scancode.0") || keyBindText.equals("key.keyboard.0")) {
					keyBindText = "Not Bound";
				}

				// Render different text based on state
				if (listeningForKey) {
					long time = System.currentTimeMillis();
					int frame = (int) ((time / 500) % 3);
					String anim = switch (frame) {
						case 0 -> "Listening.";
						case 1 -> "Listening..";
						case 2 -> "Listening...";
						default -> "Listening";
					};
					Render2D.drawString(drawContext, anim, (actualX + actualWidth) - 101, Math.round(actualY + 13), 0xFFFFFF);
				} else {
					if (keyBindText.equals("Not Bound")) {
						// Render "Not Bound" at left-aligned position
						Render2D.drawString(drawContext,
								keyBindText,
								actualX + actualWidth - 101,
								Math.round(actualY + 13),
								0x9B9B9B
						);
					} else {
						// Render actual keybind at right-aligned position
						Render2D.drawString(drawContext,
								keyBindText,
								actualX + actualWidth - 25,
								Math.round(actualY + 13),
								0x9B9B9B
						);
					}
				}
				break;
			case Collapsed:
				Color baseColor2 = new Color(22, 22, 22, 100);
				Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6, actualWidth + 8, 31, baseColor2, baseColor2);
				Render2D.drawString(drawContext, "Bind", actualX + 1, Math.round(actualY + 13), 0xFFFFFF);

				String keyBindText2 = this.keyBind.getValue().getLocalizedText().getString();
				if (keyBindText2.equals("scancode.0") || keyBindText2.equals("key.keyboard.0")) {
					keyBindText2 = "Not Bound";
				}

				// Render different text based on state
				if (listeningForKey) {
					long time = System.currentTimeMillis();
					int frame = (int) ((time / 500) % 3);
					String anim = switch (frame) {
						case 0 -> "Listening.";
						case 1 -> "Listening..";
						case 2 -> "Listening...";
						default -> "Listening";
					};
					Render2D.drawString(drawContext, anim, (actualX + actualWidth) - 101, Math.round(actualY + 13), 0xFFFFFF);
				} else {
					if (keyBindText2.equals("Not Bound")) {
						// Render "Not Bound" at left-aligned position
						Render2D.drawString(drawContext,
								keyBindText2,
								actualX + actualWidth - 101,
								Math.round(actualY + 13),
								0x9B9B9B
						);
					} else {
						Render2D.drawString(drawContext,
								keyBindText2,
								actualX + actualWidth - 25,
								Math.round(actualY + 13),
								0x9B9B9B
						);
					}
				}
				break;
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);
		if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
			if (hovered) {
				setListeningForKey(true);
				event.cancel();
			} else {
				setListeningForKey(false);
			}
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (listeningForKey) {
			int key = event.GetKey();
			int scanCode = event.GetScanCode();

			if (key == GLFW.GLFW_KEY_ESCAPE) {
				keyBind.setValue(InputUtil.UNKNOWN_KEY);
			} else {
				keyBind.setValue(InputUtil.fromKeyCode(key, scanCode));
			}

			listeningForKey = false;
			event.cancel();
		}
	}

	private void setListeningForKey(boolean state) {
		listeningForKey = state;
		if (listeningForKey) {
			Payload.getInstance().eventManager.AddListener(KeyDownListener.class, this);
		} else {
			Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
		}
	}
}