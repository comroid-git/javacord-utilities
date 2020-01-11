// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.strum;

import java.util.function.Function;

public class Map<A, B> implements StrumElement {
    private int ordinary;
    private Function<A, B> mapper;

    public Map(final int ordinary, final Function<A, B> mapper) {
        this.ordinary = -1;
        this.ordinary = ordinary;
        this.mapper = mapper;
    }

    @Override
    public ElementType getType() {
        return ElementType.MAP;
    }

    public int getOrdinary() {
        return this.ordinary;
    }
}
