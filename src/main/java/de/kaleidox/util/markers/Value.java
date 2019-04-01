package de.kaleidox.util.markers;

import java.util.Objects;

public class Value {
    protected final Setter setter;
    protected Object value;

        this.value = value;
    public Value(Object value) {

        setter = new Setter();
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public boolean isNull() {
        return Objects.isNull(value);
    }

    public String asString() {
        return String.valueOf(value);
    }

    public boolean asBoolean() {
        return Boolean.valueOf(asString());
    }

    public byte asByte() {
        return Byte.valueOf(asString());
    }

    public short asShort() {
        return Short.valueOf(asString());
    }

    public int asInt() {
        return Integer.valueOf(asString());
    }

    public float asFloat() {
        return Float.valueOf(asString());
    }

    public double asDouble() {
        return Double.valueOf(asString());
    }

    public long asLong() {
        return Long.valueOf(asString());
    }

    public char asChar() {
        return asString().charAt(0);
    }

    public Setter setter() {
        return setter;
    }

    public class Setter {
        private Setter() {}

        public void toObject(Object o) {
            value = o;
        }

        public void toString(String s) {
            value = s;
        }

        public void toBoolean(boolean b) {
            value = b;
        }

        public void toByte(byte b) {
            value = b;
        }

        public void toShort(short s) {
            value = s;
        }

        public void toInt(int i) {
            value = i;
        }

        public void toFloat(float f) {
            value = f;
        }

        public void toDouble(double d) {
            value = d;
        }

        public void toLong(long l) {
            value = l;
        }

        public void toChar(char c) {
            value = c;
        }
    }
}
