// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.successstate;

class MessageRepresentation {
    String title;
    String text;
    Type type;

    public MessageRepresentation(final Type type, final String title, final String text) {
        this.text = text;
        this.title = title;
        this.type = type;
    }
}
