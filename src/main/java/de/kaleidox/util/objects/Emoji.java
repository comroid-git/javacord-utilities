// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects;

import com.vdurmont.emoji.EmojiParser;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;
import org.javacord.api.entity.emoji.KnownCustomEmoji;

public class Emoji
{
    private org.javacord.api.entity.emoji.Emoji emoji;
    
    public Emoji(final org.javacord.api.entity.emoji.Emoji emoji) {
        if (emoji instanceof KnownCustomEmoji) {
            this.emoji = emoji.asKnownCustomEmoji().get();
        }
        else {
            if (!(emoji instanceof UnicodeEmojiImpl)) {
                this.emoji = null;
                throw new NullPointerException("Not an Emoji");
            }
            this.emoji = (org.javacord.api.entity.emoji.Emoji)UnicodeEmojiImpl.fromString(EmojiParser.parseToAliases(emoji.getMentionTag()));
        }
    }
    
    public Emoji(final String unicodeEmoji) {
        this.emoji = (org.javacord.api.entity.emoji.Emoji)UnicodeEmojiImpl.fromString(EmojiParser.parseToAliases(unicodeEmoji));
    }
    
    public String getPrintable() {
        if (this.emoji instanceof KnownCustomEmoji) {
            return this.emoji.asKnownCustomEmoji().get().getMentionTag();
        }
        if (this.emoji instanceof UnicodeEmojiImpl) {
            return EmojiParser.parseToUnicode(this.emoji.getMentionTag());
        }
        return "\u274c";
    }
    
    @Override
    public String toString() {
        return this.getPrintable();
    }
}
