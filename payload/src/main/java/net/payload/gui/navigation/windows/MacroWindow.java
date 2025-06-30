package net.payload.gui.navigation.windows;

import net.payload.Payload;
import net.payload.PayloadClient;
import net.payload.gui.components.ButtonComponent;
import net.payload.gui.components.ItemsComponent;
import net.payload.gui.components.SeparatorComponent;
import net.payload.gui.components.StackPanelComponent;
import net.payload.gui.components.StringComponent;
import net.payload.gui.components.TextBoxComponent;
import net.payload.gui.navigation.Window;
import net.payload.macros.Macro;

public class MacroWindow extends Window {
	private ButtonComponent startButton;
	private StringComponent startButtonText;
	private ButtonComponent replayButton;
	private StringComponent replayButtonText;

	private TextBoxComponent filenameText;
	private ItemsComponent<Macro> macrosList;
	private ButtonComponent saveButton;

	private Runnable startRunnable;
	private Runnable endRunnable;
	private Runnable replayRunnable;

	public MacroWindow() {
		super("Macro", 895, 150);

		this.minWidth = 350f;

		StackPanelComponent stackPanel = new StackPanelComponent();

		stackPanel.addChild(new StringComponent("Macros"));
		stackPanel.addChild(new SeparatorComponent());

		StringComponent label = new StringComponent("Records your inputs and plays them back.");
		stackPanel.addChild(label);

		startRunnable = new Runnable() {
			@Override
			public void run() {
				Payload.getInstance().guiManager.setClickGuiOpen(false);
				Payload.getInstance().macroManager.getRecorder().startRecording();
				startButtonText.setText("Stop Recording");
				startButton.setOnClick(endRunnable);
			}
		};

		endRunnable = new Runnable() {
			@Override
			public void run() {
				Payload.getInstance().macroManager.getRecorder().stopRecording();
				startButtonText.setText("Record");
				startButton.setOnClick(startRunnable);
			}
		};
		startButton = new ButtonComponent(startRunnable);
		startButtonText = new StringComponent("Record");
		startButton.addChild(startButtonText);
		stackPanel.addChild(startButton);

		replayRunnable = new Runnable() {
			@Override
			public void run() {
				PayloadClient payload = Payload.getInstance();
				payload.macroManager.getPlayer().play(payload.macroManager.getCurrentlySelected());
			}
		};
		replayButton = new ButtonComponent(replayRunnable);
		replayButtonText = new StringComponent("Replay");
		replayButton.addChild(replayButtonText);
		stackPanel.addChild(replayButton);
		
		addChild(stackPanel);
	}
}