package de.kaleidox.javacord.util.server.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.kaleidox.util.interfaces.Initializable;
import de.kaleidox.util.interfaces.Terminatable;
import de.kaleidox.util.markers.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static de.kaleidox.util.helpers.JsonHelper.nodeOf;
import static de.kaleidox.util.helpers.JsonHelper.objectNode;

public final class ServerPropertiesManager implements Initializable, Terminatable {
    private final Map<String, PropertyGroup> properties;
    private final File propertiesFile;

    public ServerPropertiesManager(File propertiesFile) throws IOException {
        if (!propertiesFile.exists()) propertiesFile.createNewFile();
        this.propertiesFile = propertiesFile;

        properties = new ConcurrentHashMap<>();

        init();

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
                    try {
                        terminate();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
    }

    public PropertyGroup register(String name, Object defaultValue) {
        return register(name, defaultValue, name, "No description provided.");
    }

    public PropertyGroup register(String name, Object defaultValue, String displayName, String description) {
        properties.compute(name, (k, v) -> {
            if (v == null) return new PropertyGroup(name, defaultValue, displayName, description);
            else if (!v.getDefaultValue().equals(defaultValue) && name.equals(v.getName()))
                v = new PropertyGroup(v.getName(), defaultValue, displayName, description);
            return v;
        });

        return getProperty(name);
    }

    public PropertyGroup getProperty(String name) {
        return properties.compute(name, (k, v) -> {
            if (v == null) return register(name, name);
            return v;
        });
    }

    @Override
    public void init() throws IOException {
        readData();
    }

    @Override
    public void terminate() throws IOException {
        storeData();
    }

    public void storeData() throws IOException {
        ObjectNode node = objectNode();
        ArrayNode array = node.putArray("entries");

        properties.forEach((name, group) -> {
            ObjectNode data = array.addObject();
            data.set("name", nodeOf(name));
            data.set("default", nodeOf(group.getDefaultValue().toString()));
            group.serialize(data.putArray("items"));
        });

        if (propertiesFile.exists()) propertiesFile.delete();
        propertiesFile.createNewFile();
        FileOutputStream stream = new FileOutputStream(propertiesFile);
        stream.write(node.toString().getBytes(UTF_8));
        stream.close();
    }

    private void readData() throws IOException {
        int c = 0;
        JsonNode node = new ObjectMapper().readTree(new FileInputStream(propertiesFile));

        if (node != null && node.size() != 0) {
            for (JsonNode entry : node.get("entries")) {
                PropertyGroup group = register(entry.get("name").asText(), entry.get("default").asText());

                for (JsonNode item : entry.get("items")) {
                    String typeVal = item.get("type").asText();
                    try {
                        Class<?> type = Class.forName(typeVal);
                        Value.Setter setValue = group.setValue(item.get("id").asLong());
                        String val = item.get("val").asText();

                        if (type == String.class) setValue.toString(val);
                        else setValue.toObject(type.getMethod("valueOf", String.class).invoke(null, val));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new AssertionError("Illegal structure for " + typeVal + "#valueOf", e);
                    } catch (NoSuchMethodException e) {
                        throw new AssertionError("Wrong class forName: " + typeVal + "; method valueOf not found", e);
                    } catch (ClassNotFoundException e) {
                        throw new AssertionError("Wrong class forName: " + typeVal + "; class not found", e);
                    }
                }
            }
        }
    }
}
