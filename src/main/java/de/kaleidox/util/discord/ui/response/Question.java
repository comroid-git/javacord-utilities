// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.ui.response;

import de.kaleidox.util.interfaces.Subclass;
import java.util.Optional;
import org.javacord.api.entity.emoji.Emoji;
import de.kaleidox.util.Utils;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import java.util.List;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.util.logging.ExceptionLogger;
import java.util.function.Consumer;
import org.javacord.api.entity.message.Message;
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
import java.util.ArrayList;

public class Question<ResultType> extends ResponseElement<ResultType>
{
    private final ArrayList<Option> optionsOrdered;
    
    public Question(final String name, final Messageable parent, @Nullable final Supplier<EmbedBuilder> embedBaseSupplier, @Nullable final Predicate<User> userCanRespond) {
        super(name, parent, embedBaseSupplier, userCanRespond);
        this.optionsOrdered = new ArrayList<Option>();
    }
    
    public Question<ResultType> addOption(final String emoji, final String description, final ResultType representation) {
        try {
            if (representation.getClass() == Enum.class || representation.getClass().getMethod("toString", (Class<?>[])new Class[0]).getDeclaringClass() == representation.getClass()) {
                return this.addOption(emoji, representation.toString(), description, representation);
            }
            throw new RuntimeException("The Representation [" + representation + "] has to manually override the method \"toString()\"; or you have to use the implementation of \"addOption(String, String, String, ResultType)\".");
        }
        catch (NoSuchMethodException ignored) {
            throw new AssertionError((Object)"Fatal internal error.");
        }
    }
    
    public Question<ResultType> addOption(final String emoji, final String name, final String description, final ResultType representation) {
        return this.addOption(new Option(emoji, name, description, representation));
    }
    
    public Question<ResultType> addOption(final Option option) {
        if (this.optionsOrdered.stream().anyMatch(optionS -> optionS.getEmoji().equalsIgnoreCase(option.getEmoji()))) {
            throw new ArrayStoreException("Option Emojis can not duplicate!");
        }
        if (this.optionsOrdered.size() == 25) {
            throw new RuntimeException("Only 25 optionsOrdered are allowed.");
        }
        this.optionsOrdered.add(option);
        return this;
    }
    
    @Override
    public CompletableFuture<NamedItem<ResultType>> build() {
        if (this.optionsOrdered.isEmpty()) {
            throw new NullPointerException("No options registered!");
        }
        final EmbedBuilder embed = this.embedBaseSupplier.get();
        embed.setDescription("Voting will continue for " + this.duration + " " + this.timeUnit.name().toLowerCase() + ", beginning from the timestamp.").setTimestampToNow();
        this.optionsOrdered.forEach(option -> embed.addField(option.getEmoji() + " -> " + option.getName(), option.getDescription()));
        final CompletableFuture<NamedItem<ResultType>> future = new CompletableFuture<NamedItem<ResultType>>();
        final CompletableFuture completableFuture;
        this.parent.sendMessage(embed).thenAcceptAsync(message -> {
            this.affiliateMessages.add(message);
            this.optionsOrdered.forEach(option -> message.addReaction(option.getEmoji()));
            message.addReactionAddListener(event -> {
                event.requestMessage().thenAcceptAsync((Consumer)this.affiliateMessages::add);
                final Emoji emoji = event.getEmoji();
                final User user = event.getUser();
                if (!user.isYourself() && this.userCanRespond.test(user)) {
                    final Optional<Option> any = this.optionsOrdered.stream().filter(option -> Utils.compareAnyEmoji(emoji, option.getEmoji())).findAny();
                    if (any.isPresent()) {
                        completableFuture.complete(new NamedItem<Object>(this.name, any.get().getValue()));
                    }
                    else {
                        completableFuture.cancel(true);
                    }
                }
            });
            message.addMessageDeleteListener(MessageListeners::deleteCleanup).removeAfter(this.duration, this.timeUnit).addRemoveHandler(() -> {
                message.removeAllReactions();
                message.getMessageAttachableListeners().forEach((key, value) -> message.removeMessageAttachableListener((MessageAttachableListener)key));
                return;
            });
            if (this.deleteLater) {
                completableFuture.thenRunAsync(() -> this.affiliateMessages.forEach(Message::delete)).exceptionally(ExceptionLogger.get(new Class[0]));
            }
            return;
        }).exceptionally(ExceptionLogger.get(new Class[0]));
        return future;
    }
    
    public class Option implements Subclass
    {
        private final String emoji;
        private final String description;
        private final ResultType value;
        private final String name;
        
        public Option(final String emoji, final String name, final String description, final ResultType value) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
            this.value = value;
        }
        
        public String getEmoji() {
            return this.emoji;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getDescription() {
            return this.description;
        }
        
        public ResultType getValue() {
            return this.value;
        }
        
        @Override
        public String toString() {
            return "[" + this.emoji + "|" + this.name + "] with description [" + this.description + "]";
        }
    }
}
