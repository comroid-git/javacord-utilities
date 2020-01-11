// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands;

import java.util.function.BiConsumer;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.user.User;
import java.util.Optional;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import de.kaleidox.util.commands.baseCommands.Help;
import de.kaleidox.util.Bot;
import de.kaleidox.util.objects.successstate.Type;
import de.kaleidox.util.objects.Auth;
import de.kaleidox.util.objects.serverpreferences.Preference;
import java.util.function.Function;
import org.javacord.api.entity.server.Server;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import de.kaleidox.util.objects.successstate.SuccessState;
import org.javacord.api.event.message.MessageCreateEvent;
import java.util.Collection;
import java.util.Set;
import de.kaleidox.util.objects.serverpreferences.ServerPreferences;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import de.kaleidox.util.tools.Debugger;
import java.util.HashSet;
import java.util.List;

public abstract class CommandBase
{
    public static List<CommandBase> commands;
    public static HashSet<String> KEYWORDS;
    private static Debugger log;
    private static OtherwiseRunner otherwise;
    public String[] keywords;
    public boolean requiresAuth;
    public boolean canRunPrivately;
    public boolean canIgnorePrivateChannel;
    public int[] serverParameterRange;
    public CommandGroup group;
    public EmbedBuilder helpEmbed;
    
    public CommandBase(final String keyword, final boolean requiresAuth, final boolean canRunPrivately, final int[] parameterRange, final CommandGroup group, final EmbedBuilder helpEmbed) {
        this(new String[] { keyword }, requiresAuth, canRunPrivately, false, parameterRange, group, helpEmbed);
    }
    
    public CommandBase(final String[] keywords, final boolean requiresAuth, final boolean canRunPrivately, final int[] parameterRange, final CommandGroup group, final EmbedBuilder helpEmbed) {
        this(keywords, requiresAuth, canRunPrivately, false, parameterRange, group, helpEmbed);
    }
    
    public CommandBase(final String keyword, final boolean requiresAuth, final boolean canRunPrivately, final boolean canIgnoreCommandChannel, final int[] parameterRange, final CommandGroup group, final EmbedBuilder helpEmbed) {
        this(new String[] { keyword }, requiresAuth, canRunPrivately, canIgnoreCommandChannel, parameterRange, group, helpEmbed);
    }
    
    public CommandBase(final String[] keywords, final boolean requiresAuth, final boolean canRunPrivately, final boolean canIgnoreCommandChannel, final int[] parameterRange, final CommandGroup group, final EmbedBuilder helpEmbed) {
        this.keywords = keywords;
        this.requiresAuth = requiresAuth;
        this.canRunPrivately = canRunPrivately;
        this.canIgnorePrivateChannel = canIgnoreCommandChannel;
        this.serverParameterRange = parameterRange;
        this.group = group;
        this.helpEmbed = helpEmbed;
    }
    
    public static void init() {
        CommandBase.KEYWORDS.add("kalbots");
        CommandBase.KEYWORDS.add("kaleibots");
        CommandBase.KEYWORDS.add("kaleidoxbots");
        final boolean hasPreferences;
        final boolean isPreferences;
        final ReflectiveOperationException ex;
        ReflectiveOperationException e;
        new Reflections(CommandBase.class.getPackage().getName(), new Scanner[0]).getSubTypesOf((Class)CommandBase.class).forEach(cl -> {
            hasPreferences = (ServerPreferences.getEntries().size() > 0);
            isPreferences = cl.getSimpleName().equals("Preferences");
            try {
                if (!isPreferences || hasPreferences) {
                    CommandBase.commands.add(cl.newInstance());
                }
            }
            catch (InstantiationException | IllegalAccessException ex2) {
                e = ex;
                CommandBase.log.put(e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public static void addKeywords(final Set<String> keywords) {
        CommandBase.KEYWORDS.addAll((Collection<?>)keywords);
    }
    
    public static OtherwiseRunner register(final Package commandPackage) {
        final Reflections botCommands = new Reflections(commandPackage.getName(), new Scanner[0]);
        final ReflectiveOperationException ex;
        ReflectiveOperationException e;
        botCommands.getSubTypesOf((Class)CommandBase.class).forEach(cl -> {
            try {
                CommandBase.commands.add(cl.newInstance());
            }
            catch (InstantiationException | IllegalAccessException ex2) {
                e = ex;
                CommandBase.log.put(e.getMessage());
                e.printStackTrace();
            }
            return;
        });
        return CommandBase.otherwise;
    }
    
    public static OtherwiseRunner register(final CommandBase command) {
        CommandBase.commands.add(command);
        return CommandBase.otherwise;
    }
    
    public static void process(final MessageCreateEvent event) {
        final SuccessState state = new SuccessState();
        final Message msg = event.getMessage();
        final TextChannel chl = msg.getChannel();
        final Message message;
        final List<Object> parts;
        final List<String> param;
        final ArrayList<Object> keys;
        Server srv;
        final Server finalSrv;
        final Message message2;
        final Server server;
        final List<String> list;
        Optional<CommandBase> cmdOpt;
        CommandBase cmd;
        final SuccessState successState;
        Preference commandChannelIdPref;
        String commandChannelId;
        final TextChannel textChannel;
        Auth auth;
        final SuccessState newS;
        msg.getUserAuthor().ifPresent(usr -> {
            parts = Collections.unmodifiableList((List<?>)Arrays.asList((T[])message.getContent().split(" ")));
            param = extractParam(message);
            keys = new ArrayList<Object>(CommandBase.KEYWORDS);
            srv = null;
            if (!message.isPrivate()) {
                srv = (Server)message.getServer().get();
            }
            finalSrv = srv;
            ServerPreferences.getPreference("custom_prefix").ifPresent(preference -> {
                if (!message2.isPrivate() && !preference.get(server).equalsIgnoreCase("none")) {
                    list.add(preference.get(server));
                }
                return;
            });
            if (parts.size() >= 2) {
                if (keys.stream().map((Function<? super Object, ?>)String::toLowerCase).anyMatch(w -> w.equals(parts.get(0).toLowerCase()))) {
                    cmdOpt = findCommand(parts.get(1));
                    if (cmdOpt.isPresent()) {
                        cmd = cmdOpt.get();
                        if (message.isPrivate()) {
                            if (cmd.canRunPrivately) {
                                cmd.runPrivate(event, (List<String>)Collections.unmodifiableList((List<?>)param)).evaluateOnMessage(message);
                            }
                            else {
                                message.getUserAuthor().ifPresent(user -> user.openPrivateChannel().thenAcceptAsync(pc -> SuccessState.SERVER_ONLY.evaluateOnMessage(message)));
                                CommandBase.otherwise.run(event, successState);
                            }
                        }
                        else {
                            commandChannelIdPref = ServerPreferences.getPreference("command_channel_id").get();
                            commandChannelId = commandChannelIdPref.get(srv);
                            if (commandChannelId.equals("none") || textChannel.getIdAsString().equals(commandChannelId) || cmd.group == CommandGroup.BASIC || cmd.group == CommandGroup.BOT_SETUP || cmd.canIgnorePrivateChannel) {
                                if (event.getServer().isPresent()) {
                                    if (param.size() >= cmd.serverParameterRange[0] && param.size() <= cmd.serverParameterRange[1]) {
                                        auth = Auth.softGet(srv);
                                        if (!cmd.requiresAuth || auth.isAuth(usr)) {
                                            cmd.runServer(event, (List<String>)Collections.unmodifiableList((List<?>)param)).evaluateOnMessage(message);
                                        }
                                        else {
                                            CommandBase.otherwise.run(event, successState);
                                        }
                                    }
                                    else {
                                        CommandBase.otherwise.run(event, successState);
                                    }
                                }
                                else {
                                    new SuccessState(Type.ERRORED, "Fatal Internal Error!", "Please contact the bot developer: " + Bot.ownerTag() + "\n```CommandBase#139```");
                                    successState.merge(newS).evaluateOnMessage(message);
                                    CommandBase.otherwise.run(event, successState);
                                }
                            }
                            else {
                                successState.merge(new SuccessState(Type.ERRORED, "The Preference `command_channel_id` has been set.", "That command can not override the command channel option.\nTo unset the command channel; use `dango pref command_channel_id none` to reset the command channel option.")).evaluateOnMessage(message);
                                CommandBase.otherwise.run(event, successState);
                            }
                        }
                    }
                }
            }
            else if (parts.size() == 1) {
                if (keys.stream().map((Function<? super Object, ?>)String::toLowerCase).anyMatch(w -> w.equals(parts.get(0).toLowerCase()))) {
                    if (message.isPrivate()) {
                        new Help().runPrivate(event, new ArrayList<String>());
                    }
                    else {
                        new Help().runServer(event, new ArrayList<String>());
                        CommandBase.otherwise.run(event, successState);
                    }
                }
                else {
                    CommandBase.otherwise.run(event, successState);
                }
            }
            else {
                CommandBase.otherwise.run(event, successState);
            }
        });
    }
    
    public static boolean isCommand(final MessageCreateEvent event) {
        final Message msg = event.getMessage();
        final List<String> parts = Collections.unmodifiableList((List<? extends String>)Arrays.asList((T[])msg.getContent().split(" ")));
        final List<String> keys = new ArrayList<String>(CommandBase.KEYWORDS);
        Server srv = null;
        if (!msg.isPrivate()) {
            srv = msg.getServer().get();
        }
        final Server finalSrv = srv;
        final Message message;
        final Server server;
        final List<String> list;
        ServerPreferences.getPreference("custom_prefix").ifPresent(preference -> {
            if (!message.isPrivate() && !preference.get(server).equalsIgnoreCase("none")) {
                list.add(preference.get(server));
            }
            return;
        });
        if (parts.size() >= 2 && keys.stream().map((Function<? super Object, ?>)String::toLowerCase).anyMatch(w -> w.equals(parts.get(0).toLowerCase()))) {
            final Optional<CommandBase> cmdOpt = findCommand(parts.get(1));
            return cmdOpt.isPresent();
        }
        return false;
    }
    
    public static Optional<CommandBase> findCommand(final String keyword) {
        return CommandBase.commands.stream().filter(c -> Arrays.stream(c.keywords).map((Function<? super String, ?>)String::toLowerCase).anyMatch(w -> w.equals(keyword.toLowerCase()))).findAny();
    }
    
    private static List<String> extractParam(final Message msg) {
        final List<String> keys = new ArrayList<String>(CommandBase.KEYWORDS);
        Server srv = null;
        if (!msg.isPrivate()) {
            srv = msg.getServer().get();
        }
        final Server finalSrv = srv;
        final Server server;
        final List<String> list;
        ServerPreferences.getPreference("custom_prefix").ifPresent(preference -> {
            if (!msg.isPrivate() && !preference.get(server).equalsIgnoreCase("none")) {
                list.add(preference.get(server));
            }
            return;
        });
        final List<String> parts = Collections.unmodifiableList((List<? extends String>)Arrays.asList((T[])msg.getContent().split(" ")));
        List<String> param;
        if (keys.contains(parts.get(0).toLowerCase()) && parts.size() > 2) {
            param = Collections.unmodifiableList((List<? extends String>)parts.subList(2, parts.size()));
        }
        else {
            param = new ArrayList<String>();
        }
        return param;
    }
    
    public abstract SuccessState runServer(final MessageCreateEvent p0, final List<String> p1);
    
    public abstract SuccessState runPrivate(final MessageCreateEvent p0, final List<String> p1);
    
    static {
        CommandBase.commands = new ArrayList<CommandBase>();
        CommandBase.KEYWORDS = new HashSet<String>();
        CommandBase.log = new Debugger(CommandBase.class.getName());
        CommandBase.otherwise = new OtherwiseRunner();
    }
    
    public static class OtherwiseRunner
    {
        private BiConsumer<MessageCreateEvent, SuccessState> consumer;
        
        OtherwiseRunner() {
        }
        
        void run(final MessageCreateEvent event, final SuccessState state) {
            this.consumer.accept(event, state);
        }
        
        public void otherwise(final BiConsumer<MessageCreateEvent, SuccessState> consumer) {
            this.consumer = consumer;
        }
    }
}
