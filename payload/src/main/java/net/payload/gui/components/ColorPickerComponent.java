/*
 * payload Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.payload.gui.components;

import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.events.MouseMoveEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Rectangle;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.gui.colors.Colors;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.ColorSetting.ColorMode;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static net.payload.utils.render.TextureBank.picker;
import static net.payload.utils.render.TextureBank.picker2;
import static net.payload.utils.render.TextureBank.leftarrow;
import static net.payload.utils.render.TextureBank.rightarrow;

public class ColorPickerComponent extends Component {

	private String text;
	private boolean isSliding = false;
	private boolean collapsed = true;
	private ColorSetting color;
	private static final float ANIMATION_SPEED = 12f;
	private static final int MAX_TEXT_LENGTH = 15;
	private static final float SCROLL_SPEED = 30.0f;
	private static final float SCROLL_PADDING = 90.0f;

	private String originalText;
	private String truncatedText;
	private float textScrollPosition = 0.0f;
	private boolean shouldScroll;
	private long lastUpdateTime;

	private static int getMaxTextLength() {
		switch (GuiManager.modulesettingsstyle.getValue()) {
			case Popout:
				return 999;
			case Collapsed:
			default:
				return 15;
		}
	}
	public ColorPickerComponent(String text) {
		super();
		this.lastUpdateTime = System.currentTimeMillis();
		this.originalText = text;
		this.shouldScroll = originalText.length() > MAX_TEXT_LENGTH;
		this.shouldScroll = originalText.length() > getMaxTextLength();
		this.truncatedText = shouldScroll
				? originalText.substring(0, getMaxTextLength()) + "..."  // Fix: Call the method
				: originalText;
		this.text = text;
		this.setMargin(new Margin(8f, 2f, 8f, 2f));
	}

	public ColorPickerComponent(ColorSetting color) {
		super();
		this.text = color.displayName;
		this.color = color;
		this.lastUpdateTime = System.currentTimeMillis();
		this.originalText = color.displayName;
		this.shouldScroll = originalText.length() > MAX_TEXT_LENGTH;
		this.shouldScroll = originalText.length() > getMaxTextLength();
		this.truncatedText = shouldScroll
				? originalText.substring(0, getMaxTextLength()) + "..."  // Fix: Call the method
				: originalText;
		this.setMargin(new Margin(8f, 2f, 8f, 2f));
	}


	@Override
	public void update() {
		super.update();

		long currentTime = System.currentTimeMillis();
		float deltaTime = (currentTime - lastUpdateTime) / 1000f;
		lastUpdateTime = currentTime;

		if (shouldScroll && isHovered()) {
			float fullTextWidth = Render2D.getStringWidth(originalText);
			float spacing = SCROLL_PADDING;
			float totalWidth = fullTextWidth + spacing;
			textScrollPosition += SCROLL_SPEED * deltaTime;
		} else {
			textScrollPosition = 0;
		}
	}

	public void setText(String text) {
		this.originalText = text;
		this.shouldScroll = text.length() > MAX_TEXT_LENGTH;
		this.truncatedText = shouldScroll
				? text.substring(0, MAX_TEXT_LENGTH) + "..."
				: text;
	}

	public String getText() {
		return this.originalText;
	}

	@Override
	public void measure(Size availableSize) {
		if (collapsed) {
			preferredSize = new Size(availableSize.getWidth(), 30.0f);
		} else {
			preferredSize = new Size(availableSize.getWidth(), 205.0f);
		}
	}

	@Override
	protected void onHoverStateChanged(boolean hovered) {
		if (!hovered) {
			textScrollPosition = 0;
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);
		if (event.button == MouseButton.LEFT) {
			if (event.action == MouseAction.DOWN) {
				if (hovered) {
					float mouseX = (float) event.mouseX;
					float mouseY = (float) event.mouseY;
					float actualY = actualSize.getY();
					if (mouseY < actualY + 29) {
						collapsed = !collapsed;
						invalidateMeasure();
						event.cancel();
					} else if (!collapsed) {
						if (mouseY > actualY + 29 && mouseY <= actualY + 59) {
							float actualX = this.getActualSize().getX();
							float actualWidth = this.getActualSize().getWidth();

							Rectangle leftButton = new Rectangle(actualX + actualWidth - 228, actualY + 38, 234.0f, 24.0f);

							ColorMode[] enumConstants = color.getMode().getDeclaringClass().getEnumConstants();
							int currentIndex = java.util.Arrays.asList(enumConstants).indexOf(color.getMode());
							int enumCount = enumConstants.length;
							if (leftButton.intersects(mouseX, mouseY)) {
								currentIndex = (currentIndex - 1 + enumCount) % enumCount;
							}

							color.setMode(enumConstants[currentIndex]);
						} else if (mouseY > actualY + 59) {
							if (!collapsed)
								isSliding = true;
						}
					}
					event.cancel();
				}
			} else if (event.action == MouseAction.UP) {
				isSliding = false;
			}
		}
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		super.onMouseMove(event);

		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		float actualHeight = this.getActualSize().getHeight();

		float alphaHeight = 20f;
		float mainHeight = actualHeight - 63 - alphaHeight - 5;
		float alphaY = actualY + 59 + mainHeight + 5;

		double mouseX = event.getX();
		double mouseY = event.getY();
		if (Payload.getInstance().guiManager.isClickGuiOpen() && this.isSliding) {
			Color colorToModify = color.getValue();

			if (mouseX >= actualX && mouseX <= actualX + actualWidth - 28
					&& mouseY >= actualY + 59 && mouseY <= actualY + 59 + mainHeight) {
				float horizontal = (float) Math.min(Math.max((mouseX - actualX) / (actualWidth - 28), 0.0f), 1.0f);
				float vertical = (float) Math.min(Math.max(1.0f - ((mouseY - (actualY + 59)) / mainHeight), 0.0f), 1.0f);
				colorToModify.setSaturation(horizontal);
				colorToModify.setLuminance(vertical);
			}

			else if (mouseX >= actualX + actualWidth - 25 && mouseX <= actualX + actualWidth
					&& mouseY >= actualY + 59 && mouseY <= actualY + 59 + mainHeight) {
				float vertical = (float) Math.min(Math.max((mouseY - (actualY + 59)) / mainHeight, 0.0f), 1.0f);
				colorToModify.setHue(vertical * 360.0f);
			}

			else if (mouseY >= alphaY && mouseY <= alphaY + alphaHeight) {
				float horizontal = (float) Math.min(Math.max((mouseX - actualX) / actualWidth, 0.0f), 1.0f);
				colorToModify.setAlpha(Math.round(horizontal * 255.0f));
			}
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		MatrixStack matrixStack = drawContext.getMatrices();
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();


		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		float actualHeight = this.getActualSize().getHeight();

		Color startColor = new Color(22, 22, 22, 100);
		Color endColor = new Color(22, 22, 22, 100);
		Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6, actualWidth + 8, 31, startColor, endColor);

		if (shouldScroll && isHovered()) {
			float fullTextWidth = Render2D.getStringWidth(originalText);
			float visibleWidth = actualWidth;
			float spacing = SCROLL_PADDING;

			float totalWidth = fullTextWidth + spacing;
			int copies = (int)Math.ceil(visibleWidth / totalWidth) + 2;

			drawContext.enableScissor(
					(int)(actualX - 2),
					(int)(actualY + 6),
					(int)(actualX + actualWidth - 20),
					(int)(actualY + 36)
			);

			for (int i = 0; i < copies; i++) {
				float xPos = actualX + i * totalWidth - (textScrollPosition % totalWidth);
				Render2D.drawString(
						drawContext,
						originalText,
						xPos,
						Math.round(actualY + 14),
						0xFFFFFF
				);
			}

			drawContext.disableScissor();
		} else {
			Render2D.drawString(
					drawContext,
					truncatedText,
					actualX + 1,
					Math.round(actualY + 14),
					0xFFFFFF
			);
		}

		switch (GuiManager.modulesettingsstyle.get()) {
			case Popout:
				if (!collapsed) {
					Render2D.drawBoxOutline(matrix4f, actualX + 210, actualY + 13, 17, 17, new Color(0, 0, 0, 255));
					Render2D.drawBox(matrix4f, actualX + 210, actualY + 13, 16, 16, color.getValue());
				} else {
					Render2D.drawBoxOutline(matrix4f, actualX + 210, actualY + 13, 17, 17, new Color(0, 0, 0, 255));
					Render2D.drawBox(matrix4f, actualX + 210, actualY + 13, 16, 16, color.getValue());
				}
				break;
			case Collapsed:
				if (!collapsed) {
					Render2D.drawBoxOutline(matrix4f, actualX + 150, actualY + 13, 9, 9, new Color(0, 0, 0, 255));
					Render2D.drawBox(matrix4f, actualX + 150, actualY + 13, 8, 8, color.getValue());
				} else {
					Render2D.drawBoxOutline(matrix4f, actualX + 150, actualY + 13, 9, 9, new Color(0, 0, 0, 255));
					Render2D.drawBox(matrix4f, actualX + 150, actualY + 13, 8, 8, color.getValue());
				}
				break;
		}

		if (!collapsed) {
			Render2D.drawString(drawContext, "Mode", actualX+1, Math.round(actualY + 40), 0xFFFFFF);
			Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 37, actualWidth + 8, 175, startColor, endColor);

			String enumText = color.getMode().name();
			float stringLength = Render2D.getStringWidth(enumText);
			Render2D.drawString(drawContext, enumText, actualX+40 + actualWidth - 75 - stringLength, Math.round(actualY + 40), 0xFFFFFF);

			float alphaHeight = 20f;
			float mainHeight = actualHeight - 63 - alphaHeight - 5;
			float alphaY = actualY + 59 + mainHeight + 5;

			Color colorSetting = this.color.getValue();
			Color hueColor = new Color(colorSetting.getHue(), 1f, 1f);
			Color color1 = new Color(255,255,255,0);
			Color color2 = new Color(colorSetting.getHue(), 1f, 1f);


			// Main gradient area (fixed alpha)
			Render2D.drawHorizontalGradient(matrix4f, actualX, actualY + 59, actualWidth - 28, mainHeight,
					new Color(255, 255, 255), hueColor);
			Render2D.drawVerticalGradient(matrix4f, actualX, actualY + 59, actualWidth - 28, mainHeight,
					new Color(0, 0, 0, 0), new Color(0, 0, 0));

			// Hue strip
			float increment = (mainHeight / 6.0f);
			Render2D.drawVerticalGradient(matrix4f, actualX + actualWidth - 25, actualY + 59, 20, increment,
					new Color(255, 0, 0), new Color(255, 255, 0));
			Render2D.drawVerticalGradient(matrix4f, actualX + actualWidth - 25, actualY + 59 + increment, 20, increment,
					new Color(255, 255, 0), new Color(0, 255, 0));
			Render2D.drawVerticalGradient(matrix4f, actualX + actualWidth - 25, actualY + 59 + (2 * increment), 20,
					increment, new Color(0, 255, 0), new Color(0, 255, 255));
			Render2D.drawVerticalGradient(matrix4f, actualX + actualWidth - 25, actualY + 59 + (3 * increment), 20,
					increment, new Color(0, 255, 255), new Color(0, 0, 255));
			Render2D.drawVerticalGradient(matrix4f, actualX + actualWidth - 25, actualY + 59 + (4 * increment), 20,
					increment, new Color(0, 0, 255), new Color(255, 0, 255));
			Render2D.drawVerticalGradient(matrix4f, actualX + actualWidth - 25, actualY + 59 + (5 * increment), 20,
					increment, new Color(255, 0, 255), new Color(255, 0, 0));

			Render2D.drawBoxOutline(matrix4f, actualX, actualY + 59, actualWidth - 28, mainHeight, Colors.Black);
			Render2D.drawBoxOutline(matrix4f, actualX + actualWidth - 25, actualY + 59, 20, mainHeight, Colors.Black);
			Render2D.drawBoxOutline(matrix4f, actualX, alphaY, actualWidth, alphaHeight, Colors.Black);
// Alpha slider rendering (-5 for test)
			Render2D.drawHorizontalGradient(matrix4f, actualX, alphaY, actualWidth, alphaHeight, color1, color2);


			// Main gradient indicator
			Render2D.drawCircle(matrix4f, actualX + (colorSetting.getSaturation() * (actualWidth - 28)),
					actualY + 59 + ((1.0f - colorSetting.getLuminance()) * mainHeight), 1, Colors.White);

			// Hue strip indicator
			Render2D.drawOutlinedBox(matrix4f, actualX + actualWidth - 25, actualY + 59 + (colorSetting.getHue() / 360.0f * mainHeight), 20, 2, Colors.Black, Colors.Black);
			//	Render2D.drawHorizontalGradient(matrix4f, actualX + actualWidth - 25, actualY + 59 + (colorSetting.getHue() / 360.0f * mainHeight), 20, 2, startColor, endColor);

			// Alpha slider indicator
			float alphaX = actualX + (colorSetting.getAlpha()) * actualWidth;
			//Render2D.drawVerticalGradient(matrix4f, alphaX - 1, alphaY, 2, alphaHeight, startColor, endColor);
			Render2D.drawBox(matrix4f, alphaX - 1, alphaY, 1, alphaHeight, Colors.Black);
			Render2D.drawBoxOutline(matrix4f, alphaX - 1, alphaY, 2, alphaHeight, Colors.Black);
		}


	}
}