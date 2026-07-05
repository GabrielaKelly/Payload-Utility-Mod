package net.payload.utils.discord;

import net.payload.PayloadClient;
import net.payload.gui.screens.*;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import static net.payload.PayloadClient.MC;

public class RPCManager {
    public static boolean started;
    private static final Discord rpc = Discord.INSTANCE;
    public static DiscordRPC presence = new DiscordRPC();
    private static Thread thread;

    public void startRpc() {
        if (!started) {
            started = true;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize("1268367396134191136", handlers, true, "");
            presence.startTimestamp = (System.currentTimeMillis() / 1000L);presence.largeImageText = "v" + PayloadClient.PAYLOAD_VERSION;
            rpc.Discord_UpdatePresence(presence);
            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();
                    presence.details = "";presence.state = "Payload" + PayloadClient.PAYLOAD_VERSION + " | MC 1.21";presence.smallImageText = "";
                    rpc.Discord_UpdatePresence(presence);
                    try {Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                    }}}, "TH-RPC-Handler");thread.start();
        }
    }


    private String getDetails() {
        String result = "";

        if (MC.currentScreen instanceof TitleScreen || MC.currentScreen instanceof MainMenuScreen) {
            result = "On the Menu";


        } else if (MC.currentScreen instanceof MultiplayerScreen || MC.currentScreen instanceof AddServerScreen) {
            result = "Inputting Server Info";


        } else if (MC.getCurrentServerEntry() != null) {
            result = "Playing Multiplayer";


        } else if (MC.isInSingleplayer()) {
            result = "Playing Singleplayer";


        }
        return result;
    }
}
