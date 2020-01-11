// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects;

public class NamedItem<T>
{
    private final String name;
    private final T item;
    
    public NamedItem(final String name, final T item) {
        this.name = name;
        this.item = item;
    }
    
    public String getName() {
        return this.name;
    }
    
    public T getItem() {
        return this.item;
    }
}
