

package net.payload.cmd.commands;

import net.payload.cmd.Command;
import net.payload.cmd.InvalidSyntaxException;

public class CmdHud extends Command {

    public CmdHud() {
        super("hud", "Allows you to customize the hud using commands.", "[toggle] [value]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        if (parameters.length != 2)
            throw new InvalidSyntaxException(this);

        switch (parameters[0]) {
            default:
                throw new InvalidSyntaxException(this);
        }
    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        switch (previousParameter) {
            case "toggle":
                return new String[]{"on", "off"};
            default:
                return new String[]{"toggle"};
        }
    }
}
