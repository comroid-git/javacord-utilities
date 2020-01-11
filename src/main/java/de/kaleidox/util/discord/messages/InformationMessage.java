// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.messages;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import de.kaleidox.util.Bot;
import de.kaleidox.util.Utils;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

public class InformationMessage {
    private static final ConcurrentHashMap<Messageable, InformationMessage> selfMap;
    private Messageable messageable;
    private ArrayList<InformationField> fields;
    private AtomicReference<Message> myMessage;

    static {
        selfMap = new ConcurrentHashMap<Messageable, InformationMessage>();
    }

    public InformationMessage(final Messageable messageable) {
        this.fields = new ArrayList<InformationField>();
        this.myMessage = new AtomicReference<Message>();
        this.messageable = messageable;
        InformationMessage.selfMap.putIfAbsent(messageable, this);
    }

    public InformationMessage addField(final String name, final String title, final String text) {
        return this.addField(name, title, text, false);
    }

    public InformationMessage addField(final String name, final String title, final String text, final boolean inline) {
        this.fields.add(new InformationField(name, title, text, inline));
        return this;
    }

    public InformationMessage editField(final String name, final String newText) {
        return this.editField(name, null, newText, false);
    }

    public InformationMessage editField(final String name, @Nullable final String newTitle, final String newText) {
        return this.editField(name, newTitle, newText, false);
    }

    public InformationMessage editField(final String name, @Nullable final String newTitle, final String newText, final boolean newInline) {
        final Optional<InformationField> complex = Utils.findComplex(this.fields, name, InformationField::getName);
        if (complex.isPresent()) {
            final InformationField field = complex.get();
            if (newTitle != null) {
                field.setTitle(newTitle);
            }
            field.setText(newText);
            field.setInline(newInline);
            return this;
        }
        throw new NullPointerException("Could not find field: " + name);
    }

    public InformationMessage removeField(final String name) {
        final Optional<InformationField> complex = Utils.findComplex(this.fields, name, InformationField::getName);
        if (complex.isPresent()) {
            final InformationField field = complex.get();
            this.fields.remove(field);
            return this;
        }
        throw new NullPointerException("Could not find field: " + name);
    }

    public void refresh() {
        final EmbedBuilder embed = Bot.getEmbed();
        for (final InformationField field : this.fields) {
            embed.addField(field.title, field.text, field.inline);
        }
        if (this.myMessage.get() != null) {
            this.myMessage.get().delete().thenRunAsync(() -> this.messageable.sendMessage(embed).thenAcceptAsync((Consumer) this.myMessage::set).exceptionally(ExceptionLogger.get())).exceptionally(ExceptionLogger.get(new Class[0]));
        } else {
            this.messageable.sendMessage(embed).thenAcceptAsync(msg -> this.myMessage.set(msg)).exceptionally(ExceptionLogger.get(new Class[0]));
        }
    }

    public static final InformationMessage get(final Messageable messageable) {
        return InformationMessage.selfMap.getOrDefault(messageable, new InformationMessage(messageable));
    }

    class InformationField {
        private String name;
        private String title;
        private String text;
        private boolean inline;

        InformationField(final String name, final String title, final String text, final boolean inline) {
            this.name = name;
            this.title = title;
            this.text = text;
            this.inline = inline;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public String getText() {
            return this.text;
        }

        public void setText(final String text) {
            this.text = text;
        }

        public boolean getInline() {
            return this.inline;
        }

        public void setInline(final boolean inline) {
            this.inline = inline;
        }
    }
}
