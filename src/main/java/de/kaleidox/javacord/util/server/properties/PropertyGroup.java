package de.kaleidox.javacord.util.server.properties;

import java.util.concurrent.ConcurrentHashMap;

import de.kaleidox.util.markers.Value;

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

    public Value.Setter setValue(long serverId) {
        return getValue(serverId).setter();
    }

    public Value getValue(long serverId) {
        return values.compute(serverId, (k, v) -> {
            if (v == null) return new Value(defaultValue);
            return v;
        });
    }

    // [id:"123";val:"abc";type:java.lang.String];[id:"234";val:"def";type:java.lang.Short]
    void serialize(StringBuilder sb) {
        values.forEach((id, value) -> sb.append("[id:\"")
                .append(id)
                .append("\";val:\"")
                .append(value.asString()
                        .replace("\\", "/")
                        .replace(")", "#")
                        .replace("]", "#")
                        .replace("}", "#")
                        .replace("\"", "\\\""))
                .append("\";type:\"")
                .append((value.getValue() != null ? value.getValue() : "").getClass().getName())
                .append("\"];"));
        sb.delete(sb.length(), sb.length());
    }
}
