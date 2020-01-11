// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands.baseCommands;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.kaleidox.util.commands.CommandBase;
import de.kaleidox.util.commands.CommandGroup;
import de.kaleidox.util.commands.EmbedMaker;
import de.kaleidox.util.objects.successstate.SuccessState;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;

public class Invite extends CommandBase {
    public Invite() {
        super("invite", false, true, true, new int[]{0, 1}, CommandGroup.BASIC, EmbedMaker.invite());
    }

    @Override
    public SuccessState runServer(final MessageCreateEvent event, final List<String> param) {
        final AtomicReference<SuccessState> returnValue = new AtomicReference<SuccessState>(SuccessState.NOT_RUN);
        final Message msg = event.getMessage();
        final AtomicReference<SuccessState> atomicReference;
        msg.getServerTextChannel().ifPresent(serverTextChannel -> {
            serverTextChannel.sendMessage(EmbedMaker.invite());
            atomicReference.set(SuccessState.SUCCESSFUL);
            return;
        });
        return returnValue.get();
    }

    @Override
    public SuccessState runPrivate(final MessageCreateEvent event, final List<String> param) {
        final AtomicReference<SuccessState> returnValue = new AtomicReference<SuccessState>(SuccessState.NOT_RUN);
        final Message msg = event.getMessage();
        final AtomicReference<SuccessState> atomicReference;
        msg.getUserAuthor().ifPresent(user -> {
            user.sendMessage(EmbedMaker.invite());
            atomicReference.set(SuccessState.SUCCESSFUL);
            return;
        });
        return returnValue.get();
    }
}
