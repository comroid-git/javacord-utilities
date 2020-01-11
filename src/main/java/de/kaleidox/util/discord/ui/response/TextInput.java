// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.ui.response;

import org.javacord.api.event.message.MessageCreateEvent;
import java.util.List;
import org.javacord.api.listener.ObjectAttachableListener;
import java.util.function.Consumer;
import org.javacord.api.entity.message.Message;
import org.javacord.api.listener.message.MessageAttachableListener;
import de.kaleidox.util.listeners.MessageListeners;
import java.util.function.Function;
import org.javacord.api.util.logging.ExceptionLogger;
import de.kaleidox.util.objects.NamedItem;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.user.User;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import java.util.function.Supplier;
import org.javacord.api.entity.message.Messageable;
import de.kaleidox.util.discord.EmbedFieldRepresentative;

public class TextInput extends ResponseElement<String>
{
    private Style style;
    private EmbedFieldRepresentative field;
    
    public TextInput(final String name, final Messageable parent, @Nullable final Supplier<EmbedBuilder> embedBaseSupplier, @Nullable final Predicate<User> userCanRespond) {
        super(name, parent, embedBaseSupplier, userCanRespond);
        this.style = Style.FULL;
    }
    
    public TextInput setQuestion(final String title, final String text) {
        this.field = new EmbedFieldRepresentative(title, text, false);
        return this;
    }
    
    public TextInput setStyle(final Style style) {
        this.style = style;
        return this;
    }
    
    @Override
    public CompletableFuture<NamedItem<String>> build() {
        final CompletableFuture<NamedItem<String>> future = new CompletableFuture<NamedItem<String>>();
        final EmbedBuilder embed = this.embedBaseSupplier.get();
        embed.setDescription("Please type in your response:").setTimestampToNow();
        if (this.field != null) {
            this.field.fillBuilder(embed);
        }
        final CompletableFuture<Object> completableFuture;
        this.parent.sendMessage(embed).thenAcceptAsync(message -> {
            this.affiliateMessages.add(message);
            message.getChannel().addMessageCreateListener(event -> {
                this.affiliateMessages.add(event.getMessage());
                final Message msg = event.getMessage();
                final User user = msg.getUserAuthor().get();
                if (!user.isYourself() && this.userCanRespond.test(user)) {
                    completableFuture.complete(new NamedItem<String>(this.name, (this.style == Style.READABLE) ? msg.getReadableContent() : msg.getContent()));
                }
            });
            completableFuture.thenAcceptAsync(string -> {
                if (string != null) {
                    message.edit(this.field.fillBuilder(this.embedBaseSupplier.get()).addField("Your answer was:", (String)string.getItem()));
                }
                else {
                    message.edit(this.field.fillBuilder(this.embedBaseSupplier.get()).addField("Nobody typed anything.", "There was no valid answer."));
                }
                return;
            }).exceptionally((Function<Throwable, ? extends Void>)ExceptionLogger.get(new Class[0]));
            message.addMessageDeleteListener(MessageListeners::deleteCleanup).removeAfter(this.duration, this.timeUnit).addRemoveHandler(() -> {
                completableFuture.complete(null);
                message.getMessageAttachableListeners().forEach((k, v) -> message.removeMessageAttachableListener((MessageAttachableListener)k));
                return;
            });
            if (this.deleteLater) {
                completableFuture.thenRunAsync(() -> this.affiliateMessages.forEach(Message::delete)).exceptionally((Function<Throwable, ? extends Void>)ExceptionLogger.get(new Class[0]));
            }
            return;
        });
        return future;
    }
    
    public enum Style
    {
        READABLE, 
        FULL;
    }
}
