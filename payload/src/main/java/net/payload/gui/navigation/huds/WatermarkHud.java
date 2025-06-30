package net.payload.gui.navigation.huds;

import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.WatermarkStyle;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.HudWindow;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static net.payload.gui.GuiManager.*;
import static net.payload.gui.GuiStyle.Atsu;

public class WatermarkHud extends HudWindow {
	private static final float BASE_HEIGHT = 20f;
	private static final float BASE_PADDING = 8f;
	private float currentScale = 1.0f;
	private float currentWidth = 50f;

	public WatermarkHud(int x, int y) {
		super("WatermarkHud", x, y, 50, BASE_HEIGHT);
		resizeMode = ResizeMode.None;

		SettingManager.registerSetting(watermarkStyleEnumSetting);
		SettingManager.registerSetting(watermarkcolors);
		SettingManager.registerSetting(sizeMode);

		updateDimensions();
	}

	private String getWatermarkText() {
		return "Payload pre-release";
	}

	private float calculateWidth() {
		if (MC.textRenderer == null) return 50f;
		float baseWidth = MC.textRenderer.getWidth(getWatermarkText());
		return baseWidth + BASE_PADDING;
	}

	private void updateDimensions() {
		float baseWidth = calculateWidth();

		switch (sizeMode.getValue()) {
			case SMALL -> currentScale = 0.5f;
			case LARGE -> currentScale = 2.0f;
			case MASSIVE -> currentScale = 3.0f;
			default -> currentScale = 1.0f;
		}

		float finalWidth = baseWidth * currentScale;
		float finalHeight = BASE_HEIGHT * currentScale;

		Rectangle currentPos = position.getValue();
		position.setValue(new Rectangle(
				currentPos.getX(),
				currentPos.getY(),
				finalWidth,
				finalHeight
		));

		this.setWidth(finalWidth);
		this.setHeight(finalHeight);
		this.minWidth = this.maxWidth = finalWidth;
		this.minHeight = this.maxHeight = finalHeight;
		this.currentWidth = finalWidth;
	}

	@Override
	public void update() {
		super.update();
		if (MC.textRenderer != null) {
			updateDimensions();
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		if (!isVisible()) {
			super.draw(drawContext, partialTicks);
			return;
		}

		Rectangle pos = position.getValue();
		if (!pos.isDrawable()) {
			super.draw(drawContext, partialTicks);
			return;
		}

		MatrixStack matrixStack = drawContext.getMatrices();
		matrixStack.push();
		try {
			matrixStack.scale(currentScale, currentScale, currentScale);

			float renderX = pos.getX() / currentScale;
			float renderY = pos.getY() / currentScale;
			String watermarkText = getWatermarkText();

			if (watermarkStyleEnumSetting.getValue() == WatermarkStyle.GRADIENT) {
				int startColor = guistyle.getValue() == Atsu ? 0xB6DCFF : 0xd2292c;
				int endColor = guistyle.getValue() == Atsu ? 0xB9B6E5 : 0x56181b;

				Render2D.drawGradientString(
						drawContext,
						watermarkText,
						renderX + (4 / currentScale),
						renderY + (3 / currentScale),
						startColor,
						endColor
				);
			} else {
				Render2D.drawString(
						drawContext,
						watermarkText,
						Math.round(renderX + (4 / currentScale)),
						Math.round(renderY + (3 / currentScale)),
						watermarkcolors.getValue().getColorAsInt()
				);
			}
		} finally {
			matrixStack.pop();
		}

		super.draw(drawContext, partialTicks);
	}
}