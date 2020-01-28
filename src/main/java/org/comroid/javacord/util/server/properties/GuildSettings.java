package org.comroid.javacord.util.server.properties;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.javacord.core.util.logging.LoggerUtil;

public final class GuildSettings implements Closeable {
    public static final Logger logger = LoggerUtil.getLogger(GuildSettings.class);

    private final Map<String, Property> properties;

    { // initializer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private GuildSettings(Map<String, Property> properties) {
        this.properties = properties;
    }

    private GuildSettings registerProperty(Consumer<Property.Builder> propertySetup) {
        final Property.Builder builder = new Property.Builder(this);

        propertySetup.accept(builder);

        final Property property = builder.build();
        properties.put(property.getName(), property);

        return this;
    }

    @Override
    public void close() throws IOException {
    }

    public static GuildSettings deserialize(File file) throws IOException {
        final JsonNode data = new ObjectMapper()
                .readTree(file);

        int version;
        if ((version = data.path("version").asInt(2)) != 2)
            throw new IllegalStateException("Illegal settings file version: " + version);
    }

    private static Object extractValue(String val) {
        if (val.matches("\\d+")) {
            // is number without decimals
            long longVal = Long.parseLong(val);

            if (longVal <= Byte.MAX_VALUE) return (byte) longVal;
            else if (longVal <= Short.MAX_VALUE) return (short) longVal;
            else if (longVal <= Integer.MAX_VALUE) return (int) longVal;
            else return longVal;
        } else if (val.matches("\\d+\\.\\d+")) {
            // is number with decimals
            return Double.parseDouble(val);
        } else if (val.toLowerCase().matches("(true)|(false)|(yes)|(no)|(on)|(off)")) {
            // is boolean
            switch (val.toLowerCase()) {
                case "true":
                case "yes":
                case "on":
                    return true;
                case "false":
                case "no":
                case "off":
                    return false;
                default:
                    throw new AssertionError("Unrecognized string: " + val);
            }
        } else {
            // is plain string
            return val;
        }
    }
}
