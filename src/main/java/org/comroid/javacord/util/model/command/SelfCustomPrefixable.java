package org.comroid.javacord.util.model.command;

import org.comroid.javacord.util.server.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public interface SelfCustomPrefixable<Self extends SelfCustomPrefixable> {
    Optional<Function<Long, String>> getCustomPrefixProvider();

    Self withCustomPrefixProvider(@NotNull Function<Long, String> customPrefixProvider);

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    default Self removeCustomPrefixProvider() {
        withCustomPrefixProvider((Function) null);
        return (Self) this;
    }

    // Extensions
    default Self withCustomPrefixProvider(@NotNull Property customPrefixPropertyGroup) {
        return withCustomPrefixProvider(customPrefixPropertyGroup.function(String.class));
    }
}
