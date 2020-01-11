// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.strum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Strum<A> {
    private ArrayList<A> items;
    private ArrayList<StrumElement> actions;
    private Strum<?> follower;

    public Strum() {
        this.items = new ArrayList<A>();
        this.actions = new ArrayList<StrumElement>();
    }

    private Strum(final List<A> fillWith) {
        this.items = new ArrayList<A>();
        this.actions = new ArrayList<StrumElement>();
        this.items.addAll(fillWith);
    }

    public Strum<A> refill(final A with) {
        this.newItem(with);
        return this;
    }

    public Strum<A> filter(final Predicate<A> predicate) {
        this.actions.add(new Filter<Object>(this.actions.size(), (Predicate<Object>) predicate));
        final ArrayList<A> newElements = new ArrayList<A>();
        for (final A item : this.items) {
            if (predicate.test(item)) {
                newElements.add(item);
            }
        }
        return (Strum<A>) (this.follower = new Strum<Object>(newElements));
    }

    public <B> Strum<B> map(final Function<A, B> function) {
        this.actions.add(new Map<Object, Object>(this.actions.size(), (Function<Object, Object>) function));
        final ArrayList<B> newElements = new ArrayList<B>();
        for (final A item : this.items) {
            newElements.add(function.apply(item));
        }
        return (Strum<B>) (this.follower = new Strum<Object>(newElements));
    }

    private void newItem(final A with) {
        final StrumElement nextAction = this.actions.get(0);
    }
}
