// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.messages;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import de.kaleidox.util.libs.listeners.MessageListeners;
import de.kaleidox.util.objects.Emoji;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.javacord.api.listener.message.MessageAttachableListener;

public class RefreshableMessage {
    private static final ConcurrentHashMap<Messageable, RefreshableMessage> selfMap;
    private static final String REFRESH_EMOJI = "\ud83d\udd04";
    private Messageable parent;
    private Supplier<Object> refresher;
    private Message lastMessage;

    static {
        selfMap = new ConcurrentHashMap<Messageable, RefreshableMessage>();
    }

    private RefreshableMessage(final Messageable inParent, final Supplier<Object> refresher) {
        this.lastMessage = null;
        this.parent = inParent;
        this.refresher = refresher;
        final Object item = refresher.get();
        CompletableFuture<Message> sent = null;
        if (item instanceof EmbedBuilder) {
            sent = this.parent.sendMessage((EmbedBuilder) item);
        } else if (item instanceof String) {
            sent = this.parent.sendMessage((String) item);
        } else if (item instanceof File) {
            sent = this.parent.sendMessage(new File[]{(File) item});
        }
        if (sent != null) {
            sent.thenAcceptAsync(msg -> {
                (this.lastMessage = msg).addMessageAttachableListener((MessageAttachableListener) MessageListeners.MESSAGE_DELETE_CLEANUP);
                msg.addReactionAddListener(this::onRefresh);
                msg.addReactionRemoveListener(this::onRefresh);
                msg.addReaction("\ud83d\udd04");
            });
        }
    }

    public void refresh() {
        if (this.lastMessage != null) {
            final Object item = this.refresher.get();
            if (item instanceof EmbedBuilder) {
                this.lastMessage.edit((EmbedBuilder) item);
            } else if (item instanceof String) {
                this.lastMessage.edit((String) item);
            } else if (item instanceof File) {
                this.resend();
            }
        }
    }

    public void resend() {
        final Object item = this.refresher.get();
        CompletableFuture<Message> sent = null;
        if (this.lastMessage != null) {
            this.lastMessage.delete("Outdated");
        }
        if (item instanceof EmbedBuilder) {
            sent = this.parent.sendMessage((EmbedBuilder) item);
        } else if (item instanceof String) {
            sent = this.parent.sendMessage((String) item);
        } else if (item instanceof File) {
            sent = this.parent.sendMessage(new File[]{(File) item});
        }
        if (sent != null) {
            sent.thenAcceptAsync(msg -> {
                (this.lastMessage = msg).addMessageAttachableListener((MessageAttachableListener) MessageListeners.MESSAGE_DELETE_CLEANUP);
                msg.addReactionAddListener(this::onRefresh);
                msg.addReactionRemoveListener(this::onRefresh);
                msg.addReaction("\ud83d\udd04");
            });
        }
    }

    private void onRefresh(final SingleReactionEvent event) {
        if (!event.getUser().isYourself()) {
            final Emoji emoji = new Emoji(event.getEmoji());
            if (emoji.getPrintable().equals("\ud83d\udd04")) {
                this.refresh();
            }
        }
    }

    public static final RefreshableMessage get(final Messageable forParent, final Supplier<Object> defaultRefresher) {
        if (RefreshableMessage.selfMap.containsKey(forParent)) {
            final RefreshableMessage val = RefreshableMessage.selfMap.get(forParent);
            val.resend();
            return val;
        }
        return RefreshableMessage.selfMap.put(forParent, new RefreshableMessage(forParent, defaultRefresher));
    }

    public static final Optional<RefreshableMessage> get(final Messageable forParent) {
        if (RefreshableMessage.selfMap.containsKey(forParent)) {
            final RefreshableMessage val = RefreshableMessage.selfMap.get(forParent);
            val.resend();
            return Optional.of(val);
        }
        return Optional.empty();
    }
}
