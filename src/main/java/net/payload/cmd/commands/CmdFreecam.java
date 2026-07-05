

package net.payload.cmd.commands;

import net.payload.Payload;
import net.payload.cmd.Command;
import net.payload.cmd.CommandManager;
import net.payload.cmd.InvalidSyntaxException;
import net.payload.module.modules.client.Freecam;

public class CmdFreecam extends Command {

	public CmdFreecam() {
		super("freecam", "Disables fall damage for the player", "[toggle] [value]");
	}

	@Override
	public void runCommand(String[] parameters) throws InvalidSyntaxException {
		if (parameters.length != 2)
			throw new InvalidSyntaxException(this);

		Freecam module = (Freecam) Payload.getInstance().moduleManager.freecam;

		switch (parameters[0]) {
		case "toggle":
			String state = parameters[1].toLowerCase();
			if (state.equals("on")) {
				module.state.setValue(true);
				CommandManager.sendChatMessage("Freecam toggled ON");
			} else if (state.equals("off")) {
				module.state.setValue(false);
				CommandManager.sendChatMessage("Freecam toggled OFF");
			} else {
				CommandManager.sendChatMessage("Invalid value. [ON/OFF]");
			}
			break;
		default:
			throw new InvalidSyntaxException(this);
		}
	}

	@Override
	public String[] getAutocorrect(String previousParameter) {
		switch (previousParameter) {
		case "toggle":
			return new String[] { "on", "off" };
		default:
			return new String[] { "toggle" };
		}
	}

}
