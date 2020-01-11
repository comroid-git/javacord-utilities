// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.vdurmont.emoji.EmojiParser;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.logging.ExceptionLogger;

public class InfoReaction {
    public static void add(final Message message, final String emojiTag, final Boolean deleteAfterSend, final EmbedBuilder infoEmbed) {
        final String emoji = EmojiParser.parseToUnicode(emojiTag);
        final AtomicReference<Message> sentMessage = new AtomicReference<Message>();
        message.addReaction(emoji).exceptionally(ExceptionLogger.get(new Class[0]));
        final MessageDeleteListener deleteListener = event -> {
            message.removeOwnReactionByEmoji(emoji);
            message.delete();
            message.getMessageAttachableListeners().forEach((key, value) -> message.removeMessageAttachableListener(key));
        };
        final ReactionAddListener addListener = event -> {
            if (!event.getUser().isYourself() && event.getEmoji().asUnicodeEmoji().map(emoji::equals).orElse(false)) {
                message.getChannel().sendMessage(infoEmbed).thenAccept(myMsg -> {
                    sentMessage.set(myMsg);
                    myMsg.addMessageAttachableListener((MessageAttachableListener) deleteListener);
                }).thenAccept(nothing -> {
                    if (deleteAfterSend) {
                        message.delete().exceptionally(ExceptionLogger.get(new Class[0]));
                    }
                }).exceptionally(ExceptionLogger.get(new Class[0]));
            }
        };
        final ReactionRemoveListener removeListener = event -> event.getEmoji().asUnicodeEmoji().filter(emoji::equals).ifPresent(unicodeEmoji -> {
            if (!event.getUser().isYourself() && event.getUser().equals(message.getUserAuthor().get())) {
                sentMessage.get().delete().exceptionally(ExceptionLogger.get(new Class[0]));
            }
        });
        message.addReactionAddListener(addListener);
        message.addReactionRemoveListener(removeListener);
    }

    public static void add(final CompletableFuture<Message> msgFut, final String emojiTag, final Boolean deleteAfterSend, final EmbedBuilder infoEmbed) {
        add(msgFut.join(), emojiTag, deleteAfterSend, infoEmbed);
    }

    public static void add(final Message message, final EmbedBuilder infoEmbed) {
        add(message, "\u2139", false, infoEmbed);
    }

    public static void add(final CompletableFuture<Message> msgFut, final EmbedBuilder infoEmbed) {
        add(msgFut.join(), "\u2139", false, infoEmbed);
    }

    public static void remove(final Message fromMessage) {
        fromMessage.removeOwnReactionsByEmoji("\u2705", "\u2757", "\u274c", "\u26d4", "\u2049", "\ud83d\udd1a");
        fromMessage.getMessageAttachableListeners().forEach((key, value) -> fromMessage.removeMessageAttachableListener(key));
    }
}
