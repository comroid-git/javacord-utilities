package de.kaleidox.javacord.util.commands;

import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;

public class CommandRepresentation {
    public final Method method;
    public final Command cmd;
    public @Nullable final CommandGroup group;
    public @Nullable final Object invocationTarget;

    CommandRepresentation(
            Method method,
            Command cmd,
            @Nullable CommandGroup group,
            @Nullable Object invocationTarget
    ) {
        this.method = method;
        this.cmd = cmd;
        this.group = group;
        this.invocationTarget = invocationTarget;
    }
}
