package org.comroid.javacord.util.model.command;

import org.comroid.javacord.util.server.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public interface SelfCommandChannelable<Self extends SelfCommandChannelable> {
    Optional<Function<Long, Long>> getCommandChannelProvider();

    Self withCommandChannelProvider(@NotNull Function<Long, Long> commandChannelProvider);

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    default Self removeCommandChannelProvider() {
        withCommandChannelProvider((Function) null);
        return (Self) this;
    }

    // Extensions
    default Self withCommandChannelProvider(@NotNull Property commandChannelPropertyGroup) {
        return withCommandChannelProvider(commandChannelPropertyGroup.function(Long.class));
    }
}
