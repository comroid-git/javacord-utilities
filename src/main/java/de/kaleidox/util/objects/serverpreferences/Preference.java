// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.serverpreferences;

import java.util.function.Predicate;

import org.javacord.api.entity.server.Server;

public class Preference<T> {
    private String defaultValue;
    private String name;
    private int index;
    private Predicate<T> accepts;

    public Preference(final String name, final String defaultValue, final Predicate<T> accepts, final int index) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.accepts = accepts;
        this.index = index;
    }

    public String get(final Server forServer) {
        if (ServerPreferences.mapper.containsKey(forServer.getId())) {
            return ServerPreferences.mapper.softGet(forServer.getId(), this.index, this.defaultValue);
        }
        return "none";
    }

    public void set(final Server forServer, final T value) {
        if (this.accepts.test(value)) {
            ServerPreferences.mapper.set(forServer.getId(), this.index, value);
            ServerPreferences.mapper.write();
            return;
        }
        throw new IllegalArgumentException("This Preference does not accept this value.");
    }

    public boolean accepts(final T value) {
        return this.accepts.test(value);
    }

    public String getName() {
        return this.name;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public int getIndex() {
        return this.index;
    }

    public Predicate<T> getAccepts() {
        return this.accepts;
    }
}
