package net.payload.gui.components;

import net.minecraft.client.util.math.MatrixStack;
import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.CloseableWindow;
import net.payload.gui.navigation.HudWindow;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import net.payload.gui.navigation.Window;
import org.joml.Quaternionf;

import static net.payload.utils.render.TextureBank.*;


// HudComponent.java
public class HudComponent extends Component {
	private String text;
	private HudWindow hud;
	private boolean spinning = false;
	private float spinAngle = 0;
	private float glowPhase = 0f;
	private static final float GLOW_SPEED = 2.5f;
	private static final float BASE_ALPHA = 0.6f;
	private static final float GLOW_INTENSITY = 0.4f;
	private static final float ANIMATION_SPEED = 12f;
	private static final float SNAP_THRESHOLD = 0.001f;
	private long lastUpdateTime;
	private float fillCenter = 0.5f;
	private float fillSpread = 0.0f;

	public HudComponent(String text, HudWindow hud) {
		super();
		this.text = text;
		this.hud = hud;
		this.lastUpdateTime = System.currentTimeMillis();
		this.setMargin(new Margin(8f, 2f, 8f, 2f));

		if (hud.activated.getValue()) {
			fillSpread = 0.5f;
		} else {
			fillSpread = 0.0f;
		}
	}

	@Override
	public void measure(Size availableSize) {
		preferredSize = new Size(availableSize.getWidth(), 28.0f);
	}

	@Override
	public void update() {
		super.update();
		boolean shouldSpin = Payload.getInstance().guiManager.isHudOptionsWindowOpen(hud.getID());
		if (spinning != shouldSpin) {
			spinning = shouldSpin;
			spinAngle = 0;
		}

		long currentTime = System.currentTimeMillis();
		float deltaTime = (currentTime - lastUpdateTime) / 1000f;

		if (spinning) {
			glowPhase += GLOW_SPEED * deltaTime;
			spinAngle = (spinAngle + 3) % 360;
		} else {
			glowPhase = 0f;
		}

		lastUpdateTime = currentTime;

		boolean isActive = hud.activated.getValue();
		float targetSpread = isActive ? 0.5f : 0.0f;

		float factor = 1 - (float) Math.exp(-ANIMATION_SPEED * deltaTime);
		fillSpread = lerp(fillSpread, targetSpread, factor);

		if (Math.abs(fillSpread - targetSpread) < SNAP_THRESHOLD) {
			fillSpread = targetSpread;
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		super.draw(drawContext, partialTicks);
		MatrixStack matrixStack = drawContext.getMatrices();
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		Render2D.drawString(drawContext, this.text, actualX + 4, actualY + 13, 0xFFFFFF);
		Color baseColor = new Color(22, 22, 22, 100);
		Render2D.drawHorizontalGradient(matrix4f, actualX - 4, actualY + 6, actualWidth + 8, 30, baseColor, baseColor);
		float alphaMod = (float) (Math.sin(glowPhase) * GLOW_INTENSITY) + BASE_ALPHA;
		int alpha = (int) (255 * alphaMod);
		float scale = 1.0f;
		Color glowColor = new Color(255, 255, 255, alpha);
		if (spinning) {
			switch (GuiManager.guistyle.getValue()) {
				case Atsu:

					matrixStack.push();
					matrixStack.translate(actualX + actualWidth - 14, actualY + 20, 0);
					matrixStack.multiply(new Quaternionf().rotateZ((float) Math.toRadians(spinAngle)));
					matrixStack.scale(scale, scale, 1.0f);
					Render2D.drawTexturedQuad(
							matrixStack.peek().getPositionMatrix(),
							thugbait,
							-8, -8,
							16, 16,
							glowColor
					);
					matrixStack.pop();
					break;
				case Captain:

					matrixStack.push();
					matrixStack.translate(actualX + actualWidth - 14, actualY + 20, 0);
					matrixStack.multiply(new Quaternionf().rotateZ((float) Math.toRadians(spinAngle)));
					matrixStack.scale(scale, scale, 1.0f);
					Render2D.drawTexturedQuad(
							matrixStack.peek().getPositionMatrix(),
							captaincog,
							-8, -8,
							16, 16,
							glowColor
					);
					matrixStack.pop();
					break;
				case Rias:
					matrixStack.push();
					matrixStack.translate(actualX + actualWidth - 14, actualY + 20, 0);
					matrixStack.multiply(new Quaternionf().rotateZ((float) Math.toRadians(spinAngle)));
					matrixStack.scale(scale, scale, 1.0f);
					Render2D.drawTexturedQuad(
							matrixStack.peek().getPositionMatrix(),
							riascog,
							-8, -8,
							16, 16,
							glowColor
					);
					matrixStack.pop();
					break;


			}

		}
		Render2D.drawString(drawContext, this.text, actualX + 4, actualY + 13, 0xFFFFFF);
		if (fillSpread > 0) {
			Color startColor = new Color(182, 220, 255, 155);
			Color endColor = new Color(185, 182, 229, 155);
			Color riasstart = new Color(210, 41, 44, 155);
			Color riasend = new Color(86, 24, 27, 155);
			Color leonstart = new Color(147, 90, 160, 155);
			Color leonend = new Color(137, 166, 210, 155);
			float leftRel = Math.max(0f, fillCenter - fillSpread);
			float rightRel = Math.min(1f, fillCenter + fillSpread);
			float fillX = actualX + leftRel * actualWidth;
			float fillWidth = (rightRel - leftRel) * actualWidth;
			Render2D.drawString(drawContext, this.text, actualX + 4, actualY + 13, 0xFFFFFF);
			switch (GuiManager.guistyle.getValue()) {
				case Atsu:
					Render2D.drawHorizontalGradient(matrix4f, fillX - 4, actualY + 6, fillWidth + 8, 30, startColor, endColor);
					break;
				case Captain:
					Render2D.drawHorizontalGradient(matrix4f, fillX - 4, actualY + 6, fillWidth + 8, 30, leonstart, leonend);
					break;
				case Rias:
					Render2D.drawHorizontalGradient(matrix4f, fillX - 4, actualY + 6, fillWidth + 8, 30, riasstart, riasend);
					break;
            }
			Render2D.drawString(drawContext, this.text, actualX + 4, actualY + 13, 0xFFFFFF);
		}
		Render2D.drawString(drawContext, this.text, actualX + 4, actualY + 13, 0xFFFFFF);

	}
	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);
		GuiManager guiManager = Payload.getInstance().guiManager;
		String hudId = this.hud.getID();

		if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
			if (this.hovered) {
				boolean visibility = hud.activated.getValue();
				Payload.getInstance().guiManager.setHudActive(hud, !visibility);
				event.cancel();
			}
		}
		
		else if (this.hovered && event.button == MouseButton.RIGHT && event.action == MouseAction.DOWN) {
			if (guiManager.isHudOptionsWindowOpen(hudId)) {
				guiManager.hideHudOptionsWindow(hudId);
			} else {
				guiManager.showHudOptionsWindow(hudId);
			}
			event.cancel();
		}
	}

	private float lerp(float start, float end, float factor) {
		return start + factor * (end - start);
	}
}