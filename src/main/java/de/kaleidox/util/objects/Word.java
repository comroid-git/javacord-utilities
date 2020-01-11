// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects;

public class Word
{
    private String singular;
    private String plural;
    
    public Word(final String singular, final String plural) {
        this.plural = plural;
        this.singular = singular;
    }
    
    public String get(final Number n) {
        if (n.equals(1)) {
            return this.singular;
        }
        return this.plural;
    }
}
