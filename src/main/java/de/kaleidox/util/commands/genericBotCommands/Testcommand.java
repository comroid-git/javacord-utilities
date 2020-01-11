// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands.genericBotCommands;

import java.util.List;
import java.util.Optional;

import de.kaleidox.util.commands.CommandBase;
import de.kaleidox.util.commands.CommandGroup;
import de.kaleidox.util.commands.EmbedMaker;
import de.kaleidox.util.objects.successstate.SuccessState;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

public class Testcommand extends CommandBase {
    public Testcommand() {
        super(new String[]{"testcommand", "debug"}, false, true, new int[]{0, 999}, CommandGroup.NONE, EmbedMaker.getBasicEmbed().addField("you should not be using this", ":P"));
    }

    @Override
    public SuccessState runServer(final MessageCreateEvent event, final List<String> param) {
        final Message message = event.getMessage();
        final Optional<User> optionalUser = message.getUserAuthor();
        final Server srv = event.getServer().get();
        final ServerTextChannel channel = event.getServerTextChannel().get();
        final DiscordApi api = event.getApi();
        final User user = event.getMessage().getUserAuthor().get();
        if (optionalUser.isPresent()) {
            final User usr = optionalUser.get();
            if (usr.isBotOwner()) {
            }
        }
        return SuccessState.UNAUTHORIZED;
    }

    @Override
    public SuccessState runPrivate(final MessageCreateEvent event, final List<String> param) {
        final Optional<User> optionalUser = event.getMessage().getUserAuthor();
        if (optionalUser.isPresent()) {
            final User usr = optionalUser.get();
            if (usr.isBotOwner()) {
                event.getServerTextChannel().get();
                return SuccessState.SUCCESSFUL.addMessage("The cake is a lie.");
            }
        }
        return SuccessState.UNAUTHORIZED;
    }
}
