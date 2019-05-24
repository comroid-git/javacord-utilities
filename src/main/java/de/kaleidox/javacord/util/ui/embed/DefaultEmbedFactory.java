package de.kaleidox.javacord.util.ui.embed;

import java.util.function.Supplier;

import org.javacord.api.entity.message.embed.EmbedBuilder;

public enum DefaultEmbedFactory implements Supplier<EmbedBuilder> {
    INSTANCE;

    private Supplier<EmbedBuilder> embedSupplier;

    DefaultEmbedFactory() {
        this.embedSupplier = EmbedBuilder::new;
    }

    @Override
    public EmbedBuilder get() {
        return embedSupplier.get().removeAllFields();
    }

    public static void setEmbedSupplier(Supplier<EmbedBuilder> embedSupplier) {
        INSTANCE.embedSupplier = embedSupplier;
    }

    public static EmbedBuilder create() {
        return INSTANCE.get();
    }
}
