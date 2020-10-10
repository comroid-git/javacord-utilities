package org.comroid.javacord.util.model;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SelfDescribable<Self extends SelfDescribable> {
    String NO_DESCRIPTION = "No description provided.";

    Optional<String> getDescription();

    @SuppressWarnings("NullableProblems")
    Self withDescription(@NotNull String description);

    @SuppressWarnings("ConstantConditions")
    default Self removeDescription() {
        return withDescription(null);
    }
}
