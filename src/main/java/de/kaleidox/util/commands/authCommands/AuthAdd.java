// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands.authCommands;

import org.javacord.api.entity.user.User;
import org.javacord.api.entity.message.Message;
import java.util.stream.Collector;
import java.util.function.Function;
import de.kaleidox.util.objects.Auth;
import org.javacord.api.entity.server.Server;
import de.kaleidox.util.objects.successstate.Type;
import de.kaleidox.util.objects.successstate.SuccessState;
import java.util.List;
import org.javacord.api.event.message.MessageCreateEvent;
import de.kaleidox.util.commands.EmbedMaker;
import de.kaleidox.util.commands.CommandGroup;
import de.kaleidox.util.commands.CommandBase;

public class AuthAdd extends CommandBase
{
    public AuthAdd() {
        super(new String[] { "auth", "auth-create", "auth-add" }, true, false, new int[] { 0, 1 }, CommandGroup.AUTH_COMMANDS, EmbedMaker.getBasicEmbed().addField("Auth Setup", "Adds one or more Users to this Server's Auth list.\nAuth Users have access to Bot Setup Commands.\n").addField("Note:", "_Administrators and Users with Permission \"Manage Server\" are Auth by Default._"));
    }
    
    @Override
    public SuccessState runServer(final MessageCreateEvent event, final List<String> param) {
        SuccessState state = new SuccessState(Type.NOT_RUN);
        final Message msg = event.getMessage();
        final Server srv = msg.getServer().get();
        final List<User> mentionedUsers = (List<User>)msg.getMentionedUsers();
        final Auth auth = Auth.softGet(srv);
        if (mentionedUsers.size() < 1) {
            state.addMessage(Type.ERRORED, "No User Mentions found.");
        }
        else {
            state = mentionedUsers.stream().map((Function<? super Object, ?>)auth::addAuth).collect((Collector<? super Object, SuccessState, SuccessState>)SuccessState.collectMessages(state, 1));
        }
        return state;
    }
    
    @Override
    public SuccessState runPrivate(final MessageCreateEvent event, final List<String> param) {
        return SuccessState.SERVER_ONLY;
    }
}
