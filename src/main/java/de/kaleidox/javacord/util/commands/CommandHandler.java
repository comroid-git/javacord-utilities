package de.kaleidox.javacord.util.commands;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.kaleidox.javacord.util.server.properties.PropertyGroup;
import de.kaleidox.javacord.util.ui.embed.DefaultEmbedFactory;
import de.kaleidox.javacord.util.ui.messages.InformationMessage;
import de.kaleidox.javacord.util.ui.messages.RefreshableMessage;
import de.kaleidox.javacord.util.ui.messages.categorizing.CategorizedEmbed;
import de.kaleidox.javacord.util.ui.messages.paging.PagedEmbed;
import de.kaleidox.javacord.util.ui.messages.paging.PagedMessage;
import de.kaleidox.javacord.util.ui.reactions.InfoReaction;
import de.kaleidox.util.markers.Value;

import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
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
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.event.message.MessageEvent;
import org.javacord.core.util.logging.LoggerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.System.arraycopy;
import static org.javacord.api.util.logging.ExceptionLogger.get;

public final class CommandHandler {
    private static final Logger logger = LoggerUtil.getLogger(CommandHandler.class);
    static final String NO_GROUP = "@NoGroup#";

    private final DiscordApi api;
    private final Map<String, CommandRepresentation> commands = new ConcurrentHashMap<>();
    private final Map<Long, long[]> responseMap = new ConcurrentHashMap<>();

    public String[] prefixes;
    public boolean autoDeleteResponseOnCommandDeletion;
    public boolean useBotMentionAsPrefix;
    private Supplier<EmbedBuilder> embedSupplier = null;
    private @Nullable PropertyGroup customPrefixProperty;
    private long[] serverBlacklist;

    public CommandHandler(DiscordApi api) {
        this(api, false);
    }

    public CommandHandler(DiscordApi api, boolean handleMessageEdit) {
        this.api = api;

        prefixes = new String[]{"!"};
        autoDeleteResponseOnCommandDeletion = true;
        customPrefixProperty = null;
        serverBlacklist = new long[0];

        api.addMessageCreateListener(this::handleMessageCreate);
        if (handleMessageEdit)
            api.addMessageEditListener(this::handleMessageEdit);
        api.addMessageDeleteListener(this::handleMessageDelete);
    }

    public Set<CommandRepresentation> getCommands() {
        HashSet<CommandRepresentation> reps = new HashSet<>();

        commands.forEach((s, commandRep) -> reps.add(commandRep));

        return reps;
    }

    public void registerCommands(Object register) {
        if (register instanceof Class)
            extractCommandRep(null, ((Class) register).getMethods());
        else if (register instanceof Method)
            extractCommandRep(null, (Method) register);
        else extractCommandRep(register, register.getClass().getMethods());
    }

    public void unregisterCommands(Object unregister) {
        if (unregister instanceof Class)
            tryUnregister(((Class) unregister).getMethods());
        else if (unregister instanceof Method)
            tryUnregister((Method) unregister);
        else tryUnregister(unregister.getClass().getMethods());
    }

    public void useDefaultHelp(@Nullable Supplier<EmbedBuilder> embedSupplier) {
        this.embedSupplier = (embedSupplier == null ? DefaultEmbedFactory.INSTANCE : embedSupplier);
        registerCommands(this);
    }

    public void useCustomPrefixes(@NotNull PropertyGroup propertyGroup) {
        this.customPrefixProperty = Objects.requireNonNull(propertyGroup);
    }

    public long[] getServerBlacklist() {
        return serverBlacklist;
    }

    public void setServerBlacklist(long... serverIds) {
        this.serverBlacklist = serverIds;
    }

    @CommandGroup(name = "Basic Commands", description = "All commands for basic interaction with the bot")
    @Command(aliases = "help", usage = "help [command]", description = "Shows a list of commands and what they do.")
    public Object defaultHelpCommand(TextChannel channel, String[] args) {
        if (args.length == 0) {
            if (getCommands().stream()
                    .filter(cmd -> cmd.showInHelpCommand)
                    .filter(cmd -> !Objects.equals(cmd.groupName, "Basic Commands"))
                    .allMatch(cmd -> cmd.groupName == null)) {
                PagedEmbed embed = new PagedEmbed(channel, embedSupplier);
                getCommands().stream()
                        .filter(cmd -> cmd.showInHelpCommand)
                        .sorted(Comparator.<CommandRepresentation>comparingInt(cmd -> cmd.groupOrdinal)
                                .thenComparingInt(rep -> rep.ordinal))
                        .forEachOrdered(cmd -> embed.addField("__" + cmd.aliases[0] + "__: _" + prefixes[0]
                                + cmd.usage + "_", cmd.description));
                return embed;
            } else {
                CategorizedEmbed embed = new CategorizedEmbed(channel);

                getCommands().stream()
                        .filter(cmd -> cmd.showInHelpCommand)
                        .sorted(Comparator.<CommandRepresentation>comparingInt(cmd -> cmd.groupOrdinal)
                                .thenComparingInt(rep -> rep.ordinal))
                        .filter(cmd -> embed.getCategories()
                                .stream()
                                .noneMatch(cat -> cmd.groupName != null
                                        ? cat.getName().equals(cmd.groupName)
                                        : cat.getName().equals("Other commands")))
                        .forEachOrdered(cmd -> {
                            if (cmd.groupName != null)
                                embed.addCategory(cmd.groupName, cmd.groupDescription != null ? cmd.groupDescription : "");
                            else embed.addCategory("Other commands", "Ungrouped commands");
                        });

                getCommands().stream()
                        .filter(cmd -> cmd.showInHelpCommand)
                        .sorted(Comparator.<CommandRepresentation>comparingInt(cmd -> cmd.groupOrdinal)
                                .thenComparingInt(rep -> rep.ordinal))
                        .forEach(cmd -> embed.getCategories()
                                .stream()
                                .filter(cat -> cmd.groupName != null
                                        ? cat.getName().equals(cmd.groupName)
                                        : cat.getName().equals("Other commands"))
                                .findFirst()
                                .ifPresent(cat -> cat.addField("__" + cmd.aliases[0] + "__: _" + prefixes[0]
                                        + cmd.usage + "_", cmd.description)));

                return embed;
            }
        } else {
            EmbedBuilder embed = embedSupplier.get();
            Optional<CommandRepresentation> command = getCommands().stream()
                    .filter(cmd -> {
                        for (String alias : cmd.aliases)
                            if (alias.equalsIgnoreCase(args[0]))
                                return true;
                        return false;
                    }).findAny();

            if (command.isPresent()) {
                CommandRepresentation cmd = command.get();
                embed.addField("__" + cmd.aliases[0] + "__: _" + prefixes[0] + cmd.usage + "_", cmd.description);
            } else embed.addField(
                    "__Unknown Command__: _" + args[0] + "_",
                    "Type _\"" + prefixes[0] + "help\"_ for a list of commands."
            );

            return embed;
        }

    }

    private void extractCommandRep(@Nullable Object invocationTarget, Method... methods) {
        for (Method method : methods) {
            Command cmd = method.getAnnotation(Command.class);
            if (cmd == null) continue;

            CommandGroup group;
            if ((group = method.getAnnotation(CommandGroup.class)) == null) {
                Class<?> declaring = method.getDeclaringClass();
                if ((group = declaring.getAnnotation(CommandGroup.class)) == null) {
                    Class<?>[] supers = new Class[declaring.getInterfaces().length
                            + (declaring.getSuperclass() == Object.class ? 0 : 1)];
                    for (int i = 0; i < declaring.getInterfaces().length; i++) supers[i] = declaring.getInterfaces()[i];
                    if (declaring.getSuperclass() != Object.class)
                        supers[supers.length - 1] = declaring.getSuperclass();

                    int i = 0;
                    while (i < supers.length && (group = supers[i].getAnnotation(CommandGroup.class)) == null)
                        i++;
                }
            }

            if (!Modifier.isStatic(method.getModifiers()) && Objects.isNull(invocationTarget))
                throw new IllegalArgumentException("Invocation Target cannot be null on non-static methods!");
            if (Modifier.isAbstract(method.getModifiers()))
                throw new AbstractMethodError("Command annotated method cannot be abstract!");

            boolean hasErrored = false;
            if (!cmd.enableServerChat()
                    && cmd.requiredDiscordPermission() != PermissionType.SEND_MESSAGES) {
                logger.error("Command " + method.getName() + "(" + Arrays.stream(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", ")) + ")"
                        + ": Conflicting command properties; private-only commands cannot require permissions!");
                hasErrored = true;
            }
            if (!cmd.enableServerChat() && !cmd.enableServerChat()) {
                logger.error("Command " + method.getName() + "(" + Arrays.stream(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", ")) + ")"
                        + ": Conflicting command properties; command cannot disallow both private and server chat!");
                hasErrored = true;
            }

            if (hasErrored) continue;

            CommandRepresentation commandRep = new CommandRepresentation(method, cmd, group, invocationTarget);
            if (cmd.aliases().length > 0)
                for (String alias : cmd.aliases()) commands.put(alias, commandRep);
            else commands.put(method.getName(), commandRep);
            if (group == null)
                logger.info("Command " + (cmd.aliases().length == 0 ? method.getName() : cmd.aliases()[0])
                        + " was registered without a CommandGroup annotation!");
        }
    }

    private void tryUnregister(Method... methods) {
        for (Method method : methods)
            commands.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().method.equals(method))
                    .forEach(entry -> commands.remove(entry.getKey()));
    }

    private void handleMessageCreate(MessageCreateEvent event) {
        if (isBlacklisted(event)) return;

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
        if (isBlacklisted(event)) return;

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
        if (isBlacklisted(event)) return;

        if (autoDeleteResponseOnCommandDeletion) {
            long[] ids = responseMap.get(event.getMessageId());
            if (ids == null) return;
            api.getMessageById(
                    ids[0],
                    api.getChannelById(ids[1]).flatMap(Channel::asTextChannel).orElseThrow(AssertionError::new))
                    .thenCompose(Message::delete)
                    .exceptionally(get());
        }
    }

    private boolean isBlacklisted(MessageEvent event) {
        if (!event.getServer().isPresent()) return true;

        long id = event.getServer().get().getId();

        for (long blacklisted : serverBlacklist) if (id == blacklisted) return false;

        return false;
    }

    private void handleCommand(final Message message, final TextChannel channel, final Params commandParams) {
        String usedPrefix = extractUsedPrefix(message);

        // if no prefix was used, stop handling
        if (usedPrefix == null) return;

        String content = message.getContent();

        CommandRepresentation cmd;
        String[] split = splitContent(content);
        String[] args;
        if (usedPrefix.matches("^(.*\\s.*)+$")) {
            cmd = commands.get(split[1].toLowerCase());
            args = new String[split.length - 2];
            arraycopy(split, 2, args, 0, args.length);
        } else {
            cmd = commands.get(split[0].substring(usedPrefix.length()).toLowerCase());
            args = new String[split.length - 1];
            arraycopy(split, 1, args, 0, args.length);
        }

        if (cmd == null) return;
        commandParams.args = args;
        List<String> problems = new ArrayList<>();

        if (message.isPrivateMessage() && !cmd.enablePrivateChat)
            problems.add("This command can only be run in a server channel!");
        else if (!message.isPrivateMessage() && !cmd.enableServerChat)
            problems.add("This command can only be run in a private channel!");

        if (!message.getUserAuthor()
                .map(usr -> message.getChannel()
                        .asServerTextChannel()
                        .map(stc -> stc.hasAnyPermission(usr,
                                PermissionType.ADMINISTRATOR,
                                cmd.requiredDiscordPermission))
                        .orElse(true))
                .orElse(false))
            problems.add("You are missing the required permission: "
                    + cmd.requiredDiscordPermission.name() + "!");

        int reqArgs = cmd.requiredArguments;
        if (commandParams.args.length < reqArgs) problems.add("This command requires at least "
                + reqArgs + " argument" + (reqArgs == 1 ? "" : "s") + "!");

        int reqChlMent = cmd.requiredChannelMentions;
        if (message.getMentionedChannels().size() < reqChlMent) problems.add("This command requires at least "
                + reqChlMent + " channel mention" + (reqChlMent == 1 ? "" : "s") + "!");

        int reqUsrMent = cmd.requiredUserMentions;
        if (message.getMentionedUsers().size() < reqUsrMent) problems.add("This command requires at least "
                + reqUsrMent + " user mention" + (reqUsrMent == 1 ? "" : "s") + "!");

        int reqRleMent = cmd.requiredRoleMentions;
        if (message.getMentionedRoles().size() < reqRleMent) problems.add("This command requires at least "
                + reqRleMent + " role mention" + (reqRleMent == 1 ? "" : "s") + "!");

        if (cmd.runInNSFWChannelOnly
                && !channel.asServerTextChannel().map(ServerTextChannel::isNsfw).orElse(true))
            problems.add("This command can only run in an NSFW marked channel!");

        if (problems.size() > 0) {
            applyResponseDeletion(message.getId(), channel.sendMessage(DefaultEmbedFactory.create()
                    .setColor(Color.RED)
                    .setDescription(String.join("\n", problems)))
                    .exceptionally(get()));
            return;
        }

        if (cmd.async) api.getThreadPool()
                .getExecutorService()
                .submit(() -> doInvoke(cmd, commandParams, channel, message));
        else doInvoke(cmd, commandParams, channel, message);
    }

    private @Nullable String extractUsedPrefix(final Message message) {
        String content = message.getContent();
        int usedPrefix = -1;

        // gather all possible prefixes
        String[] prefs = new String[prefixes.length
                + (useBotMentionAsPrefix ? 2 : 0)
                + (customPrefixProperty != null ? 1 : 0)];
        arraycopy(prefixes, 0, prefs, 0, prefixes.length);
        if (useBotMentionAsPrefix) {
            prefs[prefixes.length] = api.getYourself().getMentionTag() + " ";
            prefs[prefixes.length + 1] = api.getYourself().getNicknameMentionTag() + " ";
        }
        message.getServer()
                .map(DiscordEntity::getId)
                .map(customPrefixProperty::getValue)
                .map(Value::asString)
                .ifPresent(val -> prefs[prefs.length - 1] = val);

        for (int i = 0; i < prefs.length; i++)
            if (content.toLowerCase().indexOf(prefs[i]) == 0) {
                usedPrefix = i;
                break;
            }

        return usedPrefix == -1 ? null : prefs[usedPrefix];
    }

    private String[] splitContent(String content) {
        List<String> yields = new ArrayList<>();
        yields.add("");

        boolean inString = false;
        int i = 0, y = 0, s = -1;
        char c = 0, p;

        while (i < content.length()) {
            p = c;
            c = content.charAt(i);

            switch (c) {
                case '"':
                    // if not in string & starter is not escaped
                    if (!inString && p != '\\') {
                        // if string starts after a space
                        if (p == ' ') {
                            // start string
                            yields.add("");
                            s = y++;
                            inString = true;
                        } else {
                            // escape "
                            yields.set(y, yields.get(y) + c);
                        }
                        // if in string & ender is not escaped
                    } else if (inString && p != '\\') {
                        // if there are more chars
                        if (content.length() < i + 1) {
                            // if next char is space
                            if (content.length() < i + 1 && content.charAt(i + 1) == ' ') {
                                // end string
                                yields.add("");
                                s = y++;
                                inString = false;
                            } else {
                                // escape "
                                yields.set(y, yields.get(y) + c);
                            }
                        } else {
                            // end string
                            inString = false;
                        }
                        // if " was escaped
                    } else {
                        // append to string
                        yields.set(y, yields.get(y) + c);
                    }
                    break;
                case ' ':
                    if (inString) {
                        // append to string
                        yields.set(y, yields.get(y) + c);
                    } else {
                        // start new item
                        yields.add("");
                        s = y++;
                    }
                    break;
                case '\\':
                    // never include escaping character
                    break;
                default:
                    // append to string
                    yields.set(y, yields.get(y) + c);
                    break;
            }

            i++;
        }

        if (inString) { // if still in string -> join split string together
            int prevSize = yields.size();
            new ArrayList<>(yields)
                    .stream()
                    .skip(s)
                    .flatMap(str -> Arrays.stream(str.split(" ")))
                    .forEachOrdered(yields::add);
            if (prevSize >= s + 1) yields.subList(s + 1, prevSize + 1).clear();
            yields.set(s + 1, '"' + yields.get(s + 1));
        }

        yields.removeIf(String::isEmpty);

        return yields.toArray(new String[0]);
    }

    private void doInvoke(CommandRepresentation commandRep, Params commandParams, TextChannel channel, Message message) {
        Object reply;
        try {
            reply = invoke(commandRep.method, commandParams, commandRep.invocationTarget);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access command method!", e);
        } catch (InvocationTargetException e) {
            new InfoReaction(message, "⚠", "Command threw an exception: ["
                    + e.getCause().getClass().getSimpleName() + "] " + e.getCause().getMessage(),
                    1, TimeUnit.MINUTES, () -> DefaultEmbedFactory.create().setColor(Color.RED));
            logger.catching(e.getCause());
            return;
        }

        if (reply != null) {
            CompletableFuture<Message> msgFut = null;

            if (reply instanceof EmbedBuilder) msgFut = channel.sendMessage((EmbedBuilder) reply);
            else if (reply instanceof MessageBuilder) msgFut = ((MessageBuilder) reply).send(channel);
            else if (reply instanceof InformationMessage) ((InformationMessage) reply).refresh();
            else if (reply instanceof PagedEmbed) msgFut = ((PagedEmbed) reply).build();
            else if (reply instanceof PagedMessage) ((PagedMessage) reply).refresh();
            else if (reply instanceof RefreshableMessage) ((RefreshableMessage) reply).refresh();
            else if (reply instanceof CategorizedEmbed) msgFut = ((CategorizedEmbed) reply).build();
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
            else if (User.class.isAssignableFrom(klasse))
                args[i] = param.author.asUser().orElse(null);
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
