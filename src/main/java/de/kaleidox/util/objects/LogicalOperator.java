// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects;

import java.util.Optional;
import java.util.stream.Stream;

public enum LogicalOperator {
    UNKNOWN("unknown"),
    AND("and"),
    OR("or"),
    NOT("not"),
    XOR("xor");

    String name;

    LogicalOperator(final String name) {
        this.name = name;
    }

    public boolean test(final Stream<Boolean> booleans) {
        switch (this) {
            case AND: {
                return booleans.allMatch(b -> b);
            }
            case OR: {
                return booleans.anyMatch(b -> b);
            }
            case XOR: {
                return booleans.filter(b -> b).count() > 1L;
            }
            case NOT: {
                return booleans.noneMatch(b -> b);
            }
            default: {
                return false;
            }
        }
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "LogicalOperator (" + this.name + ")";
    }

    public static Optional<LogicalOperator> find(final String tag) {
        return Stream.of(values()).filter(lo -> lo.name.equalsIgnoreCase(tag)).findAny();
    }
}
