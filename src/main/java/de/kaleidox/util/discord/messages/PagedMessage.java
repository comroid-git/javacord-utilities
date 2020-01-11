// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.messages;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import de.kaleidox.util.libs.listeners.MessageListeners;
import de.kaleidox.util.objects.Emoji;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.javacord.api.listener.message.MessageAttachableListener;

public class PagedMessage {
    private static final ConcurrentHashMap<Messageable, PagedMessage> selfMap;
    private static final String PREV_PAGE_EMOJI = "\u2b05";
    private static final String NEXT_PAGE_EMOJI = "\u27a1";
    private static final int SUITABLE_MAX_LENGTH = 1700;
    private Messageable parent;
    private Supplier<String> head;
    private Supplier<String> body;
    private Message lastMessage;
    private List<String> pages;
    private int page;

    static {
        selfMap = new ConcurrentHashMap<Messageable, PagedMessage>();
    }

    private PagedMessage(final Messageable inParent, final Supplier<String> head, final Supplier<String> body) {
        this.lastMessage = null;
        this.pages = new ArrayList<String>();
        this.parent = inParent;
        this.head = head;
        this.body = body;
        this.page = 0;
        this.resend();
    }

    public void refresh() {
        this.page = 0;
        this.refreshPage();
    }

    public void refreshPage() {
        this.refreshPages();
        if (this.lastMessage != null) {
            this.lastMessage.edit(this.getPageContent());
        }
    }

    public void resend() {
        this.refreshPages();
        if (this.lastMessage != null) {
            this.lastMessage.delete("Outdated");
        }
        this.parent.sendMessage(this.getPageContent()).thenAcceptAsync(msg -> {
            (this.lastMessage = msg).addMessageAttachableListener((MessageAttachableListener) MessageListeners.MESSAGE_DELETE_CLEANUP);
            msg.addReactionAddListener(this::onPageClick);
            msg.addReactionRemoveListener(this::onPageClick);
            msg.addReaction("\u2b05");
            msg.addReaction("\u27a1");
        });
    }

    private void onPageClick(final SingleReactionEvent event) {
        if (!event.getUser().isYourself()) {
            final Emoji emoji = new Emoji(event.getEmoji());
            final String printable = emoji.getPrintable();
            switch (printable) {
                case "\u2b05": {
                    if (this.page > 0) {
                        --this.page;
                    }
                    this.refreshPage();
                    break;
                }
                case "\u27a1": {
                    if (this.page < this.pages.size() - 1) {
                        ++this.page;
                    }
                    this.refreshPage();
                    break;
                }
            }
        }
    }

    private String getPageContent() {
        return this.pages.get(this.page) + "\n\n" + "`Page " + (this.page + 1) + " of " + this.pages.size() + " | " + "Last Refresh: " + new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis())) + " [GMT+2]`";
    }

    private void refreshPages() {
        final String completeHead = this.head.get();
        final String completeBody = this.body.get();
        final String completeMessage = completeHead + completeBody;
        final List<String> bodyLines = Arrays.asList(completeBody.split("\n"));
        this.pages.clear();
        if (completeMessage.length() < 1700) {
            this.pages.add(completeMessage);
        } else {
            StringBuilder pageBuilder = new StringBuilder(completeHead);
            for (int i = 0; i < bodyLines.size(); ++i) {
                pageBuilder.append(bodyLines.get(i));
                pageBuilder.append("\n");
                if (i == bodyLines.size() - 1 || pageBuilder.length() + bodyLines.get(i + 1).length() >= 1700) {
                    this.pages.add(pageBuilder.toString());
                    pageBuilder = new StringBuilder(completeHead);
                }
            }
        }
    }

    public static final PagedMessage get(final Messageable forParent, final Supplier<String> defaultHead, final Supplier<String> defaultBody) {
        if (PagedMessage.selfMap.containsKey(forParent)) {
            final PagedMessage val = PagedMessage.selfMap.get(forParent);
            val.resend();
            return val;
        }
        return PagedMessage.selfMap.put(forParent, new PagedMessage(forParent, defaultHead, defaultBody));
    }

    public static final Optional<PagedMessage> get(final Messageable forParent) {
        if (PagedMessage.selfMap.containsKey(forParent)) {
            final PagedMessage val = PagedMessage.selfMap.get(forParent);
            val.resend();
            return Optional.of(val);
        }
        return Optional.empty();
    }
}
