package de.kaleidox.javacord.util.embed;

import java.util.function.Supplier;

import org.javacord.api.entity.message.embed.EmbedBuilder;

public enum DefaultEmbedFactory implements Supplier<EmbedBuilder> {
    INSTANCE;

    private Supplier<EmbedBuilder> embedSupplier;

    DefaultEmbedFactory() {
        this.embedSupplier = EmbedBuilder::new;
    }

    public void setEmbedSupplier(Supplier<EmbedBuilder> embedSupplier) {
        this.embedSupplier = embedSupplier;
    }

    @Override
    public EmbedBuilder get() {
        return embedSupplier.get().removeAllFields();
    }
}
