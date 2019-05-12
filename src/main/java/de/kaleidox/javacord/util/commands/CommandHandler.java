package de.kaleidox.javacord.util.commands;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.kaleidox.javacord.util.embed.DefaultEmbedFactory;
import de.kaleidox.javacord.util.server.properties.PropertyGroup;
import de.kaleidox.javacord.util.ui.messages.InformationMessage;
import de.kaleidox.javacord.util.ui.messages.PagedEmbed;
import de.kaleidox.javacord.util.ui.messages.PagedMessage;
import de.kaleidox.javacord.util.ui.messages.RefreshableMessage;

import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.core.util.logging.LoggerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.javacord.api.util.logging.ExceptionLogger.get;

public final class CommandHandler {
    private static final Logger logger = LoggerUtil.getLogger(CommandHandler.class);

    private final DiscordApi api;
    private final ConcurrentHashMap<String, CommandRep> commands = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, long[]> responseMap = new ConcurrentHashMap<>();

    public String[] prefixes;
    public boolean autoDeleteResponseOnCommandDeletion;
    private PropertyGroup authMethodProperty = null;
    private Supplier<EmbedBuilder> embedSupplier = null;
    @Nullable private PropertyGroup customPrefixProperty;
    private boolean exclusiveCustomPrefix;

    public CommandHandler(DiscordApi api) {
        this.api = api;

        prefixes = new String[]{"!"};
        autoDeleteResponseOnCommandDeletion = true;
        customPrefixProperty = null;
        exclusiveCustomPrefix = false;

        api.addMessageCreateListener(this::handleMessageCreate);
        api.addMessageEditListener(this::handleMessageEdit);
        api.addMessageDeleteListener(this::handleMessageDelete);
    }

    public Set<CommandRep> getCommands() {
        HashSet<CommandRep> reps = new HashSet<>();

        commands.forEach((s, commandRep) -> reps.add(commandRep));

        return reps;
    }

    public void useDefaultHelp(@Nullable Supplier<EmbedBuilder> embedSupplier) {
        this.embedSupplier = (embedSupplier == null ? DefaultEmbedFactory.INSTANCE : embedSupplier);
        registerCommands(this);
    }

    public void useCustomPrefixes(@NotNull PropertyGroup propertyGroup, boolean exclusiveCustomPrefix) {
        this.customPrefixProperty = propertyGroup;
        this.exclusiveCustomPrefix = exclusiveCustomPrefix;
    }

    public void registerCommands(Object register) {
        if (register instanceof Class)
            extractCommandRep(null, ((Class) register).getMethods());
        else if (register instanceof Method)
            extractCommandRep(null, (Method) register);
        else extractCommandRep(register, register.getClass().getMethods());
    }

    public void useAuthManager(PropertyGroup authMethodProperty) {
        this.authMethodProperty = authMethodProperty;
    }

    @Command(aliases = "help", usage = "help [command]", description = "Shows a list of commands and what they do.")
    public Object defaultHelpCommand(Command.Parameters param) {
        if (param.getArguments().length == 0) {
            PagedEmbed embed = new PagedEmbed(param.getTextChannel(), embedSupplier);

            getCommands().forEach(commandRep -> {
                Command cmd = commandRep.annotation;
                String[] aliases = cmd.aliases();
                if (aliases.length == 0) aliases = new String[]{commandRep.method.getName()};
                embed.addField("__" + aliases[0] + "__: _" + prefixes[0] + cmd.usage() + "_", cmd.description());
            });

            return embed;
        } else if (param.getArguments().length >= 1) {
            EmbedBuilder embed = embedSupplier.get();

            Optional<CommandRep> command = getCommands().stream()
                    .filter(cmd -> {
                        for (String alias : cmd.annotation.aliases())
                            if (alias.equalsIgnoreCase(param.getArguments()[0]))
                                return true;
                        return false;
                    }).findAny();

            if (command.isPresent()) {
                Command cmd = command.get().annotation;
                String[] aliases = cmd.aliases();
                if (aliases.length == 0) aliases = new String[]{command.get().method.getName()};
                embed.addField("__" + aliases[0] + "__: _" + prefixes[0] + cmd.usage() + "_", cmd.description());
            } else embed.addField(
                    "__Unknown Command__: _" + param.getArguments()[0] + "_",
                    "Type _\"" + prefixes[0] + "help\"_ for a list of commands."
            );

            return embed;
        }

        throw new AssertionError();
    }

    private void extractCommandRep(@Nullable Object invocationTarget, Method... methods) {
        for (Method method : methods) {
            Command annotation = method.getAnnotation(Command.class);
            if (annotation == null) continue;

            if (!Modifier.isStatic(method.getModifiers()) && Objects.isNull(invocationTarget))
                throw new IllegalArgumentException("Invocation Target cannot be null on non-static methods!");
            if (Modifier.isAbstract(method.getModifiers()))
                throw new AbstractMethodError("Command annotated method cannot be abstract!");

            boolean hasErrored = false;
            if (!annotation.enableServerChat()
                    && annotation.requiredDiscordPermission() != PermissionType.SEND_MESSAGES) {
                logger.error("Command " + method.getName() + "(" + Arrays.stream(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", ")) + ")"
                        + ": Conflicting command properties; private-only commands cannot require permissions!");
                hasErrored = true;
            }
            if (!annotation.enableServerChat() && !annotation.enableServerChat()) {
                logger.error("Command " + method.getName() + "(" + Arrays.stream(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", ")) + ")"
                        + ": Conflicting command properties; command cannot disallow both private and server chat!");
                hasErrored = true;
            }

            if (hasErrored) continue;

            CommandRep commandRep = new CommandRep(method, annotation, invocationTarget);
            if (annotation.aliases().length > 0)
                for (String alias : annotation.aliases()) commands.put(alias, commandRep);
            else commands.put(method.getName(), commandRep);
        }
    }

    private void handleMessageCreate(MessageCreateEvent event) {
        Params params = new Params(
                api,
                event,
                null,
                event.getServer().orElse(null),
                event.getChannel(),
                event.getMessage(),
                event.getMessageAuthor()
        );

        handleCommand(params.message, event.getChannel(), params);
    }

    private void handleMessageEdit(MessageEditEvent event) {
        Params params = new Params(
                api,
                null,
                event,
                event.getServer().orElse(null),
                event.getChannel(),
                event.getMessage().orElseGet(() -> event.requestMessage().join()),
                event.getMessageAuthor().orElse(null)
        );

        handleCommand(params.message, event.getChannel(), params);
    }

    private void handleMessageDelete(MessageDeleteEvent event) {
        if (autoDeleteResponseOnCommandDeletion) {
            long[] ids = responseMap.get(event.getMessageId());
            api.getMessageById(
                    ids[0],
                    api.getChannelById(ids[1]).flatMap(Channel::asTextChannel).orElseThrow(AssertionError::new))
                    .thenCompose(Message::delete)
                    .exceptionally(get());
        }
    }

    private void handleCommand(final Message message, final TextChannel channel, final Params commandParams) {
        String content = message.getContent();
        int usedPrefix = -1;
        String[] pref = null;

        if (!message.isPrivateMessage() && customPrefixProperty != null) {
            @SuppressWarnings("OptionalGetWithoutIsPresent") Server server = message.getServer().get();
            if (exclusiveCustomPrefix) {
                if (content.indexOf(customPrefixProperty.getValue(server.getId()).asString()) == 0)
                    usedPrefix = Integer.MAX_VALUE;
            } else {
                pref = new String[prefixes.length + 1];
                System.arraycopy(prefixes, 0, pref, 0, prefixes.length);
                pref[pref.length - 1] = customPrefixProperty.getValue(server.getId()).asString();

                for (int i = 0; i < pref.length; i++)
                    if (content.indexOf(pref[i]) == 0)
                        usedPrefix = i;
            }
        } else {
            switch (prefixes.length) {
                case 1:
                    if (content.indexOf(prefixes[0]) == 0)
                        usedPrefix = 0;
                    break;
                default:
                    for (int i = 0; i < prefixes.length; i++)
                        if (content.indexOf(prefixes[i]) == 0)
                            usedPrefix = i;
                    break;
                case 0:
                    return;
            }
        }

        if (pref == null && usedPrefix < prefixes.length) pref = prefixes;
        if (usedPrefix == -1 || pref == null) return;

        CommandRep commandRep;
        String[] split = content.split("[\\s&&[^\\n]]++");
        String[] args;
        if (pref[usedPrefix].matches("^(.*\\s.*)+$")) {
            commandRep = commands.get(split[1]);
            args = new String[split.length - 2];
            System.arraycopy(split, 2, args, 0, args.length);
        } else {
            commandRep = commands.get(split[0].substring(pref[usedPrefix].length()));
            args = new String[split.length - 1];
            System.arraycopy(split, 1, args, 0, args.length);
        }

        if (commandRep == null) return;
        commandParams.args = args;
        List<String> problems = new ArrayList<>();

        if (message.isPrivateMessage() && !commandRep.annotation.enablePrivateChat())
            problems.add("This command can only be run in a server channel!");
        else if (!message.isPrivateMessage() && !commandRep.annotation.enableServerChat())
            problems.add("This command can only be run in a private channel!");

        switch (authMethodProperty == null ? "discord_permission" : commandParams.getServer()
                .map(server -> authMethodProperty.getValue(server).asString())
                .orElse("discord_permission")) {
            case "allow_all":
                break;
            case "discord_permission":
                if (!message.getUserAuthor()
                        .map(usr -> message.getChannel()
                                .asServerTextChannel()
                                .map(stc -> stc.hasAnyPermission(usr,
                                        PermissionType.ADMINISTRATOR,
                                        commandRep.annotation.requiredDiscordPermission()))
                                .orElse(true))
                        .orElse(false))
                    problems.add("You are missing the required permission: "
                            + commandRep.annotation.requiredDiscordPermission().name() + "!");
                break;
            default:
                throw new AssertionError("Unreachable statement reached");
        }

        int reqChlMent = commandRep.annotation.requiredChannelMentions();
        if (message.getMentionedChannels().size() < reqChlMent) problems.add("This command requires at least "
                + reqChlMent + " channel mention" + (reqChlMent == 1 ? "" : "s") + "!");

        int reqUsrMent = commandRep.annotation.requiredUserMentions();
        if (message.getMentionedUsers().size() < reqUsrMent) problems.add("This command requires at least "
                + reqUsrMent + " user mention" + (reqUsrMent == 1 ? "" : "s") + "!");

        int reqRleMent = commandRep.annotation.requiredRoleMentions();
        if (message.getMentionedRoles().size() < reqRleMent) problems.add("This command requires at least "
                + reqRleMent + " role mention" + (reqRleMent == 1 ? "" : "s") + "!");

        if (commandRep.annotation.runInNSFWChannelOnly()
                && !channel.asServerTextChannel().map(ServerTextChannel::isNsfw).orElse(true))
            problems.add("This command can only run in an NSFW marked channel!");

        if (problems.size() > 0) {
            applyResponseDeletion(message.getId(), channel.sendMessage(DefaultEmbedFactory.create()
                    .setColor(Color.RED)
                    .setDescription(String.join("\n", problems)))
                    .exceptionally(get()));
            return;
        }

        if (commandRep.annotation.async()) api.getThreadPool()
                .getExecutorService()
                .submit(() -> doInvoke(commandRep, commandParams, channel, message));
        else doInvoke(commandRep, commandParams, channel, message);
    }

    private void doInvoke(CommandRep commandRep, Params commandParams, TextChannel channel, Message message) {
        Object reply;
        try {
            reply = invoke(commandRep.method, commandParams, commandRep.invocationTarget);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access command method!", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Command method threw an Exception!", e);
        }

        if (reply != null) {
            CompletableFuture<Message> msgFut = null;

            if (reply instanceof EmbedBuilder) msgFut = channel.sendMessage((EmbedBuilder) reply);
            else if (reply instanceof MessageBuilder) msgFut = ((MessageBuilder) reply).send(channel);
            else if (reply instanceof InformationMessage) ((InformationMessage) reply).refresh();
            else if (reply instanceof PagedEmbed) msgFut = ((PagedEmbed) reply).build();
            else if (reply instanceof PagedMessage) ((PagedMessage) reply).refresh();
            else if (reply instanceof RefreshableMessage) ((RefreshableMessage) reply).refresh();
            else msgFut = channel.sendMessage(String.valueOf(reply));

            if (msgFut != null)
                applyResponseDeletion(message.getId(), msgFut.exceptionally(get()));
        }
    }

    private Object invoke(Method method, Params param, @Nullable Object invocationTarget)
            throws InvocationTargetException, IllegalAccessException {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> klasse = parameterTypes[i];

            if (DiscordApi.class.isAssignableFrom(klasse))
                args[i] = param.discord;
            else if (MessageCreateEvent.class.isAssignableFrom(klasse))
                args[i] = param.createEvent;
            else if (MessageEditEvent.class.isAssignableFrom(klasse))
                args[i] = param.editEvent;
            else if (Server.class.isAssignableFrom(klasse))
                args[i] = param.server;
            else if (Boolean.class.isAssignableFrom(klasse))
                args[i] = param.message.isPrivateMessage();
            else if (TextChannel.class.isAssignableFrom(klasse)) {
                if (ServerTextChannel.class.isAssignableFrom(klasse))
                    args[i] = param.textChannel.asServerTextChannel().orElse(null);
                else if (PrivateChannel.class.isAssignableFrom(klasse))
                    args[i] = param.textChannel.asPrivateChannel().orElse(null);
                else args[i] = param.textChannel;
            } else if (Message.class.isAssignableFrom(klasse))
                args[i] = param.message;
            else if (MessageAuthor.class.isAssignableFrom(klasse))
                args[i] = param.author;
            else if (String[].class.isAssignableFrom(klasse))
                args[i] = param.args;
            else if (Command.Parameters.class.isAssignableFrom(klasse))
                args[i] = param;
            else args[i] = null;
        }

        return method.invoke(invocationTarget, args);
    }

    private void applyResponseDeletion(long cmdMsgId, CompletableFuture<Message> message) {
        message.thenAcceptAsync(msg -> {
            if (autoDeleteResponseOnCommandDeletion)
                responseMap.put(cmdMsgId, new long[]{msg.getId(), msg.getChannel().getId()});
        });
    }

    public class CommandRep {
        public final Method method;
        public final Command annotation;
        @Nullable public final Object invocationTarget;

        private CommandRep(Method method, Command annotation, @Nullable Object invocationTarget) {
            this.method = method;
            this.annotation = annotation;
            this.invocationTarget = invocationTarget;
        }
    }

    private class Params implements Command.Parameters {
        private final DiscordApi discord;
        @Nullable private final MessageCreateEvent createEvent;
        @Nullable private final MessageEditEvent editEvent;
        @Nullable private final Server server;
        private final TextChannel textChannel;
        private final Message message;
        private final MessageAuthor author;
        private String[] args;

        private Params(
                DiscordApi discord,
                @Nullable MessageCreateEvent createEvent,
                @Nullable MessageEditEvent editEvent,
                @Nullable Server server,
                TextChannel textChannel,
                Message message,
                @Nullable MessageAuthor author
        ) {
            this.discord = discord;
            this.createEvent = createEvent;
            this.editEvent = editEvent;
            this.server = server;
            this.textChannel = textChannel;
            this.message = message;
            this.author = author;
        }

        @Override
        public DiscordApi getDiscord() {
            return discord;
        }

        @Override
        public Optional<MessageCreateEvent> getMessageCreateEvent() {
            return Optional.ofNullable(createEvent);
        }

        @Override
        public Optional<MessageEditEvent> getMessageEditEvent() {
            return Optional.ofNullable(editEvent);
        }

        @Override
        public Optional<Server> getServer() {
            return Optional.ofNullable(server);
        }

        @Override
        public TextChannel getTextChannel() {
            return textChannel;
        }

        @Override
        public Message getCommandMessage() {
            return message;
        }

        @Override
        public Optional<MessageAuthor> getCommandExecutor() {
            return Optional.ofNullable(author);
        }

        @Override
        public String[] getArguments() {
            return args;
        }
    }
}
