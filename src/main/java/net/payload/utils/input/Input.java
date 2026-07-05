package net.payload.utils.input;

import static net.payload.PayloadClient.MC;

import org.lwjgl.glfw.GLFW;

public class Input {
	private static CursorStyle lastCursorStyle = CursorStyle.Default;

	public static void setCursorStyle(CursorStyle style) {

		if (lastCursorStyle != style) {
			GLFW.glfwSetCursor(MC.getWindow().getHandle(), style.getGlfwCursor());
			lastCursorStyle = style;
		}
	}
}
