// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;

public class Wastebasket {
    public static void add(final Message msg) {
        if (msg.getAuthor().isYourself() && !msg.getPrivateChannel().isPresent()) {
            msg.addReaction("\ud83d\uddd1");
            msg.addReactionAddListener(reaAdd -> {
                final Emoji emoji = reaAdd.getEmoji();
                if (!reaAdd.getUser().isBot()) {
                    emoji.asUnicodeEmoji().ifPresent(then -> {
                        if (then.equals("\ud83d\uddd1")) {
                            msg.delete();
                        }
                    });
                }
            });
        }
    }

    public static void add(final CompletableFuture<Message> messageCompletableFuture) {
        messageCompletableFuture.thenAcceptAsync((Consumer<? super Message>) Wastebasket::add);
    }
}
