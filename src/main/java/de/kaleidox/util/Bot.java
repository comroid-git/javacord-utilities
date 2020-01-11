// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util;

import java.awt.Color;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.Optional;
import org.javacord.api.entity.permission.Role;
import java.util.List;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.annotation.Nullable;
import org.javacord.api.entity.user.User;
import java.util.function.BiFunction;
import org.javacord.api.entity.server.Server;
import java.util.function.Function;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import java.util.function.Supplier;

public class Bot
{
    private static Supplier<EmbedBuilder> basicEmbedCreator;
    private static Function<Server, EmbedBuilder> serverEmbedCreator;
    private static BiFunction<Server, User, EmbedBuilder> serverUserEmbedCreator;
    
    public static final void registerBuilder(@Nullable final Supplier<EmbedBuilder> basicEmbedCreator, @Nullable final Function<Server, EmbedBuilder> serverEmbedCreator, @Nullable final BiFunction<Server, User, EmbedBuilder> serverUserEmbedCreator) {
        if (basicEmbedCreator != null) {
            Bot.basicEmbedCreator = basicEmbedCreator;
        }
        if (serverEmbedCreator != null) {
            Bot.serverEmbedCreator = serverEmbedCreator;
        }
        if (serverUserEmbedCreator != null) {
            Bot.serverUserEmbedCreator = serverUserEmbedCreator;
        }
    }
    
    public static EmbedBuilder getEmbed() {
        return Bot.basicEmbedCreator.get();
    }
    
    public static EmbedBuilder getEmbed(final Server server) {
        return Bot.serverEmbedCreator.apply(server);
    }
    
    public static EmbedBuilder getEmbed(final Server server, final User user) {
        return Bot.serverUserEmbedCreator.apply(server, user);
    }
    
    public static boolean isTesting() {
        return System.getProperty("os.name").equals("Windows 10");
    }
    
    public static String dblBotToken() {
        return readFilePlain("keys/tokenDiscordBotsOrg.txt");
    }
    
    public static String botToken() {
        return readFilePlain(isTesting() ? "keys/tokenTest.txt" : "keys/tokenMain.txt");
    }
    
    public static String botName() {
        return Registerer.SELF.getName();
    }
    
    public static String ownerTag() {
        return Registerer.API.getOwner().join().getDiscriminatedName();
    }
    
    public static String discordInvite() {
        return "http://discord.kaleidox.de";
    }
    
    public static String readFilePlain(final String name) {
        String give;
        try {
            final BufferedReader br = new BufferedReader(new FileReader(name));
            give = br.readLine();
        }
        catch (IOException e) {
            return "0";
        }
        return give;
    }
    
    static {
        Bot.basicEmbedCreator = (() -> new EmbedBuilder().setFooter(botName()).setAuthor(botName()).setTimestampToNow().setColor(Utils.getRandomColor()));
        final List collect;
        Bot.serverEmbedCreator = (Function<Server, EmbedBuilder>)(server -> {
            collect = (List)server.getRoles(Registerer.SELF).stream().map(Role::getColor).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            return new EmbedBuilder().setFooter(botName()).setAuthor(botName()).setTimestampToNow().setColor((Color)collect.get(collect.size() - 1));
        });
        final List collect2;
        Bot.serverUserEmbedCreator = (BiFunction<Server, User, EmbedBuilder>)((server1, user) -> {
            collect2 = (List)server1.getRoles(Registerer.SELF).stream().map(Role::getColor).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            return new EmbedBuilder().setFooter("Requested by " + user.getDiscriminatedName(), user.getAvatar().getUrl().toString()).setAuthor(botName()).setTimestampToNow().setColor((Color)collect2.get(collect2.size() - 1));
        });
    }
}
