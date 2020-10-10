package org.comroid.javacord.util.model.container;

import org.comroid.javacord.util.server.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class ValueContainer implements ContainerAccessor {
    private final String fallbackString;
    private Object value;

    @Override
    public boolean isNull() {
        return getRaw() == null;
    }

    @Override
    public Object getRaw() {
        return value;
    }

    public void setRaw(Object raw) {
        this.value = raw;
    }

    public ValueContainer(String fallbackString) {
        this(fallbackString, null);
    }

    public ValueContainer(String fallbackString, Object value) {
        this.fallbackString = Objects.requireNonNull(fallbackString, "FallbackString cannot be null");
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ValueContainer)
            return ((ValueContainer) obj).value.equals(value);
        return false;
    }

    @Override
    public String toString() {
        return String.format("ValueContainer[%s]", value);
    }

    @Override
    public @NotNull String fallbackString() {
        return fallbackString;
    }

    @Override
    public @NotNull String stringValue() {
        return String.valueOf(getRaw());
    }

    @Override
    public @NotNull String asString(String... fallback) {
        return new PrioritySupplier<>(false, "null")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join(" ", fallback))
                .butRather(this::stringValue)
                .get();
    }

    @Override
    public boolean asBoolean(String... fallback) {
        return Boolean.parseBoolean(new PrioritySupplier<>(false, "false")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get(str -> str.matches(Property.DEFAULT_PATTERNS.get(Boolean.class)), false));
    }

    @Override
    public int asInt(String... fallback) {
        return Integer.parseInt(new PrioritySupplier<>(false, "0")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get(str -> str.matches(Property.DEFAULT_PATTERNS.get(Integer.class)), false));
    }

    @Override
    public long asLong(String... fallback) {
        return Long.parseLong(new PrioritySupplier<>(false, "0")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get(str -> str.matches(Property.DEFAULT_PATTERNS.get(Long.class)), false));
    }

    @Override
    public float asFloat(String... fallback) {
        return Float.parseFloat(new PrioritySupplier<>(false, "0.0")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get(str -> str.matches(Property.DEFAULT_PATTERNS.get(Float.class)), false));
    }

    @Override
    public double asDouble(String... fallback) {
        return Double.parseDouble(new PrioritySupplier<>(false, "0.0")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get(str -> str.matches(Property.DEFAULT_PATTERNS.get(Double.class)), false));
    }
}
