package org.comroid.javacord.util.model.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class PrioritySupplier<T> implements Supplier<T> {
    private final List<Supplier<T>> suppliers = new ArrayList<>(1);
    private final boolean nullable;
    private final FixedValueSupplier fallback;

    public PrioritySupplier(boolean nullable, T fallback) {
        if (!nullable && fallback == null)
            throw new NullPointerException("Fallback cannot be null for non nullable PrioritySupplier!");

        this.nullable = nullable;
        this.fallback = new FixedValueSupplier(fallback);

        suppliers.add(this.fallback);
    }

    public PrioritySupplier<T> butRather(@Nullable T value) {
        return butRather(new FixedValueSupplier(value));
    }

    public PrioritySupplier<T> butRather(@Nullable Supplier<T> supplier) {
        if (supplier == null)
            return butRather((T) null);

        suppliers.add(supplier);

        return this;
    }

    @Contract("true -> _; false -> !null")
    public @Nullable T get(boolean nullable) {
        return get(null, nullable);
    }

    @Contract("_, true -> _; _, false -> !null")
    public @Nullable T get(@Nullable Predicate<T> filter, boolean nullable) {
        final List<Supplier<T>> suppliers = new ArrayList<>(this.suppliers);
        Collections.reverse(suppliers);

        T yield = fallback.value;

        for (Supplier<T> supplier : suppliers) {
            try {
                if (((yield = supplier.get()) == null && !nullable) | (filter != null && !filter.test(yield))) {
                    yield = fallback.value; // reset
                    continue;
                }

                break;
            } catch (Exception ignored) {
                // try another supplier
                yield = fallback.value; // reset
            }
        }

        return yield;
    }

    @Override
    public @Nullable T get() {
        return get(this.nullable);
    }

    private class FixedValueSupplier implements Supplier<T> {
        private final @Nullable T value;

        public FixedValueSupplier(@Nullable T value) {
            this.value = value;
        }

        @Override
        public @Nullable T get() {
            return value;
        }
    }
}
