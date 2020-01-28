package org.comroid.javacord.util.test.server.properties

import org.comroid.javacord.util.server.properties.GuildSettings
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.comroid.javacord.util.server.properties.GuildSettings.using
import static org.junit.Assert.assertEquals

class GuildSettingsTest {
    private File file = File.createTempFile("guildSettingsTest", ".json")

    @Before
    void setup() {
        GuildSettings settings = using file

        settings.registerProperty(prop -> prop
                .setType(String.class)
                .setName("bot.version")
                .setDefaultValue("5.2")
                .setPattern("\\d\\.\\d"))
        settings.properties("bot.version")
                .findAny()
                .orElseThrow(AssertionError::new)
                .setRawValue(100, "6.2")

        // todo: complete this unit test

        settings.register("bot.traits", 419)
                .setDisplayName("Traits")
                .setDescription("These are traits")
                .setValue(100).toInt(420)
        settings.getProperty("bot.traits").getValue(200)

        settings.register("bot.name", "William")
                .setDisplayName("Name")
                .setDescription("These are names")
                .setValue(100).toString("Alfred")
        settings.getProperty("bot.name").getValue(200)

        settings.register("bot.emoji", "\uD83C\uDF61")
                .setDisplayName("Emoji")
                .setDescription("These are emojis")
                .setValue(100).toString("\uD83D\uDD12")
        settings.getProperty("bot.emoji").getValue(200)

        settings.close()
    }

    @Test(timeout = 10000L)
    void testSerialization() throws IOException {
        GuildSettings deserializer = new GuildSettings(file)

        PropertyGroup traitsProperty = deserializer.register("bot.traits", 419)
        assertEquals 420, traitsProperty.getValue(100).asInt()
        assertEquals 419, traitsProperty.getValue(200).asInt()
        assertEquals 419, traitsProperty.getDefaultValue().asInt()
        assertEquals "Traits", traitsProperty.getDisplayName()
        assertEquals "These are traits", traitsProperty.getDescription()

        PropertyGroup nameProperty = deserializer.register("bot.name", "William")
        assertEquals "Alfred", nameProperty.getValue(100).stringValue()
        assertEquals "William", nameProperty.getValue(200).stringValue()
        assertEquals "William", nameProperty.getDefaultValue().stringValue()
        assertEquals "Name", nameProperty.getDisplayName()
        assertEquals "These are names", nameProperty.getDescription()

        PropertyGroup emojiProperty = deserializer.register("bot.emoji", "\uD83C\uDF61")
        assertEquals "\uD83D\uDD12", emojiProperty.getValue(100).stringValue()
        assertEquals "\uD83C\uDF61", emojiProperty.getValue(200).stringValue()
        assertEquals "\uD83C\uDF61", emojiProperty.getDefaultValue().stringValue()
        assertEquals "Emoji", emojiProperty.getDisplayName()
        assertEquals "These are emojis", emojiProperty.getDescription()
    }

    @After
    void cleanup() {
        file.delete()
    }
}
