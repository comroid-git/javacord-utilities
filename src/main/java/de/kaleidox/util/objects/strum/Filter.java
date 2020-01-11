// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.strum;

import java.util.function.Predicate;

public class Filter<T> implements StrumElement
{
    private int ordinary;
    private Predicate<T> predicate;
    
    public Filter(final int ordinary, final Predicate<T> predicate) {
        this.ordinary = -1;
        this.ordinary = ordinary;
        this.predicate = predicate;
    }
    
    @Override
    public ElementType getType() {
        return ElementType.FILTER;
    }
    
    public int getOrdinary() {
        return this.ordinary;
    }
}
