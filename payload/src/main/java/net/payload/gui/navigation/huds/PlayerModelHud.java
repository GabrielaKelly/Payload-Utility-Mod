package net.payload.gui.navigation.huds;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.CenterOrientation;
import net.payload.gui.navigation.HudWindow;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.blaze3d.systems.RenderSystem;

import static net.payload.gui.GuiManager.*;

public class PlayerModelHud extends HudWindow {
	private static final float BASE_HEIGHT = 90f;
	private static final float BASE_WIDTH = 50f;
	private static final int DEFAULT_MODEL_SIZE = 30;
	private static final float ROTATION_SMOOTHING = 0.5f;

	private float currentScale = 1.0f;
	private float lastYaw = 0f;
	private float lastPitch = 0f;

	public PlayerModelHud(int x, int y) {
		super("PlayerModelHud", x, y, BASE_WIDTH, BASE_HEIGHT);
		resizeMode = ResizeMode.None;
		updateDimensions();
	}

	private void updateDimensions() {
		switch (playerModelSizeMode.getValue()) {
			case SMALL -> currentScale = 0.5f;
			case LARGE -> currentScale = 2.0f;
			case MASSIVE -> currentScale = 3.0f;
			default -> currentScale = 1.0f;
		}

		float finalWidth = BASE_WIDTH * currentScale;
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
		updateDimensions();
	}

	private void drawEntity(DrawContext context, float x, float y, int size, float yaw, float pitch, LivingEntity entity) {
		if (entity == null) return;

		// Smooth rotation transitions
		lastYaw = MathHelper.lerp(ROTATION_SMOOTHING, lastYaw, yaw);
		lastPitch = MathHelper.lerp(ROTATION_SMOOTHING, lastPitch, pitch);

		float tanYaw = (float) Math.atan(lastYaw / 40.0f);
		float tanPitch = (float) Math.atan(lastPitch / 40.0f);

		Quaternionf quaternion = new Quaternionf().rotateZ((float) Math.PI);

		// Store original entity state
		float previousBodyYaw = entity.bodyYaw;
		float previousYaw = entity.getYaw();
		float previousPitch = entity.getPitch();
		float previousPrevHeadYaw = entity.prevHeadYaw;
		float prevHeadYaw = entity.headYaw;

		try {
			// Apply new rotations
			entity.bodyYaw = 180.0f + tanYaw * 20.0f;
			entity.setYaw(180.0f + tanYaw * 40.0f);
			entity.setPitch(-tanPitch * 20.0f);
			entity.headYaw = entity.getYaw();
			entity.prevHeadYaw = entity.getYaw();

			// Fixed: Calculate position based on the HUD dimensions and scale
			int posX = (int)(x + getWidth() / 2);
			int posY = (int)(y + getHeight() * 0.9f);

			// Ensure size is scaled properly
			int scaledSize = (int)(size * currentScale);

			InventoryScreen.drawEntity(
					context,
					posX,
					posY,
					scaledSize,
					new Vector3f(),
					quaternion,
					null,
					entity
			);

		} catch (Exception e) {
			// Fixed: Add exception handling to prevent crashes on rendering errors
			System.err.println("Error rendering player model: " + e.getMessage());
		} finally {
			// Restore original entity state
			entity.bodyYaw = previousBodyYaw;
			entity.setYaw(previousYaw);
			entity.setPitch(previousPitch);
			entity.prevHeadYaw = previousPrevHeadYaw;
			entity.headYaw = prevHeadYaw;
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		if (!isVisible() || MC.player == null) {
			super.draw(drawContext, partialTicks);
			return;
		}

		Rectangle pos = getActualSize(); // Fixed: Use getActualSize() for consistent behavior
		if (!pos.isDrawable()) {
			super.draw(drawContext, partialTicks);
			return;
		}

		PlayerEntity player = MC.player;
		if (player == null) {
			super.draw(drawContext, partialTicks);
			return;
		}

		// Save rendering state
		MatrixStack matrixStack = drawContext.getMatrices();

		// Setup GL state similar to ArmorHud
		matrixStack.push();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		// Fixed: Enable depth test to correctly render the player model
		RenderSystem.enableDepthTest();

		try {
			float offset = centerOrientation.get() == CenterOrientation.North ? 180 : 0;
			float targetYaw = copyYaw.get()
					? MathHelper.wrapDegrees(player.prevYaw +
					(player.getYaw() - player.prevYaw) *
							MC.getRenderTickCounter().getTickDelta(true) + offset)
					: (float) customYaw.get();
			float targetPitch = copyPitch.get() ? player.getPitch() : (float) customPitch.get();

			// Fixed: Use the actual position values from the Rectangle
			matrixStack.translate(pos.getX().floatValue(), pos.getY().floatValue(), 100.0); // Fixed: Add Z offset

			// Fixed: Don't apply scale in the matrix since it's handled in drawEntity
			// The scale is already applied to the HUD dimensions


			drawEntity(
					drawContext,
					0,
					0,
					DEFAULT_MODEL_SIZE,
					-targetYaw,
					-targetPitch,
					player
			);

		} finally {
			// Restore GL state
			matrixStack.pop();
			RenderSystem.disableBlend();
			RenderSystem.disableDepthTest(); // Fixed: Disable depth test after rendering
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}

		// Ensure we call the parent's draw method
		super.draw(drawContext, partialTicks);
	}

	@Override
	public void reset() {
		super.reset(); // Fixed: Call parent reset
		lastYaw = 0f;
		lastPitch = 0f;
	}
}