package net.payload.macros;

import java.util.LinkedList;

import net.payload.Payload;
import net.payload.event.events.KeyDownEvent;
import net.payload.event.events.KeyUpEvent;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.events.MouseMoveEvent;
import net.payload.event.events.MouseScrollEvent;
import net.payload.event.listeners.KeyDownListener;
import net.payload.event.listeners.KeyUpListener;
import net.payload.event.listeners.MouseClickListener;
import net.payload.event.listeners.MouseMoveListener;
import net.payload.event.listeners.MouseScrollListener;
import net.payload.macros.actions.KeyClickMacroEvent;
import net.payload.macros.actions.MacroEvent;
import net.payload.macros.actions.MouseClickMacroEvent;
import net.payload.macros.actions.MouseMoveMacroEvent;
import net.payload.macros.actions.MouseScrollMacroEvent;

public class MacroRecorder
		implements MouseClickListener, MouseMoveListener, MouseScrollListener, KeyDownListener, KeyUpListener {

	private LinkedList<MacroEvent> currentMacro = new LinkedList<MacroEvent>();
	private long startTime = 0;
	private boolean recording = false;

	public void startRecording() {
		if (!recording) {
			currentMacro = new LinkedList<MacroEvent>();
			recording = true;
			startTime = System.nanoTime();

			Payload.getInstance().eventManager.AddListener(MouseClickListener.class, this);
			Payload.getInstance().eventManager.AddListener(MouseMoveListener.class, this);
			Payload.getInstance().eventManager.AddListener(MouseScrollListener.class, this);
			Payload.getInstance().eventManager.AddListener(KeyDownListener.class, this);
			Payload.getInstance().eventManager.AddListener(KeyUpListener.class, this);
		}
	}

	public void stopRecording() {
		if (recording) {
			recording = false;
			startTime = 0;

			Payload.getInstance().eventManager.RemoveListener(MouseClickListener.class, this);
			Payload.getInstance().eventManager.RemoveListener(MouseMoveListener.class, this);
			Payload.getInstance().eventManager.RemoveListener(MouseScrollListener.class, this);
			Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
			Payload.getInstance().eventManager.RemoveListener(KeyUpListener.class, this);

			addToMacroManager();
		}
	}

	public void addToMacroManager() {
		if (!recording && currentMacro != null) {
			Macro macro = new Macro(currentMacro);
			Payload.getInstance().macroManager.addMacro(macro);
			Payload.getInstance().macroManager.setCurrentlySelected(macro);
			currentMacro = null;
		}
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		if (event.GetKey() != Payload.getInstance().guiManager.clickGuiButton.getValue().getCode()
				&& !Payload.getInstance().guiManager.isClickGuiOpen()) {
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new KeyClickMacroEvent(timeStamp, event.GetKey(), event.GetScanCode(), event.GetAction(),
					event.GetModifiers()));
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (event.GetKey() != Payload.getInstance().guiManager.clickGuiButton.getValue().getCode()
				&& !Payload.getInstance().guiManager.isClickGuiOpen()) {
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new KeyClickMacroEvent(timeStamp, event.GetKey(), event.GetScanCode(), event.GetAction(),
					event.GetModifiers()));
		}
	}

	@Override
	public void onMouseScroll(MouseScrollEvent event) {
		if (!Payload.getInstance().guiManager.isClickGuiOpen()) {
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new MouseScrollMacroEvent(timeStamp, event.GetHorizontal(), event.GetVertical()));
		}
	}

	@Override
	public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
		if (!Payload.getInstance().guiManager.isClickGuiOpen()) {
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new MouseMoveMacroEvent(timeStamp, mouseMoveEvent.getX(), mouseMoveEvent.getY()));
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent mouseClickEvent) {
		if (!Payload.getInstance().guiManager.isClickGuiOpen()) {
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new MouseClickMacroEvent(timeStamp, mouseClickEvent.button, mouseClickEvent.action,
					mouseClickEvent.mods));
		}
	}
}
