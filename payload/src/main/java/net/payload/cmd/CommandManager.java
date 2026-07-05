

/**
 * A class to represent a system to manage Commands.
 */
package net.payload.cmd;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.logging.LogUtils;

import net.payload.api.IAddon;
import net.payload.cmd.commands.*;
import net.payload.settings.SettingManager;
import net.payload.settings.types.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandManager {
	private final Map<String, Command> commands = new HashMap<>();
	private final List<String> commandHistory = new ArrayList<>();

	public static StringSetting PREFIX = StringSetting.builder().id("payload_prefix").displayName("Prefix")
			.defaultValue(".payload").build();

	// Commands
	public final CmdClickgui clickgui = new CmdClickgui();
	public final CmdFreecam freecam = new CmdFreecam();
	public final CmdFont font = new CmdFont();
	public final CmdFriends friends = new CmdFriends();
	public final CmdHelp help = new CmdHelp();
	public final CmdTP tp = new CmdTP();
	public final CmdXRay xray = new CmdXRay();
	public final CmdHistory history = new CmdHistory();

	public CommandManager(List<IAddon> addons) {
		SettingManager.registerSetting(PREFIX);

		for (Field field : getClass().getDeclaredFields()) {
			if (Command.class.isAssignableFrom(field.getType())) {
				try {
					Command cmd = (Command) field.get(this);
					commands.put(cmd.getName(), cmd);
				} catch (IllegalAccessException e) {
					LogUtils.getLogger().error("Error initializing Payload commands: " + e.getMessage());
				}
			}
		}

		addons.forEach(addon -> addon.commands().forEach(command -> {
			if (!commands.containsKey(command.getName())) {
				commands.put(command.getName(), command);
			} else {
				LogUtils.getLogger().warn("Warning: Duplicate command name \"" + command.getName()
						+ "\" from addon. This command will not be registered.");
			}
		}));
	}

	/** Gets the command object from a syntax. */
	public Command getCommandBySyntax(String syntax) {
		return commands.get(syntax);
	}

	/**
	 * Gets all of the Commands currently registered, including ones registered by
	 * addons.
	 */
	public Map<String, Command> getCommands() {
		return commands;
	}

	/**
	 * Gets the total number of registered Commands, including ones registered by
	 * addons
	 */
	public int getNumOfCommands() {
		return commands.size();
	}

	/** Runs a command. */
	public void command(String[] commandIn) {
		try {
			commandHistory.add(String.join(" ", commandIn));

			Command command = commands.get(commandIn[1]);
			if (command == null) {
				sendChatMessage("Invalid Command! Type " + Formatting.LIGHT_PURPLE + ".payload help" + Formatting.RESET
						+ " for a list of commands.");
			} else {
				String[] parameterList = Arrays.copyOfRange(commandIn, 2, commandIn.length);
				command.runCommand(parameterList);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			sendChatMessage("Invalid Command! Type " + Formatting.LIGHT_PURPLE + ".payload help" + Formatting.RESET
					+ " for a list of commands.");
		} catch (InvalidSyntaxException e) {
			e.PrintToChat();
		}
	}

	/** Returns the command history. */
	public List<String> getCommandHistory() {
		return commandHistory;
	}

	/** Prints a message into the Minecraft Chat. */
	public static void sendChatMessage(String message) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.inGameHud != null) {
			mc.inGameHud.getChatHud().addMessage(Text.of(Formatting.DARK_PURPLE + "[" + Formatting.LIGHT_PURPLE + "Payload"
					+ Formatting.DARK_PURPLE + "] " + Formatting.RESET + message));
		}
	}
}
