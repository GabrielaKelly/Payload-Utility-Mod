package net.payload.cmd.commands;

import net.payload.Payload;
import net.payload.PayloadClient;
import net.payload.cmd.Command;
import net.payload.cmd.CommandManager;
import net.payload.cmd.InvalidSyntaxException;
import net.minecraft.client.font.TextRenderer;

public class CmdFont extends Command {

    public CmdFont() {
        super("font", "Sets the HUD font.", "[set] [value]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        if (parameters.length != 2)
            throw new InvalidSyntaxException(this);

        PayloadClient payload = Payload.getInstance();

        switch (parameters[0]) {
            case "set":
                try {
                    String font = parameters[1];
                    TextRenderer t = payload.fontManager.fontRenderers.get(font);
                    if (t != null) {
                        payload.fontManager.SetRenderer(t);
                    }
                } catch (Exception e) {
                    CommandManager.sendChatMessage("Invalid value.");
                }
                break;
            default:
                throw new InvalidSyntaxException(this);
        }
    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        switch (previousParameter) {
            case "set":
                PayloadClient payload = Payload.getInstance();

                String[] suggestions = new String[payload.fontManager.fontRenderers.size()];

                int i = 0;
                for (String fontName : payload.fontManager.fontRenderers.keySet())
                    suggestions[i++] = fontName;

                return suggestions;
            default:
                return new String[]{"set"};
        }
    }
}