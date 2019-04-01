package de.kaleidox.javacord.util.server.properties;

import java.util.concurrent.ConcurrentHashMap;

import de.kaleidox.util.markers.Value;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.server.Server;

import static de.kaleidox.util.helpers.JsonHelper.nodeOf;

public final class PropertyGroup {
    private final String name;
    private final Object defaultValue;
    private final ConcurrentHashMap<Long, Value> values;

    public PropertyGroup(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;

        values = new ConcurrentHashMap<>();
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
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

    // [id:"123";val:"abc";type:java.lang.String];[id:"234";val:"def";type:java.lang.Short]
    void serialize(ArrayNode node) {
        values.forEach((id, value) -> {
            ObjectNode object = node.addObject();

            object.set("id", nodeOf(id));
            object.set("val", nodeOf(value.asString()));
            object.set("type", nodeOf((value.getValue() != null ? value.getValue() : "").getClass().getName()));
        });
    }
}
