package de.comroid.javacord.util.commands.eval.model;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

@Deprecated
public class EvalEmbed extends Embed {
    public EvalEmbed(Server server, User user) {
        super(server, user); 
    }
}
