package net.payload.gui.navigation.huds;

import net.payload.gui.*;
import net.payload.gui.navigation.HudWindow;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import static net.payload.gui.GuiManager.speedSize;

public class SpeedNumberHud extends HudWindow {
	private static final float BASE_HEIGHT = 20f;
	private static final double SPEED_SMOOTHING_FACTOR = 0.5;

	private String speedText = null;
	private double lastSpeed = 0.0;
	private int tickCounter = 0;
	private float currentScale = 1.0f;

	public SpeedNumberHud(int x, int y) {
		super("Speedometer", x, y, 50, 24);
		resizeMode = ResizeMode.None;
		updateDimensions();
	}

	private float calculateWidth() {
		if (MC.player == null || MC.textRenderer == null || speedText == null) {
			return 50f;
		}

		float textWidth = MC.textRenderer.getWidth(speedText);
		return textWidth + 8f;
	}

	private void updateDimensions() {
		float baseWidth = calculateWidth();

		switch (speedSize.getValue()) {
			case SMALL -> currentScale = 0.5f;
			case LARGE -> currentScale = 2.0f;
			case MASSIVE -> currentScale = 3.0f;
			default -> currentScale = 1.0f;
		}

		float finalWidth = baseWidth * currentScale * 1.92f;
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
	}

	private void updateSpeed() {
		PlayerEntity player = MC.player;
		if (player != null) {
			double dx = player.getX() - player.prevX;
			double dz = player.getZ() - player.prevZ;
			double dy = player.getY() - player.prevY;

			double currentSpeed = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20;

			if (GuiManager.speedUnit.getValue() == SpeedUnit.KMPH) {
				currentSpeed *= 3.6;
			}

			double smoothedSpeed = (lastSpeed * SPEED_SMOOTHING_FACTOR) +
					(currentSpeed * (1.0 - SPEED_SMOOTHING_FACTOR));

			lastSpeed = smoothedSpeed;

			String unit = GuiManager.speedUnit.getValue().getDisplay();
			speedText = String.format("Speed: %.1f %s", lastSpeed, unit);

			updateDimensions();
		} else {
			speedText = null;
			lastSpeed = 0.0;
		}
	}

	@Override
	public void update() {
		super.update();

		float updateInterval = Math.max(1, (int)(GuiManager.SpeedUpdateDelay.getValue() * 20));
		if (++tickCounter >= updateInterval) {
			tickCounter = 0;
			updateSpeed();
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		if (!isVisible() || speedText == null) {
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

			Render2D.drawString(
					drawContext,
					speedText,
					pos.getX() / currentScale,
					pos.getY() / currentScale,
					GuiManager.speedhudcolor.getValue().getColorAsInt()
			);
		} finally {
			matrixStack.pop();
		}

		super.draw(drawContext, partialTicks);
	}

	public double getCurrentSpeed() {
		return lastSpeed;
	}
}