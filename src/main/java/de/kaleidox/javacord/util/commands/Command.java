package de.kaleidox.javacord.util.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    /**
     * Defines aliases for the command. Must not contain {@code null}.
     *
     * @return The aliases of the command.
     */
    String[] aliases() default {};

    /**
     * Defines the description that is shown in the default help command.
     *
     * @return The description of the command.
     */
    String description() default "No description provided.";

    String usage() default "No usage provided.";

    /**
     * Defines whether the command is shown in the default help command. Default value is {@code TRUE}.
     *
     * @return Whether to list this command in the default help command.
     */
    boolean shownInHelpCommand() default true;

    /**
     * Defines whether this command should be usable in a private chat. Default value is {@code true}.
     *
     * @return Whether this command is available in private chat.
     */
    boolean enablePrivateChat() default true;

    /**
     * Defines whether this command should be usable in a server chat. Default value is {@code true}.
     *
     * @return Whether this command is available in server chat.
     */
    boolean enableServerChat() default true;

    /**
     * Defines a permission that the user is required to have be in the executing context; e.g. the ServerTextChannel.
     * Default value is {@link PermissionType#SEND_MESSAGES}.
     *
     * @return The required permission to execute this command.
     */
    PermissionType requiredDiscordPermission() default PermissionType.SEND_MESSAGES;

    /**
     * Defines the minimum amount of channel mentions required for the command to run. Default value is {@code 0}.
     *
     * @return The minimum required amount of channel mentions.
     */
    int requiredChannelMentions() default 0;

    /**
     * Defines the minimum amount of user mentions required for the command to run. Default value is {@code 0}.
     *
     * @return The minimum required amount of user mentions.
     */
    int requiredUserMentions() default 0;

    /**
     * Defines the minimum amount of role mentions required for the command to run. Default value is {@code 0}.
     *
     * @return The minimum required amount of role mentions.
     */
    int requiredRoleMentions() default 0;

    /**
     * Defines whether a command should only be usable in an NSFW channel.
     *
     * @return Whether to run this command in NSFW channels only.
     */
    boolean runInNSFWChannelOnly() default false;

    /**
     * Command parameter structure
     */
    interface Parameters {
        DiscordApi getDiscord();

        Optional<MessageCreateEvent> getMessageCreateEvent();

        Optional<MessageEditEvent> getMessageEditEvent();

        Optional<Server> getServer();

        default boolean isPrivate() {
            return !getServer().isPresent();
        }

        TextChannel getTextChannel();

        default Optional<ServerTextChannel> getServerTextChannel() {
            return getTextChannel().asServerTextChannel();
        }

        default Optional<PrivateChannel> getPrivateChannel() {
            return getTextChannel().asPrivateChannel();
        }

        Message getCommandMessage();

        default List<ServerTextChannel> getChannelMentions() {
            return getCommandMessage().getMentionedChannels();
        }

        default List<User> getUserMentions() {
            return getCommandMessage().getMentionedUsers();
        }

        default List<Role> getRoleMentions() {
            return getCommandMessage().getMentionedRoles();
        }

        Optional<MessageAuthor> getCommandExecutor();

        String[] getArguments();
    }
}
