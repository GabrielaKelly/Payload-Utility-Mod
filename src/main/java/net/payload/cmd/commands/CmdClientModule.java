package net.payload.cmd.commands;

public class CmdClientModule {
    public static void ModuleManagerStart() {
        try {
            Class.forName("net.payload.ModuleManager");
        } catch (ClassNotFoundException e) {
        }
    }
}
