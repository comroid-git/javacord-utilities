// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util;

import de.kaleidox.util.commands.CommandBase;
import java.util.Set;
import java.util.function.BiFunction;
import org.javacord.api.entity.server.Server;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import java.util.function.Supplier;
import de.kaleidox.util.commands.EmbedMaker;
import org.javacord.api.entity.user.User;
import org.javacord.api.DiscordApi;

public class Registerer
{
    public static DiscordApi API;
    public static User SELF;
    public static User OWNER;
    
    public static void initUtils(final DiscordApi api) {
        Registerer.API = api;
        Registerer.SELF = api.getYourself();
        api.getOwner().thenAcceptAsync(user -> Registerer.OWNER = user);
    }
    
    public static class EmbedMaker
    {
        public static void register(final String pVersion, final String pInviteLink, final String pDonateLink, final String pDiscordLink, final String pGitLink, final String pBugreportLink) {
            de.kaleidox.util.commands.EmbedMaker.init(pVersion, pInviteLink, pDonateLink, pDiscordLink, pGitLink, pBugreportLink);
        }
    }
    
    public static class Bot
    {
        public static void register(@Nullable final Supplier<EmbedBuilder> basicEmbedCreator, @Nullable final Function<Server, EmbedBuilder> serverEmbedCreator, @Nullable final BiFunction<Server, User, EmbedBuilder> serverUserEmbedCreator) {
            de.kaleidox.util.Bot.registerBuilder(basicEmbedCreator, serverEmbedCreator, serverUserEmbedCreator);
        }
    }
    
    public static class CommandBase
    {
        public static void registerKeywords(final Set<String> keywords) {
            de.kaleidox.util.commands.CommandBase.addKeywords(keywords);
        }
        
        public static de.kaleidox.util.commands.CommandBase.OtherwiseRunner registerCommands(final Package fromPackage) {
            return de.kaleidox.util.commands.CommandBase.register(fromPackage);
        }
    }
}
