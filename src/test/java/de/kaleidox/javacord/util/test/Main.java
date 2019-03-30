package de.kaleidox.javacord.util.test;

import java.awt.Color;

import de.kaleidox.javacord.util.commands.CommandHandler;

import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public final class Main {
    public static void main(String[] args) {
        new DiscordApiBuilder()
                .setToken(args[0])
                .login()
                .thenAcceptAsync(api -> {
                    CommandHandler cmd = new CommandHandler(api);

                    cmd.useDefaultHelp(() -> new EmbedBuilder().setColor(Color.BLUE));
                    cmd.autoDeleteResponseOnCommandDeletion = true;
                    cmd.prefixes = new String[]{"!dango "};
                });
    }
}
