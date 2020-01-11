// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects;

public class DoublePartObject<A, B> {
    private A partA;
    private B partB;

    public DoublePartObject(final A a, final B b) {
        this.partA = a;
        this.partB = b;
    }

    public A getA() {
        return this.partA;
    }

    public B getB() {
        return this.partB;
    }
}
