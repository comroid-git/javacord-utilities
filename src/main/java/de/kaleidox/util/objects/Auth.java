// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects;

import java.util.concurrent.ConcurrentHashMap;

import de.kaleidox.util.Bot;
import de.kaleidox.util.Registerer;
import de.kaleidox.util.Utils;
import de.kaleidox.util.objects.serializer.PropertiesMapper;
import de.kaleidox.util.objects.serializer.SelectedPropertiesMapper;
import de.kaleidox.util.objects.successstate.SuccessState;
import de.kaleidox.util.objects.successstate.Type;
import de.kaleidox.util.tools.Debugger;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class Auth {
    private static final ConcurrentHashMap<Long, Auth> selfMap;
    public static PropertiesMapper auths;
    private Debugger log;
    private Server myServer;
    private Long serverId;

    static {
        selfMap = new ConcurrentHashMap<Long, Auth>();
        Auth.auths = new PropertiesMapper(Utils.getOrCreateProps("authUsers"), ';');
    }

    private Auth(final Server server) {
        this.log = new Debugger(Auth.class.getName());
        this.myServer = server;
        this.serverId = this.myServer.getId();
        Utils.safePut(Auth.selfMap, this.serverId, this);
    }

    public boolean isAuth(final User user) {
        boolean val = false;
        if (Auth.auths.containsValue(this.serverId.toString(), user.getIdAsString())) {
            val = true;
        }
        if (this.myServer.hasPermission(user, PermissionType.MANAGE_SERVER)) {
            val = true;
        }
        if (this.myServer.isAdmin(user)) {
            val = true;
        }
        if (user.isBotOwner()) {
            val = true;
        }
        return val;
    }

    public SuccessState addAuth(final User user) {
        final SuccessState val = SuccessState.NOT_RUN;
        Auth.auths.add(this.serverId.toString(), user.getIdAsString()).write();
        val.addMessage(Type.SUCCESSFUL, "Added auth for User: " + user);
        this.log.put("Added Auth for " + this.myServer.getName());
        Auth.auths.write();
        return val;
    }

    public SuccessState removeAuth(final User user) {
        final SuccessState val = SuccessState.NOT_RUN;
        if (Auth.auths.containsValue(this.myServer.getIdAsString(), user.getIdAsString())) {
            Auth.auths.removeValue(this.serverId.toString(), user.getIdAsString());
            val.addMessage(Type.SUCCESSFUL, "Removed auth for User: " + user);
            this.log.put("Removed Auth for " + this.myServer.getName());
        } else {
            val.addMessage(Type.UNSUCCESSFUL, "User " + user + " already is authed.");
        }
        Auth.auths.write();
        return val;
    }

    public SuccessState sendEmbed(final ServerTextChannel chl) {
        final SuccessState state = new SuccessState(Type.NOT_RUN);
        final EmbedBuilder eb = Bot.getEmbed();
        final SelectedPropertiesMapper select = Auth.auths.select(this.serverId);
        eb.setTitle(Bot.botName() + " - **Authed Users on __" + this.myServer.getName() + "__:**");
        eb.setDescription("_Administrators and User with Permission \"Manage Server\" are Authed by Default._");
        if (select.size() != 0) {
            for (final String x : select.getAll()) {
                final User usr = Registerer.API.getUserById(x).join();
                eb.addField(usr.getName(), usr.getMentionTag(), true);
            }
            chl.sendMessage(eb).thenRun(state::successful);
        } else {
            eb.addField("No Auths.", "There are no Auths for this Server.", false);
            chl.sendMessage(eb).thenRun(state::successful);
        }
        return state;
    }

    public static Auth softGet(final Server server) {
        return Auth.selfMap.containsKey(server.getId()) ? Auth.selfMap.get(server.getId()) : Auth.selfMap.put(server.getId(), new Auth(server));
    }
}
