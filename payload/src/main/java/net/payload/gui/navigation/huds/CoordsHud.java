package net.payload.gui.navigation.huds;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.payload.gui.CoordStyles;
import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static net.payload.gui.GuiManager.*;
import static net.payload.gui.CoordStyles.*;

public class CoordsHud extends HudWindow {
	private static final float BASE_HEIGHT = 23f;
	private float currentScale = 1.0f;

	public CoordsHud(int x, int y) {
		super("CoordsHud", x, y, 50, BASE_HEIGHT);
		resizeMode = ResizeMode.None;

		SettingManager.registerSetting(coordStyles);
		SettingManager.registerSetting(poscolor);
		SettingManager.registerSetting(coordssizeMode);

		updateDimensions();
	}

	private String getCoordinatesText() {
		if (MC.player == null) return "";

		RegistryKey<World> dimensionKey = MC.player.getWorld().getRegistryKey();

		double x = MC.player.getX();
		double y = MC.player.getY();
		double z = MC.player.getZ();
		double netherX = x / 8.0;
		double netherZ = z / 8.0;

		if (dimensionKey == World.NETHER) {
			netherX = x * 8.0;
			netherZ = z * 8.0;
		}

		if (coordStyles.getValue().equals(CoordStyles.PAYLOAD)) {
			return String.format("(%.0f, %.0f, %.0f) [%.0f, %.0f]",
					x, y, z, netherX, netherZ);
		} else if (coordStyles.getValue().equals(CoordStyles.FUTURE)) {
			return String.format("\u00a77\u00a7lXYZ \u00a7f\u00a7l%.1f\u00a77, \u00a7f\u00a7l%.1f\u00a77, \u00a7f\u00a7l%.1f \u00a77\u00a7l[\u00a7f\u00a7l%.1f\u00a77, \u00a7f\u00a7l%.1f\u00a77\u00a7l]",
					x, y, z, netherX, netherZ);
		}
		return "";
	}

	private float calculateWidth() {
		if (MC.player == null || MC.textRenderer == null) return 50f;

		String coordsText = getCoordinatesText();
		return MC.textRenderer.getWidth(coordsText) + 8f;
	}

	private void updateDimensions() {
		float baseWidth = calculateWidth();

		switch (coordssizeMode.getValue()) {
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
		if (!isVisible() || MC.player == null) {
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

			String coordsText = getCoordinatesText();
			Render2D.drawString(
					drawContext,
					coordsText,
					Math.round(renderX + (4 / currentScale)),
					Math.round(renderY + (3 / currentScale)),
					poscolor.getValue().getColorAsInt()
			);
		} finally {
			matrixStack.pop();
		}

		super.draw(drawContext, partialTicks);
	}
}