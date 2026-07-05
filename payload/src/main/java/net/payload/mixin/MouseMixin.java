

package net.payload.mixin;

import net.payload.Payload;
import net.payload.PayloadClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.payload.event.events.MouseClickEvent;
import net.payload.event.events.MouseMoveEvent;
import net.payload.event.events.MouseScrollEvent;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public class MouseMixin {
	@Shadow
	private double x;
	@Shadow
	private double y;

	@Inject(at = { @At("HEAD") }, method = { "onMouseButton(JIII)V" }, cancellable = true)
	private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		PayloadClient payload = Payload.getInstance();
		if (payload != null && payload.eventManager != null) {
			MouseClickEvent event = new MouseClickEvent(x, y, button, action, mods);
			payload.eventManager.Fire(event);

			if (payload.guiManager.isClickGuiOpen()) {
				ci.cancel();
			}
		}
	}

	@Inject(at = { @At("HEAD") }, method = { "onMouseScroll(JDD)V" }, cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		PayloadClient payload = Payload.getInstance();
		if (payload != null && payload.eventManager != null) {
			MouseScrollEvent event = new MouseScrollEvent(horizontal, vertical);
			payload.eventManager.Fire(event);

			if (event.isCancelled()) {
				ci.cancel();
			}
		}
	}

	@Inject(at = { @At("HEAD") }, method = { "lockCursor()V" }, cancellable = true)
	private void onLockCursor(CallbackInfo ci) {
		PayloadClient payload = Payload.getInstance();
		if (payload != null && payload.guiManager != null) {
			if (payload.guiManager.isClickGuiOpen())
				ci.cancel();
		}
	}

	@Inject(at = { @At("HEAD") }, method = { "onCursorPos(JDD)V" }, cancellable = true)
	private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
		PayloadClient payload = Payload.getInstance();
		if (payload != null && payload.eventManager != null) {
			double cursorDeltaX = x - this.x;
			double cursorDeltaY = y - this.y;

			MouseMoveEvent event = new MouseMoveEvent(x, y, cursorDeltaX, cursorDeltaY);
			payload.eventManager.Fire(event);

			if (event.isCancelled()) {
				// Update the XY but not the delta (used for camera movements);
				this.x = x;
				this.y = y;
				ci.cancel();
			}

		}
	}
}