

package net.payload.cmd.commands;

import net.payload.cmd.Command;
import net.payload.cmd.InvalidSyntaxException;

public class CmdSpam extends Command {

    public CmdSpam() {
        super("spam", "Spams the chat with a certain message.", "[times] [message]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        if (parameters.length < 2)
            throw new InvalidSyntaxException(this);

        // Combines the "parameters" into a string to be printed.
        StringBuilder message = new StringBuilder();
        for (int msg = 1; msg < parameters.length; msg++) {
            message.append(parameters[msg]).append(" ");
        }

        // Prints out that message X number of times.
        for (int i = 0; i < Integer.parseInt(parameters[0]); i++) {
            mc.player.networkHandler.sendChatMessage(message.toString());
        }

    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        switch (previousParameter) {
            default:
                return new String[]{"[payload.technology] Did you know? Garry's Mod has had 13 iterations."};
        }
    }
}
