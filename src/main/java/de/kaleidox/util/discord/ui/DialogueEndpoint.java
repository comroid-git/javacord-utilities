// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.ui;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import de.kaleidox.util.objects.NamedItem;

public class DialogueEndpoint extends DialogueBranch<Void> {
    private final Consumer<List<NamedItem>> responsesConsumer;

    public DialogueEndpoint(final Consumer<List<NamedItem>> responsesConsumer) {
        super(null);
        this.responsesConsumer = responsesConsumer;
    }

    @Override
    protected CompletableFuture<Void> runEndpoint(final List<NamedItem> collectedItems) {
        return CompletableFuture.supplyAsync(() -> {
            this.responsesConsumer.accept(collectedItems);
            return null;
        });
    }
}
