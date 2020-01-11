// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands.genericBotCommands;

import java.util.Optional;
import java.util.ArrayList;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.Message;
import de.kaleidox.util.objects.successstate.Type;
import de.kaleidox.util.objects.serverpreferences.Preference;
import de.kaleidox.util.objects.serverpreferences.ServerPreferences;
import de.kaleidox.util.Bot;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
import de.kaleidox.util.objects.successstate.SuccessState;
import java.util.List;
import org.javacord.api.event.message.MessageCreateEvent;
import de.kaleidox.util.commands.EmbedMaker;
import de.kaleidox.util.commands.CommandGroup;
import de.kaleidox.util.commands.CommandBase;

public class Preferences extends CommandBase
{
    public Preferences() {
        super(new String[] { "setup", "pref", "preferences" }, true, false, true, new int[] { 0, 2 }, CommandGroup.BOT_SETUP, EmbedMaker.getBasicEmbed().addField("Bot Preferences", "Lets you specify how you want the bot to behave in different ways.").addField("Changing Values", "If you want to change a value of a variable, you need to provide a fitting value for that variable.\nThe Variables have different Regular Expressions that define what kind of Inputs are valid, and if you click the reactions from DangoBot on your Commands, you get a detailed description on what went wrong, or if everything went alright.").addField("Resetting to defaults:", "The preferences can be set to their defaults by using `dango setup reset`"));
    }
    
    @Override
    public SuccessState runServer(final MessageCreateEvent event, final List<String> param) {
        final SuccessState state = new SuccessState();
        final Message msg = event.getMessage();
        final Server srv = msg.getServer().get();
        final ServerTextChannel stc = msg.getServerTextChannel().get();
        final User usr = msg.getUserAuthor().get();
        final EmbedBuilder embed = Bot.getEmbed(srv, usr);
        if (!ServerPreferences.getMapper().containsKey(srv.getId())) {
            ServerPreferences.initPrefs(srv);
        }
        switch (param.size()) {
            case 0: {
                final ArrayList<Preference> entries = ServerPreferences.getEntries();
                if (entries.size() > 0) {
                    entries.forEach(pref -> embed.addField("Preference `" + pref.getName() + "` has the value:", "```" + pref.get(srv) + "```"));
                    stc.sendMessage(embed.setDescription("All preferences and their values:"));
                }
                else {
                    stc.sendMessage(embed.addField("Whoops!", "No Preferences available!"));
                }
                state.successful();
                break;
            }
            case 1: {
                final Optional<Preference> optionalPreference = ServerPreferences.getPreference(param.get(0).toLowerCase());
                if (optionalPreference.isPresent()) {
                    embed.addField("Preference `" + param.get(0) + "` has the value:", "```" + optionalPreference.get().get(srv) + "```");
                    stc.sendMessage(embed);
                    state.successful();
                    break;
                }
                state.addMessage(Type.ERRORED, "Could not find a preference called `" + param.get(0).toLowerCase() + "`");
                break;
            }
            case 2: {
                final Optional<Preference> optionalPreference = ServerPreferences.getPreference(param.get(0).toLowerCase());
                final String value = param.get(1);
                if (optionalPreference.isPresent()) {
                    final Preference preference = optionalPreference.get();
                    if (preference.accepts(value)) {
                        preference.set(srv, value);
                        embed.addField("Preference `" + param.get(0) + "` has ben changed.", "New value: ```" + value + "```");
                        stc.sendMessage(embed);
                        state.successful();
                    }
                    else {
                        state.addMessage(Type.ERRORED, "This preference does not accept this input.");
                    }
                    break;
                }
                state.addMessage(Type.ERRORED, "Could not find a preference called `" + param.get(0).toLowerCase() + "`");
                break;
            }
            default: {
                state.arguments();
                break;
            }
        }
        return state;
    }
    
    @Override
    public SuccessState runPrivate(final MessageCreateEvent event, final List<String> param) {
        return SuccessState.SERVER_ONLY;
    }
}
