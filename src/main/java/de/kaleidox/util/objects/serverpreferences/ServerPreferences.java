// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.serverpreferences;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import de.kaleidox.util.Utils;
import de.kaleidox.util.commands.CommandBase;
import de.kaleidox.util.commands.genericBotCommands.Preferences;
import de.kaleidox.util.objects.serializer.PropertiesMapper;

import org.javacord.api.entity.server.Server;

public class ServerPreferences {
    protected static final PropertiesMapper mapper;
    private static final ArrayList<Preference> entries;

    static {
        mapper = new PropertiesMapper(Utils.getOrCreateProps("serverPreferences"));
        entries = new ArrayList<Preference>();
    }

    public static final <T> Preference registerPreference(final String name, final String defaultValue, final Predicate<T> accepts) {
        synchronized (ServerPreferences.entries) {
            final Preference pref = new Preference(name, defaultValue, accepts, ServerPreferences.entries.size());
            CommandBase.register(new Preferences());
            ServerPreferences.entries.add(pref);
            return pref;
        }
    }

    public static final <T> Preference registerPreference(final String name, final String defaultValue, final Predicate<T> accepts, final int index) {
        synchronized (ServerPreferences.entries) {
            final Preference pref = new Preference(name, defaultValue, accepts, index);
            CommandBase.register(new Preferences());
            ServerPreferences.entries.add(pref);
            return pref;
        }
    }

    public static Optional<Preference> getPreference(final String name) {
        return ServerPreferences.entries.stream().filter(pref -> pref.getName().equals(name)).findAny();
    }

    public static ArrayList<Preference> getEntries() {
        return ServerPreferences.entries;
    }

    public static void initPrefs(final Server forServer) {
        final long id = forServer.getId();
        synchronized (ServerPreferences.mapper) {
            if (ServerPreferences.mapper.containsKey(id)) {
                ServerPreferences.mapper.clear(id);
            }
            ServerPreferences.entries.forEach(pref -> ServerPreferences.mapper.add(id, pref.getDefaultValue()));
            ServerPreferences.mapper.write();
        }
    }

    public static PropertiesMapper getMapper() {
        return ServerPreferences.mapper;
    }
}
