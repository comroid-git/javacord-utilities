// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects;

public class Value {
    private String of;
    private Class type;

    public Value(final String of, final Class ofType) {
        this.of = of;
        this.type = ofType;
    }

    public String asString() {
        return this.of;
    }

    public boolean asBoolean() {
        return this.type == Boolean.class && Boolean.valueOf(this.of);
    }

    public int asInteger() {
        if (this.type == Integer.class) {
            return Integer.parseInt(this.of);
        }
        return 0;
    }

    public long asLong() {
        if (this.type == Long.class) {
            return Long.parseLong(this.of);
        }
        return 0L;
    }

    public float asFloat() {
        if (this.type == Float.class) {
            return Float.parseFloat(this.of);
        }
        return 0.0f;
    }

    public double asDouble() {
        if (this.type == Double.class) {
            return Double.parseDouble(this.of);
        }
        return 0.0;
    }

    public LogicalOperator asLogicalOperator() {
        if (this.type == LogicalOperator.class) {
            return LogicalOperator.find(this.of).orElse(LogicalOperator.UNKNOWN);
        }
        return LogicalOperator.UNKNOWN;
    }

    @Override
    public String toString() {
        return this.of;
    }
}
