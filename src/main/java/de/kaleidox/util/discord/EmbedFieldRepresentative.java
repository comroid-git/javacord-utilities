// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord;

import org.javacord.api.entity.message.embed.EmbedBuilder;

public class EmbedFieldRepresentative
{
    private final String title;
    private final String text;
    private final boolean inline;
    
    public EmbedFieldRepresentative(final String title, final String text) {
        this(title, text, false);
    }
    
    public EmbedFieldRepresentative(final String title, final String text, final boolean inline) {
        this.title = title;
        this.text = text;
        this.inline = inline;
    }
    
    public EmbedBuilder fillBuilder(final EmbedBuilder embedBuilder) {
        return embedBuilder.addField(this.title, this.text, this.inline);
    }
}
