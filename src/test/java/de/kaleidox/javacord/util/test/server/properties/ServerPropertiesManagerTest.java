package de.kaleidox.javacord.util.test.server.properties;

import java.io.File;
import java.io.IOException;

import de.kaleidox.javacord.util.server.properties.ServerPropertiesManager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServerPropertiesManagerTest {

    private ServerPropertiesManager manager;

    @Before
    public void setup() throws IOException {
        manager = new ServerPropertiesManager(new File("props/serverProperties.sav"));

        manager.register("bot.traits", 419).setValue(100).toInt(420);
        manager.getProperty("bot.traits").getValue(200);

        manager.register("bot.name", "William").setValue(100).toString("Alfred");
        manager.getProperty("bot.name").getValue(200);
    }

    @Test
    public void testSerialization() throws IOException {
        manager.storeData();

        ServerPropertiesManager deserializer = new ServerPropertiesManager(new File("props/serverProperties.sav"));

        assertEquals(420, deserializer.getProperty("bot.traits").getValue(100).asInt());
        assertEquals(419, deserializer.getProperty("bot.traits").getValue(200).asInt());

        assertEquals("Alfred", deserializer.getProperty("bot.name").getValue(100).asString());
        assertEquals("William", deserializer.getProperty("bot.name"). getValue(200).asString());
    }
}
