package net.payload.gui.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.events.MouseMoveEvent;
import net.payload.event.events.MouseScrollEvent;
import net.payload.event.listeners.MouseClickListener;
import net.payload.event.listeners.MouseMoveListener;
import net.payload.event.listeners.MouseScrollListener;

public class Page implements MouseMoveListener, MouseClickListener, MouseScrollListener {
	private final String title;
	private final List<Window> tabs = Collections.synchronizedList(new ArrayList<>());
	private boolean isVisible;

	public Page(String title) {
		this.title = title;
	}

	public void initialize() {
		synchronized (tabs) {
			tabs.forEach(Window::initialize);
		}
	}

	public String getTitle() {
		return this.title;
	}

	public List<Window> getWindows() {
		synchronized (tabs) {
			return new ArrayList<>(tabs);
		}
	}

	public void addWindow(Window hud) {
		synchronized (tabs) {
			hud.parentPage = this;
			hud.setVisible(this.isVisible);
			tabs.add(hud);

			if (hud.isInitialized()) {
				hud.invalidateMeasure();
			} else {
				hud.initialize();
			}
		}
	}

	public void removeWindow(Window hud) {
		synchronized (tabs) {
			hud.parentPage = null;
			tabs.remove(hud);
		}
	}

	public void setVisible(boolean state) {
		this.isVisible = state;

		List<Window> currentTabs;
		synchronized (tabs) {
			currentTabs = new ArrayList<>(tabs);
		}

		currentTabs.forEach(window -> window.setVisible(state));

		if (state) {
			registerEventListeners();
		} else {
			unregisterEventListeners();
		}
	}

	private void registerEventListeners() {
		Payload.getInstance().eventManager.AddListener(MouseMoveListener.class, this);
		Payload.getInstance().eventManager.AddListener(MouseClickListener.class, this);
		Payload.getInstance().eventManager.AddListener(MouseScrollListener.class, this);
	}

	private void unregisterEventListeners() {
		Payload.getInstance().eventManager.RemoveListener(MouseMoveListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(MouseClickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(MouseScrollListener.class, this);
	}

	public void update() {
		if (this.isVisible) {
			synchronized (tabs) {
				new ArrayList<>(tabs).forEach(Window::update);
			}
		}
	}

	public void render(DrawContext drawContext, float partialTicks) {
		if (this.isVisible) {
			synchronized (tabs) {
				tabs.forEach(window -> window.draw(drawContext, partialTicks));
				tabs.stream()
						.filter(window -> !(window instanceof HudWindow))
						.forEach(window -> window.draw(drawContext, partialTicks));
				tabs.stream()
						.filter(window -> window instanceof HudWindow)
						.forEach(window -> window.draw(drawContext, partialTicks));
			}
		}
	}

	public void moveToFront(Window window) {
		synchronized (tabs) {
			if (tabs.size() > 1 && tabs.contains(window)) {
				int currentIndex = tabs.indexOf(window);
				int lastIndex = tabs.size() - 1;
				Collections.swap(tabs, currentIndex, lastIndex);
			}
		}
	}

	@Override
	public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
		if (shouldHandleEvents()) {
			synchronized (tabs) {
				List<Window> reversed = new ArrayList<>(tabs);
				Collections.reverse(reversed);
				reversed.forEach(s -> s.onMouseMove(mouseMoveEvent));
			}
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent mouseClickEvent) {
		if (shouldHandleEvents()) {
			synchronized (tabs) {
				List<Window> reversed = new ArrayList<>(tabs);
				Collections.reverse(reversed);
				reversed.forEach(s -> s.onMouseClick(mouseClickEvent));
			}
		}
	}

	@Override
	public void onMouseScroll(MouseScrollEvent event) {
		if (shouldHandleEvents()) {
			synchronized (tabs) {
				List<Window> reversed = new ArrayList<>(tabs);
				Collections.reverse(reversed);
				reversed.forEach(s -> s.onMouseScroll(event));
			}
		}
	}

	private boolean shouldHandleEvents() {
		return Payload.getInstance().guiManager.isClickGuiOpen();
	}
}