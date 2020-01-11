// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.libs;

import java.util.function.Predicate;

public final class Predicates {
    public static final Predicate<Object> IS_LONG;

    static {
        IS_LONG = (aLong -> {
            if (aLong instanceof Long) {
                return true;
            } else {
                try {
                    Long.parseLong(aLong.toString());
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            }
        });
    }
}
