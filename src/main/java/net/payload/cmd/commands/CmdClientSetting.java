package net.payload.cmd.commands;

public class CmdClientSetting {
    public static void SettingManager() {
        try {
            Class.forName("net.payload.settings.SettingManager");
        } catch (ClassNotFoundException e) {
        }
    }
}
