// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.ui.response;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import java.util.List;
import org.javacord.api.listener.ObjectAttachableListener;
import java.util.function.Consumer;
import org.javacord.api.entity.message.Message;
import java.util.function.Function;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.api.listener.message.MessageAttachableListener;
import de.kaleidox.util.listeners.MessageListeners;
import de.kaleidox.util.objects.NamedItem;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.user.User;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import java.util.function.Supplier;
import org.javacord.api.entity.message.Messageable;
import de.kaleidox.util.discord.EmbedFieldRepresentative;

public class YesNo extends ResponseElement<Boolean>
{
    private static final String EMOJI_YES = "\u2705";
    private static final String EMOJI_NO = "\u274c";
    private EmbedFieldRepresentative field;
    
    public YesNo(final String name, final Messageable parent, @Nullable final Supplier<EmbedBuilder> embedBaseSupplier, @Nullable final Predicate<User> userCanRespond) {
        super(name, parent, embedBaseSupplier, userCanRespond);
    }
    
    public YesNo setQuestion(final String title, final String text) {
        this.field = new EmbedFieldRepresentative(title, text, false);
        return this;
    }
    
    @Override
    public CompletableFuture<NamedItem<Boolean>> build() {
        final EmbedBuilder embed = this.embedBaseSupplier.get();
        embed.setDescription("Yes/No question:").setTimestampToNow();
        if (this.field != null) {
            this.field.fillBuilder(embed);
        }
        final CompletableFuture<NamedItem<Boolean>> future = new CompletableFuture<NamedItem<Boolean>>();
        final CompletableFuture<NamedItem<Boolean>> completableFuture;
        this.parent.sendMessage(embed).thenAcceptAsync(message -> {
            this.affiliateMessages.add(message);
            message.addReaction("\u2705");
            message.addReaction("\u274c");
            message.addReactionAddListener(event -> {
                event.requestMessage().thenAcceptAsync((Consumer)this.affiliateMessages::add);
                final Emoji emoji = event.getEmoji();
                final User user = event.getUser();
                if (!user.isYourself()) {
                    if (this.userCanRespond.test(user) && emoji.asUnicodeEmoji().isPresent()) {
                        final String s = emoji.asUnicodeEmoji().get();
                        switch (s) {
                            case "\u2705": {
                                completableFuture.complete(new NamedItem<Boolean>(super.name, true));
                                break;
                            }
                            case "\u274c": {
                                completableFuture.complete(new NamedItem<Boolean>(super.name, false));
                                break;
                            }
                            default: {
                                event.requestMessage().thenAcceptAsync(msg -> msg.removeReactionByEmoji(user, emoji));
                                break;
                            }
                        }
                    }
                    else {
                        event.requestMessage().thenAcceptAsync(msg -> msg.removeReactionByEmoji(user, emoji));
                    }
                }
            });
            message.addMessageDeleteListener(MessageListeners::deleteCleanup).removeAfter(this.duration, this.timeUnit).addRemoveHandler(() -> completableFuture.complete(new NamedItem<Boolean>(super.name, false)));
            completableFuture.thenAcceptAsync(result -> {
                message.removeAllReactions();
                message.edit(this.field.fillBuilder(this.embedBaseSupplier.get()).addField("Answer:", ((boolean)result.getItem()) ? "Yes" : "No"));
                message.getMessageAttachableListeners().forEach((k, v) -> message.removeMessageAttachableListener((MessageAttachableListener)k));
                return;
            }).exceptionally((Function<Throwable, ? extends Void>)ExceptionLogger.get(new Class[0]));
            if (this.deleteLater) {
                completableFuture.thenRunAsync(() -> this.affiliateMessages.forEach(Message::delete)).exceptionally((Function<Throwable, ? extends Void>)ExceptionLogger.get(new Class[0]));
            }
            return;
        });
        return future;
    }
}
