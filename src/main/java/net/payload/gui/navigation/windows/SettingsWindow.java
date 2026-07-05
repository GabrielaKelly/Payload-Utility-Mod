

package net.payload.gui.navigation.windows;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.payload.gui.GridDefinition;
import net.payload.gui.GridDefinition.RelativeUnit;
import net.payload.gui.Margin;
import net.payload.gui.TextAlign;
import net.payload.gui.components.ButtonComponent;
import net.payload.gui.components.GridComponent;
import net.payload.gui.components.ListComponent;
import net.payload.gui.components.SeparatorComponent;
import net.payload.gui.components.StackPanelComponent;
import net.payload.gui.components.StringComponent;
import net.payload.gui.components.TextBoxComponent;
import net.payload.gui.navigation.Window;
import net.payload.settings.SettingManager;

/**
 * Represents the Settings window that contains a list of all of the available
 * toggle-able settings.
 */
public class SettingsWindow extends Window {
	private TextBoxComponent fileName;
	private ListComponent configNames;

	public SettingsWindow() {
		super("Settings", 50, 770);

		StackPanelComponent stackPanel = new StackPanelComponent();

		stackPanel.addChild(new StringComponent("Configuration"));
		stackPanel.addChild(new SeparatorComponent());



		GridComponent loadConfigGrid = new GridComponent();
		loadConfigGrid.addColumnDefinition(new GridDefinition(1, RelativeUnit.Relative));
		loadConfigGrid.addColumnDefinition(new GridDefinition(100, RelativeUnit.Absolute));

		configNames = new ListComponent(SettingManager.configNames);
		configNames.setMargin(new Margin(2f, 4f, 2f, 4f));
		loadConfigGrid.addChild(configNames);

		ButtonComponent btnLoadConfig = new ButtonComponent(new Runnable() {
			@Override
			public void run() {
				SettingManager.setCurrentConfig(configNames.getSelectedItem());
			}
		});

		StringComponent buttonLoadString = new StringComponent("Load");
		buttonLoadString.setTextAlign(TextAlign.Center);
		btnLoadConfig.addChild(buttonLoadString);

		loadConfigGrid.addChild(btnLoadConfig);
		stackPanel.addChild(loadConfigGrid);

		GridComponent fileSaveGrid = new GridComponent();
		fileSaveGrid.addColumnDefinition(new GridDefinition(1, RelativeUnit.Relative));
		fileSaveGrid.addColumnDefinition(new GridDefinition(100, RelativeUnit.Absolute));

		fileName = new TextBoxComponent();
		fileName.setMargin(new Margin(8f, 4f, 0f, 4f));
		fileSaveGrid.addChild(fileName);

		ButtonComponent btnSaveACopy = new ButtonComponent(new Runnable() {
			@Override
			public void run() {
				String newFileName = fileName.getText();
				if (!newFileName.isBlank()) {
					try {
						SettingManager.saveCopy(newFileName);
						SettingManager.refreshSettingFiles();
						configNames.setItemsSource(SettingManager.configNames);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						fileName.setText("");
					}
				}
			}
		});

		StringComponent buttonStr = new StringComponent("Save");
		buttonStr.setTextAlign(TextAlign.Center);
		btnSaveACopy.addChild(buttonStr);
		fileSaveGrid.addChild(btnSaveACopy);
		stackPanel.addChild(fileSaveGrid);
		addChild(stackPanel);
		stackPanel.addChild(new SeparatorComponent());

		// Adding each config name as a StringComponent to the StackPanel
		for (String configName : SettingManager.configNames) {
			// Create a new StringComponent with gray color and default (non-bold) styling
			StringComponent configString = new StringComponent(configName, false, true);  // 'false' for non-bold, 'true' for gray
			stackPanel.addChild(configString);
		}

		setMinWidth(250.0f);
		setMinHeight(400.0f);
	}
}
