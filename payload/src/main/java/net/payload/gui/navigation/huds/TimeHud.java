package net.payload.gui.navigation.huds;

import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class TimeHud extends HudWindow {
	private String timeText = null;

	public TimeHud(int x, int y) {
		super("TimeHud", x, y, 80, 24);
		this.minWidth = 80f;
		this.minHeight = 20f;
		this.maxHeight = 20f;
		resizeMode = ResizeMode.None;
	}

	@Override
	public void update() {
		super.update();
		if (MC.world != null) {
			int time = ((int) MC.world.getTime() + 6000) % 24000;
			String suffix = time >= 12000 ? "PM" : "AM";

			StringBuilder timeString = new StringBuilder((time / 10) % 1200 + "");
			while (timeString.length() < 4) {
				timeString.insert(0, "0");
			}

			String[] strsplit = timeString.toString().split("");
			String hours = strsplit[0] + strsplit[1];
			if (hours.equalsIgnoreCase("00")) {
				hours = "12";
			}

			int minutes = (int) Math.floor(Double.parseDouble(strsplit[2] + strsplit[3]) / 100.0 * 60.0);
			String sm = minutes < 10 ? "0" + minutes : String.valueOf(minutes);

			timeText = hours + ":" + sm.charAt(0) + sm.charAt(1) + suffix;
		} else {
			timeText = null;
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		if (!isVisible() || timeText == null) {
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
			Render2D.drawString(
					drawContext,
					timeText,
					pos.getX(),
					pos.getY(),
					GuiManager.foregroundColor.getValue().getColorAsInt()
			);
		} finally {
			matrixStack.pop();
		}

		super.draw(drawContext, partialTicks);
	}
}