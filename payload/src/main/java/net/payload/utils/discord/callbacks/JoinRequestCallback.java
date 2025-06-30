package net.payload.utils.discord.callbacks;

import com.sun.jna.Callback;
import net.payload.utils.discord.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(final DiscordUser p0);
}
