package de.kaleidox.javacord.util.model;

import de.kaleidox.util.markers.Value;

import org.jetbrains.annotations.NotNull;

public interface SelfDefaultable<Self extends SelfDefaultable, T> {
    Self withDefaultValue(@NotNull T value);

    Value getDefaultValue();
}
