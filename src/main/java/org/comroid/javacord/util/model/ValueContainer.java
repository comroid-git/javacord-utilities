package org.comroid.javacord.util.model;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ConstantConditions")
public class ValueContainer {
    public boolean isNull() {
    }

    public Object raw() {
    }

    public @NotNull String fallbackString() {
    }

    public @NotNull String stringValue() {
        return String.valueOf(raw());
    }

    public String asString(String... fallback) {
        return getAny(String::valueOf, stringValue(), fallback);
    }

    public int asInt(String... fallback) {
        return Integer.parseInt(new PrioritySupplier<>(false, "0")
                .possible(this::fallbackString)
                .possible(String.join(" ", fallback))
                .possible(this::stringValue)
                .get());
    }

    public boolean asBoolean(String... fallback) {
        return Boolean.parseBoolean(new PrioritySupplier<>(false, "false")
                .possible(this::fallbackString)
                .possible(String.join(" ", fallback))
                .possible(this::stringValue)
                .get());
    }
}
