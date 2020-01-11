// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.util.logging.ExceptionLogger;

public class PagedEmbed {
    public static final int FIELD_MAX_CHARS = 1024;
    public static final int MAX_CHARS_PER_PAGE = 4500;
    public static final int MAX_FIELDS_PER_PAGE = 8;
    public static final String PREV_PAGE_EMOJI = "\u2b05";
    public static final String NEXT_PAGE_EMOJI = "\u27a1";
    private final Messageable messageable;
    private final Supplier<EmbedBuilder> embedsupplier;
    private ConcurrentHashMap<Integer, List<Field>> pages;
    private List<Field> fields;
    private int page;
    private AtomicReference<Message> sentMessage;

    public PagedEmbed(final Messageable messageable, final Supplier<EmbedBuilder> embedsupplier) {
        this.pages = new ConcurrentHashMap<Integer, List<Field>>();
        this.fields = new ArrayList<Field>();
        this.sentMessage = new AtomicReference<Message>();
        this.messageable = messageable;
        this.embedsupplier = embedsupplier;
    }

    public PagedEmbed addField(final String title, final String text) {
        return this.addField(title, text, false);
    }

    public PagedEmbed addField(final String title, final String text, final boolean inline) {
        this.fields.add(new Field(title, text, inline));
        return this;
    }

    public CompletableFuture<Message> build() {
        this.page = 1;
        this.refreshPages();
        final CompletableFuture<Message> future = this.messageable.sendMessage(this.embedsupplier.get());
        future.thenAcceptAsync(message -> {
            this.sentMessage.set(message);
            if (this.pages.size() != 1) {
                message.addReaction("\u2b05");
                message.addReaction("\u27a1");
                message.addReactionAddListener(this::onReactionClick);
                message.addReactionRemoveListener(this::onReactionClick);
            }
            message.addMessageDeleteListener(delete -> message.getMessageAttachableListeners().forEach((a, b) -> message.removeMessageAttachableListener(a))).removeAfter(3L, TimeUnit.HOURS).addRemoveHandler(() -> {
                this.sentMessage.get().removeAllReactions();
                this.sentMessage.get().getMessageAttachableListeners().forEach((a, b) -> message.removeMessageAttachableListener(a));
            });
            return;
        }).exceptionally((Function<Throwable, ? extends Void>) ExceptionLogger.get(new Class[0]));
        return future;
    }

    public EmbedBuilder getRawEmbed() {
        return this.embedsupplier.get();
    }

    public Supplier<EmbedBuilder> getEmbedsupplier() {
        return this.embedsupplier;
    }

    private void refreshPages() {
        int fieldCount = 0;
        int pageChars = 0;
        int totalChars = 0;
        int thisPage = 1;
        this.pages.clear();
        for (final Field field2 : this.fields) {
            this.pages.putIfAbsent(thisPage, new ArrayList<Field>());
            if (fieldCount <= 8 && pageChars <= 1024 * fieldCount && totalChars < 4500) {
                this.pages.get(thisPage).add(field2);
                ++fieldCount;
                pageChars += field2.getTotalChars();
                totalChars += field2.getTotalChars();
            } else {
                ++thisPage;
                this.pages.putIfAbsent(thisPage, new ArrayList<Field>());
                this.pages.get(thisPage).add(field2);
                fieldCount = 1;
                pageChars = field2.getTotalChars();
                totalChars = field2.getTotalChars();
            }
        }
        final EmbedBuilder embed = this.embedsupplier.get();
        this.pages.get(this.page).forEach(field -> embed.addField(field.getTitle(), field.getText(), field.getInline()));
        embed.setFooter("Page " + this.page + " of " + this.pages.size());
        if (this.sentMessage.get() != null) {
            this.sentMessage.get().edit(embed);
        }
    }

    private void onReactionClick(final SingleReactionEvent event) {
        //
        // This method could not be decompiled.
        //
        // Original Bytecode:
        //
        //     1: invokeinterface org/javacord/api/event/message/reaction/SingleReactionEvent.getEmoji:()Lorg/javacord/api/entity/emoji/Emoji;
        //     6: invokeinterface org/javacord/api/entity/emoji/Emoji.asUnicodeEmoji:()Ljava/util/Optional;
        //    11: aload_0         /* this */
        //    12: aload_1         /* event */
        //    13: invokedynamic   BootstrapMethod #2, accept:(Lde/kaleidox/util/discord/messages/PagedEmbed;Lorg/javacord/api/event/message/reaction/SingleReactionEvent;)Ljava/util/function/Consumer;
        //    18: invokevirtual   java/util/Optional.ifPresent:(Ljava/util/function/Consumer;)V
        //    21: return
        //
        // The error that occurred was:
        //
        // java.lang.NullPointerException
        //     at com.strobel.decompiler.languages.java.ast.NameVariables.generateNameForVariable(NameVariables.java:264)
        //     at com.strobel.decompiler.languages.java.ast.NameVariables.assignNamesToVariables(NameVariables.java:198)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:276)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at com.strobel.decompiler.DecompilerDriver.decompileType(DecompilerDriver.java:330)
        //     at com.strobel.decompiler.DecompilerDriver.decompileJar(DecompilerDriver.java:251)
        //     at com.strobel.decompiler.DecompilerDriver.main(DecompilerDriver.java:126)
        //
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }

    class Field {
        private final String title;
        private final String text;
        private final boolean inline;

        Field(final String title, final String text, final boolean inline) {
            this.title = title;
            this.text = text;
            this.inline = inline;
        }

        String getTitle() {
            return this.title;
        }

        String getText() {
            return this.text;
        }

        boolean getInline() {
            return this.inline;
        }

        int getTotalChars() {
            return this.title.length() + this.text.length();
        }
    }
}
