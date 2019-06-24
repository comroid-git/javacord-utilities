package de.kaleidox.javacord.util.server.properties;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import de.kaleidox.util.markers.Value;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.server.Server;

import static de.kaleidox.util.Util.nodeOf;

public final class PropertyGroup {
    private final String name;
    private final Value defaultValue;
    private final ConcurrentHashMap<Long, Value> values;
    private String displayName;
    private String description;

    public PropertyGroup(String name, Object defaultValue, String displayName, String description) {
        this.name = name;
        this.defaultValue = new Value(defaultValue);
        this.displayName = displayName;
        this.description = description;

        values = new ConcurrentHashMap<>();
    }

    public String getDisplayName() {
        return displayName;
    }

    public PropertyGroup setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PropertyGroup setDescription(String description) {
        this.description = description;
        return this;
    }

    public <R> Function<Long, R> function(final Class<? extends R> targetType) {
        return serverId -> getValue(serverId).as(targetType);
    }

    public String getName() {
        return name;
    }

    public Value getDefaultValue() {
        return defaultValue;
    }

    public Value.Setter setValue(Server server) {
        return setValue(server.getId());
    }

    public Value.Setter setValue(long serverId) {
        return getValue(serverId).setter();
    }

    public Value getValue(Server server) {
        return getValue(server.getId());
    }

    public Value getValue(long serverId) {
        return values.compute(serverId, (k, v) -> {
            if (v == null) return new Value(defaultValue);
            return v;
        });
    }

    void serialize(ArrayNode node) {
        values.forEach((id, value) -> {
            if (!value.asString().equals(defaultValue.asString())) {
                ObjectNode object = node.addObject();

                object.set("id", nodeOf(id));
                object.set("val", nodeOf(value.asString()));
                object.set("type", nodeOf((value.getValue() != null ? value.getValue() : "").getClass().getName()));
            }
        });
    }
}
