// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands.authCommands;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

import de.kaleidox.util.commands.CommandBase;
import de.kaleidox.util.commands.CommandGroup;
import de.kaleidox.util.commands.EmbedMaker;
import de.kaleidox.util.objects.Auth;
import de.kaleidox.util.objects.successstate.SuccessState;
import de.kaleidox.util.objects.successstate.Type;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

public class AuthRemove extends CommandBase {
    public AuthRemove() {
        super(new String[]{"unauth", "auth-remove", "auth-delete"}, true, false, new int[]{0, 1}, CommandGroup.AUTH_COMMANDS, EmbedMaker.getBasicEmbed().addField("Auth Setup", "Removes one or more Users to this Server's Auth list.\n").addField("Note:", "_Administrators and Users with Permission \"Manage Server\" are Auth by Default._"));
    }

    @Override
    public SuccessState runServer(final MessageCreateEvent event, final List<String> param) {
        SuccessState state = new SuccessState(Type.NOT_RUN);
        final Message msg = event.getMessage();
        final Server srv = msg.getServer().get();
        final List<User> mentionedUsers = msg.getMentionedUsers();
        final Auth auth = Auth.softGet(srv);
        if (mentionedUsers.size() < 1) {
            state.addMessage(Type.ERRORED, "No user mentions found!");
        } else {
            state = mentionedUsers.stream().map((Function<? super Object, ?>) auth::removeAuth).collect((Collector<? super Object, SuccessState, SuccessState>) SuccessState.collectMessages(state, 1));
        }
        return state;
    }

    @Override
    public SuccessState runPrivate(final MessageCreateEvent event, final List<String> param) {
        return SuccessState.SERVER_ONLY;
    }
}
