// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.successstate;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collector;
import javax.annotation.Nullable;

import de.kaleidox.util.Bot;
import de.kaleidox.util.discord.util.InfoReaction;
import de.kaleidox.util.libs.CustomCollectors;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class SuccessState {
    public static final SuccessState NOT_RUN;
    public static final SuccessState SUCCESSFUL;
    public static final SuccessState SILENT;
    public static final SuccessState UNAUTHORIZED;
    public static final SuccessState SERVER_ONLY;
    private Type mostSevereType;
    private ArrayList<MessageRepresentation> messages;

    static {
        NOT_RUN = new SuccessState(Type.NOT_RUN);
        SUCCESSFUL = new SuccessState(Type.SUCCESSFUL);
        SILENT = new SuccessState(Type.SILENT);
        UNAUTHORIZED = new SuccessState(Type.UNAUTHORIZED);
        SERVER_ONLY = new SuccessState(Type.SERVER_ONLY);
    }

    public SuccessState() {
        this.mostSevereType = Type.NONE;
        this.messages = new ArrayList<MessageRepresentation>();
        this.mostSevereType = Type.NOT_RUN;
    }

    public SuccessState(final Type type) {
        this.mostSevereType = Type.NONE;
        this.messages = new ArrayList<MessageRepresentation>();
        this.mostSevereType = type;
    }

    public SuccessState(final Type type, @Nullable final String message) {
        this.mostSevereType = Type.NONE;
        this.messages = new ArrayList<MessageRepresentation>();
        this.mostSevereType = type;
        if (message != null) {
            this.addMessage(type, message);
        }
    }

    public SuccessState(final Type type, final String title, final String text) {
        this.mostSevereType = Type.NONE;
        this.messages = new ArrayList<MessageRepresentation>();
        this.addMessage(type, title, text);
    }

    public SuccessState addMessage(@Nullable final String text) {
        return this.addMessage(this.mostSevereType, this.mostSevereType.getStandardMessage(text));
    }

    public SuccessState addMessage(final String title, final String text) {
        return this.addMessage(this.mostSevereType, title, text);
    }

    public SuccessState addMessage(final Type messageType, @Nullable final String text) {
        return this.addMessage(messageType, messageType.getStandardMessage(text));
    }

    public SuccessState addMessage(final Type messageType, final String title, final String text) {
        return this.addMessage(messageType, new MessageRepresentation(messageType, title, text));
    }

    public SuccessState addMessage(final Type messageType, final MessageRepresentation messageRepresentation) {
        this.messages.add(messageRepresentation);
        if (messageType.severity >= this.mostSevereType.severity) {
            this.mostSevereType = messageType;
        }
        return this;
    }

    public void arguments() {
        this.addMessage(Type.ERRORED, "Too many or too few arguments.");
    }

    public void successful() {
        this.mostSevereType = Type.SUCCESSFUL;
    }

    public void silent() {
        this.mostSevereType = Type.SILENT;
    }

    public Type getType() {
        return this.mostSevereType;
    }

    public void evaluateOnMessage(final Message message) {
        final Optional<Server> server = message.getServer();
        final User user = message.getUserAuthor().get();
        EmbedBuilder embed;
        if (server.isPresent()) {
            embed = Bot.getEmbed(server.get(), user);
        } else {
            embed = Bot.getEmbed();
        }
        if (this.mostSevereType.severity > 1) {
            embed.setTitle(((this.messages.size() == 1) ? "An Error" : (this.messages.size() + 1 + " Errors")) + " occured:");
        }
        if (this.messages.size() == 0) {
            this.messages.add(this.mostSevereType.getStandardMessage(null));
        }
        for (final MessageRepresentation repres : this.messages) {
            embed.addField(repres.type.reaction + " " + repres.title, repres.text);
        }
        if (this.mostSevereType != Type.SILENT) {
            InfoReaction.add(message, this.mostSevereType.reaction, false, embed);
        }
    }

    public SuccessState merge(final SuccessState newS) {
        if (this.mostSevereType.severity < newS.mostSevereType.severity) {
            this.mostSevereType = newS.mostSevereType;
        }
        this.messages.addAll(newS.messages);
        return this;
    }

    public static Collector<SuccessState, SuccessState, SuccessState> collectMessages(final SuccessState ontoState, final int severityLevelMinimum) {
        return new CustomCollectors.CustomCollectorImpl<SuccessState, SuccessState, SuccessState>(() -> ontoState, SuccessState::merge, SuccessState::merge, CustomCollectors.CH_ID);
    }
}
