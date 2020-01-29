package org.comroid.javacord.util.model;

import org.comroid.javacord.util.model.container.ValueContainer;

import org.jetbrains.annotations.NotNull;

public interface SelfDefaultable<Self extends SelfDefaultable, T> {
    Self withDefaultValue(@NotNull T value);

    ValueContainer getDefaultValue();
}
