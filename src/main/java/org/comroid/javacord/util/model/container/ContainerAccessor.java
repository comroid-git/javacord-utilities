package org.comroid.javacord.util.model.container;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ConstantConditions")
public
interface ContainerAccessor {
    boolean isNull();

    Object getRaw();

    String fallbackString();

    default @NotNull String stringValue() {
        return String.valueOf(getRaw());
    }

    default @NotNull String asString(String... fallback) {
        return new PrioritySupplier<>(false, "null")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join(" ", fallback))
                .butRather(this::stringValue)
                .get();
    }

    default boolean asBoolean(String... fallback) {
        return Boolean.parseBoolean(new PrioritySupplier<>(false, "false")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get());
    }

    default int asInt(String... fallback) {
        return Integer.parseInt(new PrioritySupplier<>(false, "0")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get());
    }

    default long asLong(String... fallback) {
        return Long.parseLong(new PrioritySupplier<>(false, "0")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get());
    }

    default float asFloat(String... fallback) {
        return Float.parseFloat(new PrioritySupplier<>(false, "0.0")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get());
    }

    default double asDouble(String... fallback) {
        return Double.parseDouble(new PrioritySupplier<>(false, "0.0")
                .butRather(this::fallbackString)
                .butRather(fallback.length == 0 ? null : String.join("", fallback))
                .butRather(this::stringValue)
                .get());
    }
}
