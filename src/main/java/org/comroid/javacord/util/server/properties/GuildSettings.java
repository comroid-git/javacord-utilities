package org.comroid.javacord.util.server.properties;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.logging.log4j.Logger;
import org.javacord.core.util.logging.LoggerUtil;

public final class GuildSettings implements Closeable {
    public static final Logger logger = LoggerUtil.getLogger(GuildSettings.class);

    private final File file;
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

    private GuildSettings(File file, JsonNode data) throws NoSuchMethodException, ClassNotFoundException {
        this.file = file;
        this.properties = new ConcurrentHashMap<>();

        for (JsonNode propertyData : data) {
            final Property property = Property.from(this, propertyData);

            this.properties.put(property.getName(), property);
        }
    }

    @Override
    public void close() throws IOException {
        storeData();
    }

    public void storeData() throws IOException {
        final String json = serialize().toPrettyString();

        final File tmpFile = File.createTempFile("GuildSettings-" + System.currentTimeMillis(), ".json.tmp");
        final FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);

        for (byte aByte : json.getBytes(StandardCharsets.UTF_8))
            fileOutputStream.write(aByte);
        fileOutputStream.close();

        if (!file.exists() || (file.exists() && file.delete()))
            tmpFile.renameTo(file);
        else throw new IOException("Could not delete previous file!");
    }

    private JsonNode serialize() {
        final ArrayNode data = JsonNodeFactory.instance.arrayNode();

        properties.forEach((key, property) -> data.add(property.serialize()));

        return data;
    }

    private GuildSettings registerProperty(Consumer<Property.Builder> propertySetup) {
        final Property.Builder builder = new Property.Builder(this);

        propertySetup.accept(builder);

        try {
            final Property property = builder.build();
            properties.put(property.getName(), property);
        } catch (Exception e) {
            throw new RuntimeException("Error registering property [ " + builder.getName() + " ]", e);
        }

        return this;
    }

    public static GuildSettings deserialize(File file) throws IOException {
        final JsonNode data = new ObjectMapper()
                .readTree(file);

        int version;
        if ((version = data.path("version").asInt(2)) != 2)
            throw new IllegalStateException("Illegal settings file version: " + version);

        try {
            return new GuildSettings(file, data);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new IOException("An exception occurred while deserializing", e);
        }
    }
}
