

package net.payload.event.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.render.Camera;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.Render3DListener;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent extends AbstractEvent {
	MatrixStack matrices;
	Frustum frustum;
	RenderTickCounter renderTickCounter;
	Camera camera;

	public MatrixStack GetMatrix() {
		return matrices;
	}

	public RenderTickCounter getRenderTickCounter() {
		return renderTickCounter;
	}

	public Frustum getFrustum() {
		return frustum;
	}

	public Camera getCamera() {
		return camera;
	}

	public Render3DEvent(MatrixStack matrix4f, Frustum frustum, Camera camera, RenderTickCounter renderTickCounter) {
		this.matrices = matrix4f;
		this.renderTickCounter = renderTickCounter;
		this.frustum = frustum;
		this.camera = camera;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : List.copyOf(listeners)) {
			Render3DListener renderListener = (Render3DListener) listener;
			renderListener.onRender(this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Render3DListener> GetListenerClassType() {
		return Render3DListener.class;
	}
}