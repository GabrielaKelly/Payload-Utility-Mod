package net.payload.gui.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.UIElement;
import net.payload.gui.colors.Color;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;

import static net.payload.gui.GuiStyle.Atsu;

public class HudWindow extends Window {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	
		protected float getScalingFactor() {
			float baseDPI = 96.0f;
			float currentDPI = (float) (MC.getWindow().getScaleFactor() * baseDPI);
			return Math.min(currentDPI / baseDPI, 3.0f);
		}

	private static final Color HOVER_COLOR = new Color(191, 253, 253);
	private static final Color DRAG_COLOR = new Color(255, 255, 255, 50);
	private static final Color GUIDE_LINE_COLOR = new Color(255, 255, 255, 100);
	private static final Color BORDER_COLOR = new Color(255, 255, 255, 100);
	private static final Color SNAP_INDICATOR_COLOR = new Color(255, 0, 255, 0);
	private boolean snapEngaged = false;
	private float snapStartX = 0;
	private float snapStartY = 0;
	private boolean hasPendingSnapUpdate = false;
	private float pendingSnapX = 0;
	private float pendingSnapY = 0;
	private boolean updateSnapX = false;
	private boolean updateSnapY = false;
	private boolean bypassBaseDrag = false;
	private float mouseStartX = 0;
	private float mouseStartY = 0;
	private float elementStartX = 0;
	private float elementStartY = 0;
	private static final float SNAP_ENGAGEMENT_THRESHOLD = 8.0f;
	private static final float SNAP_RELEASE_THRESHOLD = 20.0f;

	private final AtomicReference<SnapState> currentSnapState = new AtomicReference<>(new SnapState());

	private static final float SNAP_THRESHOLD = 10.0f;
	private static final float SNAP_INDICATOR_SIZE = 5.0f;
	private static final float EDGE_PADDING = 2.0f;

	private static class SnapState {
		Float snapX;
		Float snapY;
		boolean isSnapping;
		boolean hasActiveSnap = false;
		float snapStrength = 0.0f; // 0.0 = no snap, 1.0 = full snap

		void reset() {
			snapX = null;
			snapY = null;
			isSnapping = false;
			hasActiveSnap = false;
			snapStrength = 0.0f;
		}

		void setSnap(Float x, Float y) {
			snapX = x;
			snapY = y;
			isSnapping = (x != null || y != null);
			hasActiveSnap = isSnapping;
		}
	}

	public final BooleanSetting activated;
	private float lastWidth = 0;
	private float lastHeight = 0;
	private Point2D lastAnchorPoint = null;
	private AnchorEdge horizontalAnchor = AnchorEdge.LEFT;
	private AnchorEdge verticalAnchor = AnchorEdge.TOP;

	private static enum AnchorEdge {
		TOP, BOTTOM, LEFT, RIGHT, CENTER
	}

	private static class Point2D {
		float x, y;

		Point2D(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private void scheduleSnapApplication(SnapState snapState) {
		if (!snapState.hasActiveSnap) return;

		hasPendingSnapUpdate = true;

		if (snapState.snapX != null) {
			pendingSnapX = Math.round(snapState.snapX);
			updateSnapX = true;
		} else {
			updateSnapX = false;
		}

		if (snapState.snapY != null) {
			pendingSnapY = Math.round(snapState.snapY);
			updateSnapY = true;
		} else {
			updateSnapY = false;
		}
	}

	private void applyPendingSnapUpdates() {
		if (!hasPendingSnapUpdate) return;

		if (updateSnapX) {
			position.setX(pendingSnapX);
		}
		if (updateSnapY) {
			position.setY(pendingSnapY);
		}

		if (updateSnapX || updateSnapY) {
			float newX = updateSnapX ? pendingSnapX : position.getX().floatValue();
			float newY = updateSnapY ? pendingSnapY : position.getY().floatValue();

			if (lastAnchorPoint != null) {
				lastAnchorPoint.x = newX;
				lastAnchorPoint.y = newY;
			} else {
				lastAnchorPoint = new Point2D(newX, newY);
			}
		}

		hasPendingSnapUpdate = false;
		updateSnapX = false;
		updateSnapY = false;
	}

	private float roundToPixel(float value) {
		return Math.round(value);
	}

	private void ensurePixelAlignment() {
		float currentX = position.getX().floatValue();
		float currentY = position.getY().floatValue();

		float alignedX = roundToPixel(currentX);
		float alignedY = roundToPixel(currentY);

		if (alignedX != currentX || alignedY != currentY) {
			position.setX(alignedX);
			position.setY(alignedY);
		}
	}

	public HudWindow(String ID, float x, float y, float scale) {
		this(ID, x, y, 180.0f, 50f);
	}

	public HudWindow(String ID, float x, float y) {
		this(ID, x, y, 0, 0);
	}

	public HudWindow(String ID, float x, float y, float width, float height) {
		super(ID, x, y, width, height);
		activated = BooleanSetting.builder()
				.id(ID + "_activated")
				.defaultValue(false)
				.onUpdate(this::onActivatedChanged)
				.build();
		SettingManager.registerSetting(activated);
	}

	private void onActivatedChanged(Boolean state) {
		Payload.getInstance().guiManager.setHudActive(this, state);
	}

	@Override
	public boolean isVisible() {
		return activated.getValue();
	}

	@Override
	public void setVisible(boolean state) {
		super.setVisible(state);
		if (!state) {
			isMoving = false;
			currentSnapState.get().reset();
			lastAnchorPoint = null;
			GuiManager guiManager = Payload.getInstance().guiManager;
			if (guiManager.isHudOptionsWindowOpen(getID())) {
				guiManager.hideHudOptionsWindow(getID());
			}
		}
	}

	private void updatePositionOnResize() {
		float screenWidth = mc.getWindow().getWidth();
		float screenHeight = mc.getWindow().getHeight();
		Rectangle pos = getActualSize();
		float currentX = pos.getX().floatValue();
		float currentY = pos.getY().floatValue();
		float width = pos.getWidth().floatValue();
		float height = pos.getHeight().floatValue();

		if (lastAnchorPoint == null) {
			lastAnchorPoint = new Point2D(currentX, currentY);

			float rightDistance = screenWidth - (currentX + lastWidth);
			float leftDistance = currentX;
			float centerDistance = Math.abs((screenWidth / 2) - (currentX + lastWidth / 2));

			if (centerDistance < SNAP_THRESHOLD) {
				horizontalAnchor = AnchorEdge.CENTER;
			} else if (rightDistance < leftDistance) {
				horizontalAnchor = AnchorEdge.RIGHT;
			} else {
				horizontalAnchor = AnchorEdge.LEFT;
			}

			float bottomDistance = screenHeight - (currentY + lastHeight);
			float topDistance = currentY;
			float middleDistance = Math.abs((screenHeight / 2) - (currentY + lastHeight / 2));

			if (middleDistance < SNAP_THRESHOLD) {
				verticalAnchor = AnchorEdge.CENTER;
			} else if (bottomDistance < topDistance) {
				verticalAnchor = AnchorEdge.BOTTOM;
			} else {
				verticalAnchor = AnchorEdge.TOP;
			}
		}

		float newX = currentX;
		float newY = currentY;

		switch (horizontalAnchor) {
			case LEFT:
				newX = lastAnchorPoint.x;
				break;
			case RIGHT:
				newX = screenWidth - width - (screenWidth - lastAnchorPoint.x - lastWidth);
				break;
			case CENTER:
				newX = (screenWidth - width) / 2;
				break;
		}

		switch (verticalAnchor) {
			case TOP:
				newY = lastAnchorPoint.y;
				break;
			case BOTTOM:
				newY = screenHeight - height - (screenHeight - lastAnchorPoint.y - lastHeight);
				break;
			case CENTER:
				newY = (screenHeight - height) / 2;
				break;
		}

		newX = Math.max(EDGE_PADDING, Math.min(screenWidth - width - EDGE_PADDING, newX));
		newY = Math.max(EDGE_PADDING, Math.min(screenHeight - height - EDGE_PADDING, newY));

		if (newX != currentX || newY != currentY) {
			position.setX(newX);
			position.setY(newY);
		}

		lastWidth = width;
		lastHeight = height;
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		if (event.action == MouseAction.DOWN && hovered && event.button == MouseButton.LEFT) {
			lastAnchorPoint = null;
			snapEngaged = false;
			currentSnapState.get().reset();
		}

		if (event.action == MouseAction.UP && event.button == MouseButton.LEFT && isMoving) {
			if (snapEngaged) {
				SnapState snapState = currentSnapState.get();
				applySnapPositionCleanly(snapState);
			}
			snapEngaged = false;
		}

		super.onMouseClick(event);

		if (hovered && event.button == MouseButton.RIGHT && event.action == MouseAction.DOWN) {
			String hudId = getID();
			GuiManager guiManager = Payload.getInstance().guiManager;

			if (guiManager.isHudOptionsWindowOpen(hudId)) {
				guiManager.hideHudOptionsWindow(hudId);
			} else {
				guiManager.showHudOptionsWindow(hudId);
			}
			event.cancel();
		}
	}

	private void calculateSnapPositions(Rectangle currentPos, List<UIElement> elements) {
		SnapState snapState = currentSnapState.get();
		snapState.reset();

		float centerX = mc.getWindow().getWidth() / 2f;
		float centerY = mc.getWindow().getHeight() / 2f;
		float currentX = currentPos.getX().floatValue();
		float currentY = currentPos.getY().floatValue();
		float width = currentPos.getWidth().floatValue();
		float height = currentPos.getHeight().floatValue();

		Float closestSnapX = null;
		Float closestSnapY = null;
		float closestDistanceX = Float.MAX_VALUE;
		float closestDistanceY = Float.MAX_VALUE;

		List<Float> potentialSnapsX = new ArrayList<>();
		List<Float> potentialSnapsY = new ArrayList<>();

		potentialSnapsX.add(EDGE_PADDING);
		potentialSnapsX.add(mc.getWindow().getWidth() - width - EDGE_PADDING);
		potentialSnapsY.add(EDGE_PADDING);
		potentialSnapsY.add(mc.getWindow().getHeight() - height - EDGE_PADDING);

		potentialSnapsX.add(centerX - width / 2);
		potentialSnapsX.add(centerX);
		potentialSnapsX.add(centerX - width);

		potentialSnapsY.add(centerY - height / 2);
		potentialSnapsY.add(centerY);
		potentialSnapsY.add(centerY - height);

		for (UIElement element : elements) {
			if (element == this) continue;

			Rectangle otherPos = element.getActualSize();
			float otherX = otherPos.getX().floatValue();
			float otherY = otherPos.getY().floatValue();
			float otherWidth = otherPos.getWidth().floatValue();
			float otherHeight = otherPos.getHeight().floatValue();

			if (Math.abs(currentY - otherY) < SNAP_THRESHOLD * 3 ||
					Math.abs((currentY + height) - (otherY + otherHeight)) < SNAP_THRESHOLD * 3) {
				potentialSnapsX.add(otherX + otherWidth);
				potentialSnapsX.add(otherX - width);
			}

			if (Math.abs(currentX - otherX) < SNAP_THRESHOLD * 3 ||
					Math.abs((currentX + width) - (otherX + otherWidth)) < SNAP_THRESHOLD * 3) {
				potentialSnapsY.add(otherY + otherHeight);
				potentialSnapsY.add(otherY - height);
			}
		}

		for (Float snapX : potentialSnapsX) {
			float distance = Math.abs(currentX - snapX);
			if (distance < closestDistanceX && distance < SNAP_THRESHOLD) {
				closestDistanceX = distance;
				closestSnapX = snapX;
			}
		}

		for (Float snapY : potentialSnapsY) {
			float distance = Math.abs(currentY - snapY);
			if (distance < closestDistanceY && distance < SNAP_THRESHOLD) {
				closestDistanceY = distance;
				closestSnapY = snapY;
			}
		}

		snapState.setSnap(closestSnapX, closestSnapY);
	}

	private void applySnapOnRelease() {
		SnapState state = currentSnapState.get();
		if (state.isSnapping && (state.snapX != null || state.snapY != null)) {
			if (state.snapX != null) {
				position.setX((float) Math.round(state.snapX));
				lastAnchorPoint = new Point2D(Math.round(state.snapX),
						lastAnchorPoint != null ? lastAnchorPoint.y : position.getY().floatValue());
			}
			if (state.snapY != null) {
				position.setY((float) Math.round(state.snapY));
				lastAnchorPoint = new Point2D(
						lastAnchorPoint != null ? lastAnchorPoint.x : position.getX().floatValue(),
						Math.round(state.snapY));
			}
		}
		state.reset();
	}

	private void calculateAllSnaps(float currentX, float currentY, float width, float height,
								   float centerX, float centerY, List<UIElement> elements, SnapState state) {
		float screenWidth = MC.getWindow().getWidth();
		float screenHeight = MC.getWindow().getHeight();

		if (currentX < SNAP_THRESHOLD + EDGE_PADDING) {
			state.snapX = roundToPixel(EDGE_PADDING);
			state.isSnapping = true;
		} else if (currentX + width > screenWidth - SNAP_THRESHOLD - EDGE_PADDING) {
			state.snapX = roundToPixel(screenWidth - width - EDGE_PADDING);
			state.isSnapping = true;
		}

		if (currentY < SNAP_THRESHOLD + EDGE_PADDING) {
			state.snapY = roundToPixel(EDGE_PADDING);
			state.isSnapping = true;
		} else if (currentY + height > screenHeight - SNAP_THRESHOLD - EDGE_PADDING) {
			state.snapY = roundToPixel(screenHeight - height - EDGE_PADDING);
			state.isSnapping = true;
		}

		float elementCenterX = currentX + width / 2;
		float elementCenterY = currentY + height / 2;

		if (Math.abs(elementCenterX - centerX) < SNAP_THRESHOLD) {
			state.snapX = roundToPixel(centerX - width / 2);
			state.isSnapping = true;
		}

		if (Math.abs(elementCenterY - centerY) < SNAP_THRESHOLD) {
			state.snapY = roundToPixel(centerY - height / 2);
			state.isSnapping = true;
		}

		if (Math.abs(currentY - centerY) < SNAP_THRESHOLD) {
			state.snapY = roundToPixel(centerY);
			state.isSnapping = true;
		}

		if (Math.abs(currentY + height - centerY) < SNAP_THRESHOLD) {
			state.snapY = roundToPixel(centerY - height);
			state.isSnapping = true;
		}

		if (Math.abs(currentX - centerX) < SNAP_THRESHOLD) {
			state.snapX = roundToPixel(centerX);
			state.isSnapping = true;
		}

		if (Math.abs(currentX + width - centerX) < SNAP_THRESHOLD) {
			state.snapX = roundToPixel(centerX - width);
			state.isSnapping = true;
		}

		for (UIElement element : elements) {
			if (element == this) continue;

			Rectangle otherPos = element.getActualSize();
			float otherX = otherPos.getX().floatValue();
			float otherY = otherPos.getY().floatValue();
			float otherWidth = otherPos.getWidth().floatValue();
			float otherHeight = otherPos.getHeight().floatValue();

			calculateElementSnapping(currentX, currentY, width, height,
					otherX, otherY, otherWidth, otherHeight, state);
		}
	}

	private void calculateElementSnapping(float currentX, float currentY, float width, float height,
										  float otherX, float otherY, float otherWidth, float otherHeight,
										  SnapState state) {
		if (Math.abs(currentX - (otherX + otherWidth)) < SNAP_THRESHOLD) {
			if (currentY < otherY + otherHeight + SNAP_THRESHOLD && currentY + height > otherY - SNAP_THRESHOLD) {
				state.snapX = roundToPixel(otherX + otherWidth);
			}
		}
		if (Math.abs((currentX + width) - otherX) < SNAP_THRESHOLD) {
			if (currentY < otherY + otherHeight + SNAP_THRESHOLD && currentY + height > otherY - SNAP_THRESHOLD) {
				state.snapX = roundToPixel(otherX - width);
			}
		}

		if (Math.abs(currentY - (otherY + otherHeight)) < SNAP_THRESHOLD) {
			if (currentX < otherX + otherWidth + SNAP_THRESHOLD && currentX + width > otherX - SNAP_THRESHOLD) {
				state.snapY = roundToPixel(otherY + otherHeight);
			}
		}
		if (Math.abs((currentY + height) - otherY) < SNAP_THRESHOLD) {
			if (currentX < otherX + otherWidth + SNAP_THRESHOLD && currentX + width > otherX - SNAP_THRESHOLD) {
				state.snapY = roundToPixel(otherY - height);
			}
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		if (!isVisible()) return;

		for (UIElement child : getChildren()) {
			child.draw(drawContext, partialTicks);
		}

		Rectangle pos = getActualSize();
		if (!pos.isDrawable()) return;

		float currentWidth = pos.getWidth().floatValue();
		float currentHeight = pos.getHeight().floatValue();

		if (currentWidth != lastWidth || currentHeight != lastHeight) {
			updatePositionOnResize();
			lastWidth = currentWidth;
			lastHeight = currentHeight;
		}

		// Only handle snapping logic, don't force updates during rendering
		if (isMoving) {
			handleRealTimeSnapping(pos, Payload.getInstance().guiManager.getAllVisibleElements());
		}

		Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
		drawHudElement(drawContext, pos);

		if (Payload.getInstance().guiManager.isClickGuiOpen()) {
			drawGuideSystem(matrix);
		}
	}

	private void handleRealTimeSnapping(Rectangle currentPos, List<UIElement> elements) {
		SnapState snapState = currentSnapState.get();

		float currentX = currentPos.getX().floatValue();
		float currentY = currentPos.getY().floatValue();
		float width = currentPos.getWidth().floatValue();
		float height = currentPos.getHeight().floatValue();

		calculateSnapPositions(currentPos, elements);

		boolean shouldBeSnapped = false;

		if (snapState.snapX != null) {
			float distanceToSnapX = Math.abs(currentX - snapState.snapX);
			if (snapEngaged) {
				shouldBeSnapped = distanceToSnapX < SNAP_RELEASE_THRESHOLD;
			} else {
				shouldBeSnapped = distanceToSnapX < SNAP_ENGAGEMENT_THRESHOLD;
			}
		}

		if (snapState.snapY != null && !shouldBeSnapped) {
			float distanceToSnapY = Math.abs(currentY - snapState.snapY);
			if (snapEngaged) {
				shouldBeSnapped = distanceToSnapY < SNAP_RELEASE_THRESHOLD;
			} else {
				shouldBeSnapped = distanceToSnapY < SNAP_ENGAGEMENT_THRESHOLD;
			}
		}

		if (shouldBeSnapped && snapState.hasActiveSnap) {
			if (!snapEngaged) {
				snapEngaged = true;
			}
			applySnapPositionCleanly(snapState);
		} else {
			if (snapEngaged) {
				snapEngaged = false;
				snapState.reset();
			}
		}
	}

	private void applySnapPositionCleanly(SnapState snapState) {
		if (!snapState.hasActiveSnap) return;

		float currentX = position.getX().floatValue();
		float currentY = position.getY().floatValue();

		boolean positionChanged = false;

		if (snapState.snapX != null) {
			float snapX = Math.round(snapState.snapX);
			if (Math.abs(currentX - snapX) > 0.1f) {
				position.setX(snapX);
				positionChanged = true;
			}
		}

		if (snapState.snapY != null) {
			float snapY = Math.round(snapState.snapY);
			if (Math.abs(currentY - snapY) > 0.1f) {
				position.setY(snapY);
				positionChanged = true;
			}
		}

		if (positionChanged) {
			float newX = position.getX().floatValue();
			float newY = position.getY().floatValue();
			lastAnchorPoint = new Point2D(newX, newY);
		}
	}

	private void applySnapPosition(SnapState snapState) {
		if (!snapState.hasActiveSnap) return;

		float newX = position.getX().floatValue();
		float newY = position.getY().floatValue();

		if (snapState.snapX != null) {
			newX = Math.round(snapState.snapX);
		}
		if (snapState.snapY != null) {
			newY = Math.round(snapState.snapY);
		}

		position.setX(newX);
		position.setY(newY);

		if (lastAnchorPoint != null) {
			lastAnchorPoint.x = newX;
			lastAnchorPoint.y = newY;
		} else {
			lastAnchorPoint = new Point2D(newX, newY);
		}
	}

	private void drawHudElement(DrawContext drawContext, Rectangle pos) {
		float x = Math.round(pos.getX().floatValue());
		float y = Math.round(pos.getY().floatValue());
		float width = Math.round(pos.getWidth().floatValue());
		float height = Math.round(pos.getHeight().floatValue());

		Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();

		if (isMoving) {
			switch (GuiManager.guistyle.getValue()) {
				case Atsu:
					Render2D.drawBox(matrix, x, y, width, height, DRAG_COLOR);
					break;
				case Rias:
					Render2D.drawBox(matrix, x, y, width, height, new Color(210, 41, 44, 50));
					break;
				case Captain:
					Render2D.drawBox(matrix, x, y, width, height, new Color(210, 41, 44, 50));
					break;
			}
		}

		if (Payload.getInstance().guiManager.isClickGuiOpen()) {
			switch (GuiManager.guistyle.getValue()) {
				case Atsu:
					Render2D.drawBoxOutline(matrix, x, y, width, height, DRAG_COLOR);
					break;
				case Rias:
					Render2D.drawBoxOutline(matrix, x, y, width, height, new Color(210, 41, 44, 50));
					break;
				case Captain:
					Render2D.drawBoxOutline(matrix, x, y, width, height, new Color(147, 90, 160, 50));
					break;
			}
		}
	}

	private void drawGuideSystem(Matrix4f matrix) {
		float windowWidth = mc.getWindow().getWidth();
		float windowHeight = mc.getWindow().getHeight();
		float centerX = windowWidth / 2f;
		float centerY = windowHeight / 2f;

		Render2D.drawLine(matrix, centerX, 0, centerX, windowHeight, GUIDE_LINE_COLOR);
		Render2D.drawLine(matrix, 0, centerY, windowWidth, centerY, GUIDE_LINE_COLOR);
		Render2D.drawBoxOutline(matrix, 0, 0, windowWidth, windowHeight, BORDER_COLOR);

		SnapState state = currentSnapState.get();
		if (state.hasActiveSnap && snapEngaged) {
			Rectangle pos = getActualSize();
			float x = pos.getX().floatValue();
			float y = pos.getY().floatValue();

			Color snapColor = new Color(0, 255, 0, 200); // Green for active snap

			if (state.snapX != null) {
				Render2D.drawLine(matrix, x, 0, x, windowHeight, snapColor);
			}

			if (state.snapY != null) {
				Render2D.drawLine(matrix, 0, y, windowWidth, y, snapColor);
			}

			Render2D.drawBox(matrix,
					x - SNAP_INDICATOR_SIZE,
					y - SNAP_INDICATOR_SIZE,
					SNAP_INDICATOR_SIZE * 2,
					SNAP_INDICATOR_SIZE * 2,
					snapColor);
		}
	}

	private void applySnapping() {
		SnapState state = currentSnapState.get();
	}

	public void reset() {
		currentSnapState.get().reset();
		isMoving = false;
		snapEngaged = false;
		hasPendingSnapUpdate = false;
		updateSnapX = false;
		updateSnapY = false;
		lastAnchorPoint = null;
		horizontalAnchor = AnchorEdge.LEFT;
		verticalAnchor = AnchorEdge.TOP;
		lastWidth = 0;
		lastHeight = 0;
	}
}