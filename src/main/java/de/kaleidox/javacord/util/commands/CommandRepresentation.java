package de.kaleidox.javacord.util.commands;

import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;

public class CommandRepresentation {
    public final Method method;
    public final Command annotation;
    @Nullable public final Object invocationTarget;

    CommandRepresentation(Method method, Command annotation, @Nullable Object invocationTarget) {
        this.method = method;
        this.annotation = annotation;
        this.invocationTarget = invocationTarget;
    }
}
