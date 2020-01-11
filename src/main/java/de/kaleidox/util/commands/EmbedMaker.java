// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands;

import org.javacord.api.Javacord;
import de.kaleidox.util.Utils;
import de.kaleidox.util.Bot;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class EmbedMaker
{
    private static final String loading = "[_Loading..._]";
    public static String inviteLink;
    public static String donateLink;
    public static String discordLink;
    public static String gitLink;
    public static String bugreportLink;
    public static String version;
    
    public static void init(final String pVersion, final String pInviteLink, final String pDonateLink, final String pDiscordLink, final String pGitLink, final String pBugreportLink) {
        EmbedMaker.version = pVersion;
        EmbedMaker.inviteLink = pInviteLink;
        EmbedMaker.donateLink = pDonateLink;
        EmbedMaker.discordLink = pDiscordLink;
        EmbedMaker.gitLink = pGitLink;
        EmbedMaker.bugreportLink = pBugreportLink;
    }
    
    public static EmbedBuilder getBasicEmbed() {
        return new EmbedBuilder().setFooter(Bot.botName()).setAuthor(Bot.botName()).setTimestampToNow().setColor(Utils.getRandomColor());
    }
    
    public static EmbedBuilder bugreport() {
        return getBasicEmbed().addField("Report Bugs over here:", EmbedMaker.bugreportLink);
    }
    
    public static EmbedBuilder discord() {
        return getBasicEmbed().addField("Our Discord Server:", EmbedMaker.discordLink);
    }
    
    public static EmbedBuilder donate() {
        return getBasicEmbed().addField("You can Donate for this Bot here, if you want:", EmbedMaker.donateLink);
    }
    
    public static EmbedBuilder help() {
        return getBasicEmbed().addField("Help", "Sends a list of all commands");
    }
    
    public static EmbedBuilder info() {
        return getBasicEmbed().addField("About the bot:", "Running on Version " + EmbedMaker.version + "\nMade with love by " + Bot.ownerTag() + "\nGitHub: " + EmbedMaker.gitLink + "\nRunning on Javacord Version " + Javacord.VERSION + "\n\nEnjoy!");
    }
    
    public static EmbedBuilder invite() {
        return getBasicEmbed().addField("The Invitation for the Bot:", EmbedMaker.inviteLink);
    }
    
    static {
        EmbedMaker.inviteLink = "[_Loading..._]";
        EmbedMaker.donateLink = "[_Loading..._]";
        EmbedMaker.discordLink = "[_Loading..._]";
        EmbedMaker.gitLink = "[_Loading..._]";
        EmbedMaker.bugreportLink = "[_Loading..._]";
        EmbedMaker.version = "[_Loading..._]";
    }
}
