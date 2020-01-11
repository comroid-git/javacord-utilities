// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands.authCommands;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import de.kaleidox.util.objects.Auth;
import org.javacord.api.entity.server.Server;
import de.kaleidox.util.objects.successstate.Type;
import de.kaleidox.util.objects.successstate.SuccessState;
import java.util.List;
import org.javacord.api.event.message.MessageCreateEvent;
import de.kaleidox.util.commands.EmbedMaker;
import de.kaleidox.util.commands.CommandGroup;
import de.kaleidox.util.commands.CommandBase;

public class AuthList extends CommandBase
{
    public AuthList() {
        super(new String[] { "auths", "auth-list", "authed" }, false, false, new int[] { 0, 1 }, CommandGroup.AUTH_COMMANDS, EmbedMaker.getBasicEmbed().addField("Auth Setup", "Sends a list of Authed users from this Server."));
    }
    
    @Override
    public SuccessState runServer(final MessageCreateEvent event, final List<String> param) {
        final SuccessState state = new SuccessState(Type.NOT_RUN);
        final Message msg = event.getMessage();
        final Server srv = msg.getServer().get();
        final Auth auth = Auth.softGet(srv);
        msg.getServerTextChannel().ifPresent(chl -> state.merge(auth.sendEmbed(chl)));
        return state;
    }
    
    @Override
    public SuccessState runPrivate(final MessageCreateEvent event, final List<String> param) {
        return SuccessState.SERVER_ONLY;
    }
}
