// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands.baseCommands;

import org.javacord.api.entity.user.User;
import java.util.Optional;
import java.util.Iterator;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import de.kaleidox.util.discord.messages.PagedMessage;
import de.kaleidox.util.Utils;
import org.javacord.api.entity.channel.ServerTextChannel;
import de.kaleidox.util.objects.successstate.Type;
import de.kaleidox.util.objects.successstate.SuccessState;
import java.util.List;
import org.javacord.api.event.message.MessageCreateEvent;
import de.kaleidox.util.commands.EmbedMaker;
import de.kaleidox.util.commands.CommandGroup;
import de.kaleidox.util.commands.CommandBase;

public class Help extends CommandBase
{
    public Help() {
        super("help", false, true, true, new int[] { 0, 1 }, CommandGroup.BASIC, EmbedMaker.help());
    }
    
    @Override
    public SuccessState runServer(final MessageCreateEvent event, final List<String> param) {
        final SuccessState state = new SuccessState(Type.NOT_RUN);
        final Message msg = event.getMessage();
        final ServerTextChannel stc = msg.getServerTextChannel().get();
        if (param.size() == 0) {
            final StringBuilder sb;
            CommandGroup lastGroup;
            final Iterator<CommandBase> iterator;
            CommandBase command;
            StringBuilder cmdText;
            PagedMessage.get((Messageable)stc, () -> "**All Commands:**\n", () -> {
                sb = new StringBuilder();
                lastGroup = CommandGroup.NONE;
                CommandBase.commands.iterator();
                while (iterator.hasNext()) {
                    command = iterator.next();
                    cmdText = new StringBuilder();
                    cmdText.append("Command Keywords: `").append(Utils.concatStrings(" / ", (Object[])command.keywords)).append("` | Private Chat: ").append(command.canRunPrivately ? "\u2705" : "\u274c").append(" | Requires Authorization: ").append(command.requiresAuth ? "\u2705" : "\u274c").append(" | Supported Parameter: ").append(command.serverParameterRange[0]).append(" up to ").append(command.serverParameterRange[1]).append("\n");
                    if (!lastGroup.equals(command.group)) {
                        sb.append(command.group.name).append(cmdText.toString());
                        lastGroup = command.group;
                    }
                }
                return sb.substring(0, sb.length() - 1);
            });
            state.successful();
        }
        else {
            final Optional<CommandBase> cmdOpt = CommandBase.findCommand(param.get(0));
            if (cmdOpt.isPresent()) {
                final CommandBase command2 = cmdOpt.get();
                stc.sendMessage(command2.helpEmbed);
                state.successful();
            }
            else {
                state.addMessage(Type.UNSUCCESSFUL, "Command `" + param.get(0) + "` not found.");
            }
        }
        return state;
    }
    
    @Override
    public SuccessState runPrivate(final MessageCreateEvent event, final List<String> param) {
        final SuccessState state = new SuccessState(Type.NOT_RUN);
        final Message msg = event.getMessage();
        final User usr = msg.getUserAuthor().get();
        if (param.size() == 0) {
            final StringBuilder sb;
            CommandGroup lastGroup;
            final Iterator<CommandBase> iterator;
            CommandBase command;
            StringBuilder cmdText;
            PagedMessage.get((Messageable)usr, () -> "**All Commands:**\n", () -> {
                sb = new StringBuilder();
                lastGroup = CommandGroup.NONE;
                CommandBase.commands.iterator();
                while (iterator.hasNext()) {
                    command = iterator.next();
                    cmdText = new StringBuilder();
                    cmdText.append("Command Keywords: `").append(Utils.concatStrings(" / ", (Object[])command.keywords)).append("` | Private Chat: ").append(command.canRunPrivately ? "\u2705" : "\u274c").append(" | Requires Authorization: ").append(command.requiresAuth ? "\u2705" : "\u274c").append(" | Supported Parameter: ").append(command.serverParameterRange[0]).append(" up to ").append(command.serverParameterRange[1]).append("\n");
                    if (!lastGroup.equals(command.group)) {
                        sb.append(command.group.name).append(cmdText.toString());
                        lastGroup = command.group;
                    }
                }
                return sb.substring(0, sb.length() - 1);
            });
            state.successful();
        }
        else {
            final Optional<CommandBase> cmdOpt = CommandBase.findCommand(param.get(0));
            if (cmdOpt.isPresent()) {
                final CommandBase command2 = cmdOpt.get();
                usr.sendMessage(command2.helpEmbed);
                state.successful();
            }
            else {
                state.addMessage(Type.UNSUCCESSFUL, "Command `" + param.get(0) + "` not found.");
            }
        }
        return state;
    }
}
