package net.payload.cmd.commands;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.payload.Payload;
import net.payload.cmd.Command;
import net.payload.cmd.CommandManager;
import net.payload.cmd.InvalidSyntaxException;
import net.payload.settings.friends.Friend;
import net.payload.settings.friends.FriendsList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.Optional;

public class CmdFriends extends Command {

    public CmdFriends() {
        super("friends", "Allows the player to add and remove friends (Who will be excluded from many hacks)", "[add/remove/list] [value]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        FriendsList friendsList = Payload.getInstance().friendsList;
        MinecraftClient MC = MinecraftClient.getInstance();
        switch (parameters[0]) {
            case "add": {
                if (parameters.length < 2) throw new InvalidSyntaxException(this);

                String playerName = parameters[1];
                if (mc.player == null || mc.getNetworkHandler() == null) {
                    CommandManager.sendChatMessage("Error: Not connected to a server.");
                    return;
                }

                Optional<PlayerListEntry> entryOpt = mc.getNetworkHandler().getPlayerList().stream()
                        .filter(e -> e.getProfile().getName().equalsIgnoreCase(playerName))
                        .findFirst();

                if (entryOpt.isPresent()) {
                    PlayerListEntry entry = entryOpt.get();
                    friendsList.addFriend(entry.getProfile().getName(), entry.getProfile().getId());
                    CommandManager.sendChatMessage("Player " + entry.getProfile().getName() + " was added to the friends list.");
                } else {
                    CommandManager.sendChatMessage("Player " + playerName + " could not be found.");
                }
            }
            break;
            case "remove": {
                if (parameters.length < 2) throw new InvalidSyntaxException(this);

                String playerName = parameters[1];
                if (mc.player == null || mc.getNetworkHandler() == null) {
                    CommandManager.sendChatMessage("Error: Not connected to a server.");
                    return;
                }

                Optional<PlayerListEntry> entryOpt = mc.getNetworkHandler().getPlayerList().stream()
                        .filter(e -> e.getProfile().getName().equalsIgnoreCase(playerName))
                        .findFirst();

                if (entryOpt.isPresent()) {
                    friendsList.removeFriend(entryOpt.get().getProfile().getId());
                    CommandManager.sendChatMessage("Player " + playerName + " was removed from the friends list.");
                } else if (friendsList.contains(playerName)) {
                    friendsList.removeFriend(playerName);
                    CommandManager.sendChatMessage("Player " + playerName + " was removed from the friends list.");
                } else {
                    CommandManager.sendChatMessage("Player " + playerName + " could not be found.");
                }
            }
            break;
            case "list": {
                StringBuilder friends = new StringBuilder("Friends: ");
                for (Friend friend : friendsList.getFriends()) {
                    friends.append(friend.getUsername()).append(", ");
                }
                friends.substring(0, friends.length() - 2);
                CommandManager.sendChatMessage(friends.toString());
                break;
            }
            case "clear": {
                Payload.getInstance().friendsList.clear();
                CommandManager.sendChatMessage("Cleared friends list");
                break;
            }
        }
    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        switch (previousParameter) {
            case "add": {
                if (mc.player != null && mc.getNetworkHandler() != null) {
                    return mc.getNetworkHandler().getPlayerList().stream()
                            .map(entry -> entry.getProfile().getName())
                            .toArray(String[]::new);
                } else {
                    return new String[]{"Error"};
                }
            }
            case "remove":
                return Payload.getInstance().friendsList.getFriendNames();
            default:
                return new String[]{"add", "remove", "list"};
        }
    }
}
