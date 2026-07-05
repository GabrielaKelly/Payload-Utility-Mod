

/**
 * A class to represent an exception thrown when a command is typed with invalid syntax.
 */
package net.payload.cmd;

import net.minecraft.util.Formatting;

import java.io.Serial;

public class InvalidSyntaxException extends CommandException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidSyntaxException(Command cmd) {
        super(cmd);
    }

    @Override
    public void PrintToChat() {
        CommandManager.sendChatMessage("Invalid syntax! Correct usage: " + Formatting.LIGHT_PURPLE + ".payload " + cmd.getName() + " " + cmd.getSyntax() + Formatting.RESET);
    }
}
