// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands.genericBotCommands;

import org.javacord.api.entity.channel.PrivateChannel;
import javax.script.ScriptEngine;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import java.awt.Color;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import java.util.function.Function;
import org.javacord.api.entity.channel.ServerChannel;
import javax.script.ScriptEngineManager;
import java.util.stream.Collector;
import de.kaleidox.util.libs.CustomCollectors;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.channel.ServerTextChannel;
import de.kaleidox.util.objects.successstate.SuccessState;
import java.util.List;
import org.javacord.api.event.message.MessageCreateEvent;
import de.kaleidox.util.commands.EmbedMaker;
import de.kaleidox.util.commands.CommandGroup;
import de.kaleidox.util.commands.CommandBase;

public class Evaluate extends CommandBase
{
    public Evaluate() {
        super("eval", false, true, new int[] { 0, 999 }, CommandGroup.NONE, EmbedMaker.getBasicEmbed().addField("you should not be using this", ":P"));
    }
    
    @Override
    public SuccessState runServer(final MessageCreateEvent event, final List<String> param) {
        final Message message = event.getMessage();
        final ServerTextChannel channel = event.getServerTextChannel().get();
        final DiscordApi api = event.getApi();
        final User user = event.getMessage().getUserAuthor().get();
        if (user.isBotOwner()) {
            String code = param.subList(1, param.size()).stream().collect(CustomCollectors.toConcatenatedString(' '));
            boolean noOutput = false;
            if (code.contains("--no-output")) {
                code = code.replace("--no-output", "");
                noOutput = true;
            }
            final ScriptEngineManager manager = new ScriptEngineManager();
            final ScriptEngine engine = manager.getEngineByName("ECMAScript");
            engine.put("api", api);
            engine.put("msg", message);
            engine.put("channel", channel);
            engine.put("user", user);
            engine.put("content", message.getContent());
            engine.put("server", channel.asServerChannel().map(ServerChannel::getServer).orElse(null));
            final String[] packagesToImport = { "java.util", "java.lang", "java.net", "java.io", "Packages.org.javacord", "Packages.org.javacord.entity.channel", "Packages.org.javacord.entity.message", "Packages.org.javacord.entity.message.embed", "Packages.org.javacord.entity.emoji", "Packages.org.javacord.entity.permission", "Packages.org.javacord.entity", "Packages.org.javacord.entity.message.reaction", "Packages.org.javacord.entity.message", "Packages.org.javacord.entity.server" };
            final StringBuilder builder = new StringBuilder();
            builder.append("load('nashorn:mozilla_compat.js');\n");
            for (final String packageToImport : packagesToImport) {
                builder.append("importPackage(");
                builder.append(packageToImport);
                builder.append(");\n");
            }
            builder.append(code);
            code = builder.toString();
            final EmbedBuilder embed = new EmbedBuilder();
            try {
                final Object result = engine.eval(code);
                embed.setColor(Color.GREEN);
                embed.addField(":printer: **Result**", "```\n" + String.valueOf(result) + "\n```", false);
                embed.addField(":wrench: **Result Type**", "```\n" + ((result == null) ? "None" : result.getClass().getSimpleName()) + "\n```", false);
            }
            catch (Throwable e) {
                noOutput = false;
                embed.setColor(Color.RED);
                embed.addField(":bomb: **Error**", "```\n" + String.valueOf(e.getMessage()) + "\n```", false);
                e.printStackTrace();
            }
            if (!noOutput) {
                channel.sendMessage(embed);
            }
            return SuccessState.SUCCESSFUL;
        }
        return SuccessState.UNAUTHORIZED;
    }
    
    @Override
    public SuccessState runPrivate(final MessageCreateEvent event, final List<String> param) {
        final Message message = event.getMessage();
        final PrivateChannel channel = event.getPrivateChannel().get();
        final DiscordApi api = event.getApi();
        final User user = event.getMessage().getUserAuthor().get();
        if (user.isBotOwner()) {
            String code = param.subList(1, param.size()).stream().collect(CustomCollectors.toConcatenatedString(' '));
            boolean noOutput = false;
            if (code.contains("--no-output")) {
                code = code.replace("--no-output", "");
                noOutput = true;
            }
            final ScriptEngineManager manager = new ScriptEngineManager();
            final ScriptEngine engine = manager.getEngineByName("ECMAScript");
            engine.put("api", api);
            engine.put("msg", message);
            engine.put("channel", channel);
            engine.put("user", user);
            engine.put("content", message.getContent());
            engine.put("server", channel.asServerChannel().map(ServerChannel::getServer).orElse(null));
            final String[] packagesToImport = { "java.util", "java.lang", "java.net", "java.io", "Packages.org.javacord", "Packages.org.javacord.entity.channel", "Packages.org.javacord.entity.message", "Packages.org.javacord.entity.message.embed", "Packages.org.javacord.entity.emoji", "Packages.org.javacord.entity.permission", "Packages.org.javacord.entity", "Packages.org.javacord.entity.message.reaction", "Packages.org.javacord.entity.message", "Packages.de.kaleidox.util", "Packages.de.kaleidox.util.objects", "Packages.de.kaleidox.util.objects.strum", "Packages.de.kaleidox.util.objects.serverpreferences", "Packages.de.kaleidox.util.objects.successstate", "Packages.de.kaleidox.util.libs", "Packages.de.kaleidox.util.libs.listeners" };
            final StringBuilder builder = new StringBuilder();
            builder.append("load('nashorn:mozilla_compat.js');\n");
            for (final String packageToImport : packagesToImport) {
                builder.append("importPackage(");
                builder.append(packageToImport);
                builder.append(");\n");
            }
            builder.append(code);
            code = builder.toString();
            final EmbedBuilder embed = new EmbedBuilder();
            try {
                final Object result = engine.eval(code);
                embed.setColor(Color.GREEN);
                embed.addField(":printer: **Result**", "```\n" + String.valueOf(result) + "\n```", false);
                embed.addField(":wrench: **Result Type**", "```\n" + ((result == null) ? "None" : result.getClass().getSimpleName()) + "\n```", false);
            }
            catch (Throwable e) {
                noOutput = false;
                embed.setColor(Color.RED);
                embed.addField(":bomb: **Error**", "```\n" + String.valueOf(e.getMessage()) + "\n```", false);
                e.printStackTrace();
            }
            if (!noOutput) {
                channel.sendMessage(embed);
            }
            return SuccessState.SUCCESSFUL;
        }
        return SuccessState.UNAUTHORIZED;
    }
}
