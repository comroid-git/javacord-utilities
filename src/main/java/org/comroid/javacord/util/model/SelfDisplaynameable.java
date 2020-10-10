package org.comroid.javacord.util.model;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SelfDisplaynameable<Self extends SelfDisplaynameable> {
    Optional<String> getDisplayName();

    @SuppressWarnings("NullableProblems")
    Self withDisplayName(@NotNull String name);

    @SuppressWarnings("ConstantConditions")
    default Self removeDisplayName() {
        return withDisplayName(null);
    }
}
