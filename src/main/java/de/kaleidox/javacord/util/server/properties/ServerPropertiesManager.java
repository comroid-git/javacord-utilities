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

import static java.nio.charset.StandardCharsets.UTF_8;

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
        properties.compute(name, (k, v) -> {
            if (v == null) return new PropertyGroup(name, defaultValue);
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

    // (name:"bot.pref";default:"xyz";items:{[id:"123";val:"abc";type:"java.lang.String"];[id:"234";val:"234";type:"java.lang.Short"]})
    public void storeData() throws IOException {
        StringBuilder sb = new StringBuilder();

        properties.forEach((name, group) -> {
            sb.append("(name:\"")
                    .append(name
                            .replace("\\", "/")
                            .replace(")", "#")
                            .replace("]", "#")
                            .replace("}", "#")
                            .replace("\"", "\\\""))
                    .append("\";default:\"")
                    .append(group.getDefaultValue()
                            .toString()
                            .replace("\\", "/")
                            .replace(")", "#")
                            .replace("]", "#")
                            .replace("}", "#")
                            .replace("\"", "\\\""))
                    .append("\";items:{");
            group.serialize(sb);
            sb.append("});");
        });
        sb.delete(sb.length() - 1, sb.length());
        byte[] encode = sb.toString().getBytes(UTF_8);
        if (propertiesFile.exists()) propertiesFile.delete();
        propertiesFile.createNewFile();
        FileOutputStream stream = new FileOutputStream(propertiesFile);
        stream.write(encode);
        stream.close();
    }

    // (name:"bot.pref";default:"xyz";items:{[id:"123";val:"abc";type:"java.lang.String"];[id:"234";val:"234";type:"java.lang.Short"]})
    private void readData() throws IOException {
        FileInputStream stream = new FileInputStream(propertiesFile);
        int r;
        StringBuilder sb = new StringBuilder();
        while ((r = stream.read()) != -1) sb.append((char) r);
        char[] decode = sb.toString().toCharArray();

        r = 0;
        sb = new StringBuilder();
        char reading = '?';
        String name = null, def = null, itemVal = null, itemType = null;
        long itemId = -1;
        while (r < decode.length) {
            if (Character.isAlphabetic(decode[r])
                    || Character.isDigit(decode[r])
                    || decode[r] == '.'
                    || decode[r] == '_'
                    || decode[r] == '-')
                sb.append(decode[r]);
            else {
                switch (sb.toString()) {
                    case "name":
                        name = null;
                        reading = 'n';
                        sb = new StringBuilder();
                        break;
                    case "default":
                        def = null;
                        reading = 'f';
                        sb = new StringBuilder();
                        break;
                    case "items":
                        reading = 'i';
                        sb = new StringBuilder();
                        break;
                    case "id":
                        itemId = -1;
                        reading = 'd';
                        sb = new StringBuilder();
                        break;
                    case "val":
                        itemVal = null;
                        reading = 'v';
                        sb = new StringBuilder();
                        break;
                    case "type":
                        itemType = null;
                        reading = 't';
                        sb = new StringBuilder();
                        break;
                    default:
                        switch (reading) {
                            case 'n': // name
                                if (decode[r - 1] != '\\' && decode[r - 1] != ':' && decode[r] == '"') { // name ended
                                    name = sb.toString();
                                    sb = new StringBuilder();
                                }
                                break;
                            case 'f': // default
                                if (decode[r - 1] != '\\' && decode[r - 1] != ':' && decode[r] == '"') { // default ended
                                    def = sb.toString();
                                    sb = new StringBuilder();
                                }
                                break;
                            case 'i': // array of items
                                if (decode[r - 1] != '\\' && decode[r] == '}') { // array ended
                                    sb = new StringBuilder();
                                }
                                break;
                            case 'd': // item server id
                                if (decode[r - 1] != '\\' && decode[r - 1] != ':' && decode[r] == '"') { // id ended
                                    itemId = Long.parseLong(sb.toString());
                                    sb = new StringBuilder();
                                }
                                break;
                            case 'v': // item value
                                if (decode[r - 1] != '\\' && decode[r - 1] != ':' && decode[r] == '"') { // value ended
                                    itemVal = sb.toString();
                                    sb = new StringBuilder();
                                }
                                break;
                            case 't': // item type
                                if (decode[r - 1] != '\\' && decode[r - 1] != ':' && decode[r] == '"') { // type ended
                                    itemType = sb.toString();
                                    sb = new StringBuilder();
                                }
                                break;
                        }
                }
            }

            if (name != null && def != null) {
                register(name, def);
                def = null;
            }
            if (name != null && itemId != -1 && itemVal != null && itemType != null) {
                Object valueOf;
                try {
                    Class<?> forName = Class.forName(itemType);
                    if (forName == String.class) valueOf = itemVal;
                    else valueOf = forName.getMethod("valueOf", String.class).invoke(null, itemVal);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new AssertionError("Illegal structure for " + itemType + "#valueOf", e);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError("Wrong class forName: " + itemType + "; method valueOf not found", e);
                } catch (ClassNotFoundException e) {
                    throw new AssertionError("Wrong class forName: " + itemType + "; class not found", e);
                }
                getProperty(name).setValue(itemId).toObject(valueOf);
                itemId = -1;
                itemVal = null;
                itemType = null;
            }

            r++;
        }
    }
}
