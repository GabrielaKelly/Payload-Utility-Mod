package net.payload.gui.components;

import net.payload.gui.ModuleSettingsStyle;
import org.joml.Matrix4f;
import net.payload.event.events.MouseClickEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.settings.types.BooleanSetting;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class CheckboxComponent extends Component {
	// Constants
	private static final float ANIMATION_SPEED = 12f;
	private static final float SNAP_THRESHOLD = 0.001f;
	private static final float SCROLL_SPEED = 30.0f;
	private static final float SCROLL_PADDING = 120.0f;

	// Core properties
	private final String originalText;
	private final String truncatedText;
	private final BooleanSetting checkbox;
	private Runnable onClick;

	// Animation states
	private long lastUpdateTime;
	private float fillCenter = 0.5f;
	private float fillSpread = 0.0f;
	private float textScrollPosition = 0.0f;
	private final boolean shouldScroll;

	private static int getMaxTextLength() {
		final int DEFAULT_LENGTH = 13;
		final int EXTENDED_LENGTH = 20;
		final int POPOUT_LENGTH = 999;

		ModuleSettingsStyle currentStyle = GuiManager.modulesettingsstyle.getValue();
		boolean isMatrixScaling = GuiManager.matrixscaling.getValue();

		if (currentStyle == ModuleSettingsStyle.Popout) {
			return POPOUT_LENGTH;
		}

		if (currentStyle == ModuleSettingsStyle.Collapsed) {
			int length = isMatrixScaling ? EXTENDED_LENGTH : DEFAULT_LENGTH;
			return length;
		}
		return DEFAULT_LENGTH;
	}

	public CheckboxComponent(BooleanSetting checkbox) {
		super();
		this.lastUpdateTime = System.currentTimeMillis();
		this.checkbox = checkbox;
		this.originalText = checkbox.displayName;
		this.shouldScroll = originalText.length() > getMaxTextLength();
		this.truncatedText = shouldScroll
				? originalText.substring(0, getMaxTextLength()) + "..."
				: originalText;

		this.setMargin(new Margin(8f, 2f, 8f, 2f));

		if (checkbox.getValue()) {
			fillCenter = 0.5f;
			fillSpread = 0.5f;
		}
	}

	@Override
	public void measure(Size availableSize) {
		preferredSize = new Size(availableSize.getWidth(), 29.0f);
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		super.draw(drawContext, partialTicks);
		MatrixStack matrixStack = drawContext.getMatrices();
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		Color baseColor = new Color(22, 22, 22, 100);
		Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6, actualWidth + 8, 30, baseColor, baseColor);


		if (fillSpread > 0) {
			float leftRel = Math.max(0f, fillCenter - fillSpread);
			float rightRel = Math.min(1f, fillCenter + fillSpread);
			float fillX = actualX + leftRel * actualWidth;
			float fillWidth = (rightRel - leftRel) * actualWidth;

				switch (GuiManager.guistyle.getValue()) {
					case Atsu:
						Color startColor = new Color(182, 220, 255, 155);
						Color endColor = new Color(185, 182, 229, 155);
						Render2D.drawHorizontalGradient(matrix4f, fillX - 5, actualY + 6, fillWidth + 8, 30, startColor, endColor);
						break;
					case Rias:
						Color riasstart = new Color(210, 41, 44, 155);
						Color riasend = new Color(86, 24, 27, 155);
						Render2D.drawHorizontalGradient(matrix4f, fillX - 5, actualY + 6, fillWidth + 8, 30, riasstart, riasend);
						break;
					case Captain:
						Color leonstart = new Color(147, 90, 160, 155);
						Color leonend = new Color(137, 166, 210, 155);
						Render2D.drawHorizontalGradient(matrix4f, fillX - 5, actualY + 6, fillWidth + 8, 30, leonstart, leonend);
						break;
				}
		}

		if (shouldScroll && isHovered()) {
			float fullTextWidth = Render2D.getStringWidth(originalText);
			float visibleWidth = actualWidth;
			float spacing = SCROLL_PADDING;

			float totalWidth = fullTextWidth + spacing;

			int copies = (int)Math.ceil(visibleWidth / totalWidth) + 2;

			drawContext.enableScissor(
					(int)(actualX - 2),
					(int)(actualY + 6),
					(int)(actualX + actualWidth + 3),
					(int)(actualY + 36)
			);

			for (int i = 0; i < copies; i++) {
				float xPos = actualX + i * totalWidth - (textScrollPosition % totalWidth);
				Render2D.drawString(
						drawContext,
						originalText,
						xPos,
						Math.round(actualY + 13),
						0xFFFFFF
				);
			}

			drawContext.disableScissor();
		} else {
			Render2D.drawString(
					drawContext,
					truncatedText,
					actualX + 1,
					Math.round(actualY + 13),
					0xFFFFFF
			);
		}
	}

	@Override
	protected void onHoverStateChanged(boolean hovered) {
		if (!hovered) {
			textScrollPosition = 0;
		}
	}

	@Override
	public void update() {
		super.update();

		long currentTime = System.currentTimeMillis();
		float deltaTime = (currentTime - lastUpdateTime) / 1000f;
		lastUpdateTime = currentTime;

		float targetSpread = checkbox.getValue()
				? Math.max(fillCenter, 1.0f - fillCenter)
				: 0.0f;
		float factor = 1 - (float) Math.exp(-ANIMATION_SPEED * deltaTime);
		fillSpread = lerp(fillSpread, targetSpread, factor);

		if (Math.abs(fillSpread - targetSpread) < SNAP_THRESHOLD) {
			fillSpread = targetSpread;
		}

		if (shouldScroll && isHovered()) {
			float fullTextWidth = Render2D.getStringWidth(originalText);
			float spacing = SCROLL_PADDING;
			float totalWidth = fullTextWidth + spacing;
			textScrollPosition += SCROLL_SPEED * deltaTime;
		} else {
			textScrollPosition = 0;
		}
	}


	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);
		if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN && isHovered()) {
			float clickX = (float) event.mouseX;
			float actualX = this.getActualSize().getX();
			float actualWidth = this.getActualSize().getWidth();

			float relativeClick = Math.max(0f, Math.min(1f,
					(clickX - actualX) / actualWidth));

			fillCenter = relativeClick;
			fillSpread = 0.0f;
			checkbox.toggle();

			if (onClick != null) {
				onClick.run();
			}
			event.cancel();
		}
	}


	private float lerp(float start, float end, float factor) {
		return start + factor * (end - start);
	}

	public void setChecked(boolean checked) {
		checkbox.setValue(checked);
		if (checked) {
			fillCenter = 0.5f;
			fillSpread = 0.0f;
		} else {
			fillSpread = 0.5f;
		}
	}

	public boolean isChecked() {
		return checkbox.getValue();
	}

	public void setOnClick(Runnable onClick) {
		this.onClick = onClick;
	}
}