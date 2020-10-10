package org.comroid.javacord.util.model.command;

import org.comroid.javacord.util.commands.CommandRepresentation;

import java.util.Collection;

public interface SelfMultiCommandRegisterable<Self extends SelfMultiCommandRegisterable> {
    Collection<CommandRepresentation> getCommands();

    Self registerCommandTarget(Object target);

    Self unregisterCommandTarget(Object target);

    // Extensions
    @SuppressWarnings("unchecked")
    default Self registerCommands(Object... targets) {
        for (Object target : targets)
            registerCommandTarget(target);
        return (Self) this;
    }

    @SuppressWarnings("unchecked")
    default Self unregisterCommands(Object... targets) {
        for (Object target : targets)
            unregisterCommandTarget(target);
        return (Self) this;
    }
}
