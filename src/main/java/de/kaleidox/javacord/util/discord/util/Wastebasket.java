package de.kaleidox.javacord.util.discord.util;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;

import java.util.concurrent.CompletableFuture;

public class Wastebasket {
    public static void add(Message msg) {
        if (msg.getAuthor().isYourself() && !msg.getPrivateChannel().isPresent()) {
            msg.addReaction("🗑");
            msg.addReactionAddListener(reaAdd -> {
                Emoji emoji = reaAdd.getEmoji();

                if (!reaAdd.getUser().isBot()) {
                    emoji.asUnicodeEmoji().ifPresent(then -> {
                        if (then.equals("🗑")) {
                            msg.delete();
                        }
                    });
                }
            });
        }
    }

    public static void add(CompletableFuture<Message> messageCompletableFuture) {
        messageCompletableFuture.thenAcceptAsync(Wastebasket::add);
    }
}
