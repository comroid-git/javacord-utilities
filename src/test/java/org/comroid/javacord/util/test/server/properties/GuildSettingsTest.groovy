package org.comroid.javacord.util.test.server.properties

import org.comroid.javacord.util.server.properties.GuildSettings
import org.comroid.javacord.util.server.properties.Property
import org.junit.Before
import org.junit.Test

import static org.comroid.javacord.util.server.properties.GuildSettings.using
import static org.junit.Assert.assertEquals

class GuildSettingsTest {
    private File file

    {
        file = File.createTempFile("guildSettingsTest", ".json")
        file.deleteOnExit()

        System.out.printf "Using temporary file %s for UnitTest\n", file.absolutePath
    }

    @Before
    void setup() {
        GuildSettings settings = using file

        settings.registerProperty(prop -> prop
                .setType(String.class)
                .setName("bot.version")
                .setDefaultValue("5.2")
                .setPattern("\\d\\.\\d"))
        settings.property("bot.version")
                .orElseThrow()
                .setRawValue(100, "6.2")

        settings.registerProperty(prop -> prop
                .setType(String.class)
                .setName("bot.name")
                .setDefaultValue("william")
                .setPattern(Property.ANY_STRING))
        settings.property("bot.name")
                .orElseThrow()
                .setRawValue(100, "rufus")

        settings.registerProperty(prop -> prop
                .setType(Integer.class)
                .setName("bot.counter")
                .setDefaultValue(500 as String)
                .setPattern("\\d+"))
        settings.property("bot.counter")
                .orElseThrow()
                .setRawValue(100, 200)

        settings.close()
    }

    @Test(timeout = 10000L)
    void testDeserialization() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line
            while ((line = br.readLine()) != null) {
                System.out.println(line)
            }
        }

        GuildSettings settings = using file

        def versionProp = settings.property("bot.version").orElseThrow()
        def version100 = versionProp
                .getValue(100)
                .asString()
        def version200 = versionProp
                .getValue(200)
                .asString()
        def version300 = versionProp
                .getValue(300)
                .asString("2.3")

        def nameProp = settings.property("bot.name").orElseThrow()
        def name100 = nameProp
                .getValue(100)
                .asString()
        def name200 = nameProp
                .getValue(200)
                .asString()
        def name300 = nameProp
                .getValue(300)
                .asString("jack")

        def counterProp = settings.property("bot.counter").orElseThrow()
        def counter100 = counterProp
                .getValue(100)
                .asInt()
        def counter200 = counterProp
                .getValue(200)
                .asInt()
        def counter300 = counterProp
                .getValue(300)
                .asInt(400 as String)

        assertEquals "6.2", version100
        assertEquals "5.2", version200
        assertEquals "2.3", version300

        assertEquals "rufus", name100
        assertEquals "william", name200
        assertEquals "jack", name300

        assertEquals 200, counter100
        assertEquals 500, counter200
        assertEquals 400, counter300
    }
}
