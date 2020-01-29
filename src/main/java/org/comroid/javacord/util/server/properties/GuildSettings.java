package org.comroid.javacord.util.server.properties;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.comroid.javacord.util.commands.Command;
import org.comroid.javacord.util.commands.CommandGroup;
import org.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import org.comroid.javacord.util.ui.messages.paging.PagedEmbed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
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

    @Command(
            aliases = "property",
            description = "Access server-based properties",
            usage = "property [<filter> | <property-name> <new value>]",
            ordinal = 5000,
            maximumArguments = 2,
            enablePrivateChat = false,
            requiredDiscordPermissions = PermissionType.MANAGE_SERVER,
            convertStringResultsToEmbed = true,
            useTypingIndicator = true
    )
    @CommandGroup(name = "Basic Commands", description = "All commands for basic interaction with the bot. `@default` resets the property")
    public Object property(Server server, User user, ServerTextChannel stc, String[] args) {
        final EmbedBuilder embed = DefaultEmbedFactory.create(server, user);

        switch (args.length) {
            case 0: // list all
                final List<Property> properties = properties("").collect(Collectors.toList());

                // populate embed with all properties
                embed.setDescription("All Properties:");
                for (Property property : properties)
                    embed.addField(
                            String.format("var %s = %s;", property.getName(), property.getValue(server).getRaw()),
                            String.format("%s\nDefault Value: %s",
                                    property.getDescription().orElse("No description set."),
                                    property.getDefaultValue().getRaw()));

                break;
            case 1: // property filter
                final List<Property> yields = properties(args[0]).collect(Collectors.toList());

                // populate embed with all yields
                embed.setDescription(String.format("Properties matching `%s`", args[0]));
                for (Property property : yields)
                    embed.addField(
                            String.format("var %s = %s;", property.getName(), property.getValue(server).getRaw()),
                            String.format("%s\nDefault Value: %s",
                                    property.getDescription().orElse("No description set."),
                                    property.getDefaultValue().getRaw()));

                break;
            default: // set property
                final Property property = property(args[0]).orElseThrow(() ->
                        new NoSuchElementException("No Property with name " + args[0] + " could be found!"));

                // overwrite value
                if (property.setRawValue(server.getId(), args[1])) {
                    // successful
                    embed.setDescription(String.format("Property `%s` was updated!", property.getName()))
                            .addField(
                                    String.format("var %s = %s;", property.getName(), property.getValue(server).getRaw()),
                                    String.format("%s\nDefault Value: %s",
                                            property.getDescription().orElse("No description set."),
                                            property.getDefaultValue().getRaw()));
                } else {
                    // unsuccessful
                    embed.setDescription(String.format("Property `%s` could not be updated; input must match regular expression: ```%s```",
                            property.getName(), property.getPattern().pattern()))
                            .addField(
                                    String.format("var %s = %s;", property.getName(), property.getValue(server).getRaw()),
                                    String.format("%s\nDefault Value: %s",
                                            property.getDescription().orElse("No description set."),
                                            property.getDefaultValue().getRaw()));
                }

                break;
        }

        final PagedEmbed pagedEmbed = new PagedEmbed(stc, () -> embed);
        embed.updateAllFields(field -> pagedEmbed.addField(field.getName(), field.getValue(), false))
                .removeAllFields();

        return pagedEmbed;
    }

    @Override
    public void close() throws IOException {
        storeData();
    }

    public void storeData() throws IOException {
        final String json = serialize().toString();

        final File tmpFile = File.createTempFile("GuildSettings-" + System.currentTimeMillis(), ".json.tmp");
        final FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);

        for (byte aByte : json.getBytes(StandardCharsets.UTF_8))
            fileOutputStream.write(aByte);
        fileOutputStream.close();

        if (!file.exists() || (file.exists() && file.delete()))
            tmpFile.renameTo(file);
        else throw new IOException("Could not delete previous file!");
    }

    public Optional<Property> property(String name) {
        return properties(name)
                .filter(prop -> prop.getName().equals(name))
                .findAny();
    }

    public Stream<Property> properties(String thatStartWith) {
        return properties.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(thatStartWith))
                .map(Map.Entry::getValue);
    }

    public GuildSettings registerProperty(Consumer<Property.Builder> propertySetup) {
        final Property.Builder builder = new Property.Builder(this);

        propertySetup.accept(builder);

        try {
            properties.compute(builder.getName(), (key, prop) -> {
                try {
                    if (prop == null)
                        return builder.build();
                    else return prop.rebuild(builder);
                } catch (NoSuchMethodException | ClassNotFoundException e) {
                    throw new RuntimeException("Could not create Property", e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error registering property [ " + builder.getName() + " ]", e);
        }

        return this;
    }

    private JsonNode serialize() {
        final ArrayNode data = JsonNodeFactory.instance.arrayNode();

        properties.forEach((key, property) -> data.add(property.serialize()));

        return data;
    }

    public static GuildSettings using(File file) throws IOException {
        JsonNode data = file.exists() ? new ObjectMapper().readTree(file) : JsonNodeFactory.instance.arrayNode();

        // if data is still null, set it to an empty array
        if (data == null) data = JsonNodeFactory.instance.arrayNode();

        try {
            return new GuildSettings(file, data);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new IOException("An exception occurred while deserializing", e);
        }
    }
}
