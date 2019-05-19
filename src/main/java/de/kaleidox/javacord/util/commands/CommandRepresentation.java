package de.kaleidox.javacord.util.commands;

import java.lang.reflect.Method;

import org.javacord.api.entity.permission.PermissionType;
import org.jetbrains.annotations.Nullable;

public final class CommandRepresentation {
    public final Method method;
    public final String[] aliases;
    public final String description;
    public final String usage;
    public final int ordinal;
    public final boolean showInHelpCommand;
    public final boolean enablePrivateChat;
    public final boolean enableServerChat;
    public final PermissionType requiredDiscordPermission;
    public final int requiredChannelMentions;
    public final int requiredUserMentions;
    public final int requiredRoleMentions;
    public final boolean runInNSFWChannelOnly;
    public final boolean async;
    public final String groupName;
    public final String groupDescription;
    public final int groupOrdinal;
    public @Nullable final Object invocationTarget;

    @Override
    public String toString() {
        return method.getName();
    }

    @SuppressWarnings("StringEquality")
    CommandRepresentation(
            Method method,
            Command cmd,
            @Nullable CommandGroup group,
            @Nullable Object invocationTarget
    ) {
        this.method = method;
        this.invocationTarget = invocationTarget;

        this.aliases = (cmd.aliases().length == 0 ? new String[]{method.getName()} : cmd.aliases());
        this.description = cmd.description();
        this.usage = cmd.usage();
        this.ordinal = cmd.ordinal();
        this.showInHelpCommand = cmd.shownInHelpCommand();
        this.enablePrivateChat = cmd.enablePrivateChat();
        this.enableServerChat = cmd.enableServerChat();
        this.requiredDiscordPermission = cmd.requiredDiscordPermission();
        this.requiredChannelMentions = cmd.requiredChannelMentions();
        this.requiredUserMentions = cmd.requiredUserMentions();
        this.requiredRoleMentions = cmd.requiredRoleMentions();
        this.runInNSFWChannelOnly = cmd.runInNSFWChannelOnly();
        this.async = cmd.async();

        if (group != null) {
            if (group.name() != CommandHandler.NO_GROUP) groupName = group.name();
            else groupName = method.getDeclaringClass().getSimpleName();

            if (group.description() != CommandHandler.NO_GROUP) groupDescription = group.description();
            else groupDescription = "No group description provided.";

            groupOrdinal = group.ordinal();
        } else {
            groupName = null;
            groupDescription = null;
            groupOrdinal = Integer.MAX_VALUE;
        }
    }

    @SuppressWarnings("StringEquality")
    CommandRepresentation(
            Method method,
            Command cmd,
            String groupName,
            String groupDescription,
            int groupOrdinal,
            @Nullable Object invocationTarget
    ) {
        this.method = method;
        this.invocationTarget = invocationTarget;

        this.aliases = (cmd.aliases().length == 0 ? new String[]{method.getName()} : cmd.aliases());
        this.description = cmd.description();
        this.usage = cmd.usage();
        this.ordinal = cmd.ordinal();
        this.showInHelpCommand = cmd.shownInHelpCommand();
        this.enablePrivateChat = cmd.enablePrivateChat();
        this.enableServerChat = cmd.enableServerChat();
        this.requiredDiscordPermission = cmd.requiredDiscordPermission();
        this.requiredChannelMentions = cmd.requiredChannelMentions();
        this.requiredUserMentions = cmd.requiredUserMentions();
        this.requiredRoleMentions = cmd.requiredRoleMentions();
        this.runInNSFWChannelOnly = cmd.runInNSFWChannelOnly();
        this.async = cmd.async();

        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupOrdinal = groupOrdinal;
    }
}
