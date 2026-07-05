
package net.payload.module.modules.client;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

public class Notifications extends Module {

    private BooleanSetting settingsModule = BooleanSetting.builder()
            .id("notifications_modules")
            .displayName("Modules")
            .defaultValue(true)
            .build();

    public Notifications() {
        super("Notifications");
        this.setCategory(Category.of("Client"));
        this.setDescription("Many useful features in chat");

        this.addSetting(settingsModule);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onToggle() {

    }

    public void moduleToggle(String message) {
        if (nullCheck()) return;

        if (this.state.getValue() && settingsModule.getValue()) {
            sendChatMessage(message);
        }
    }
}