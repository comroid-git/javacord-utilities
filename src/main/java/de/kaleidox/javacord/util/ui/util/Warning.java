package de.kaleidox.javacord.util.ui.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class Warning {
    private static final ConcurrentHashMap<Messageable, Long> timeoutMap = new ConcurrentHashMap<>();

    public Warning(Messageable parent, String text, EmbedBuilder baseEmbed, long timeout, TimeUnit timeUnit) {
        if (!timeoutMap.containsKey(parent)) {
            parent.sendMessage(baseEmbed.addField("Warning:", text));
            timeoutMap.put(parent, System.nanoTime() + timeUnit.toNanos(timeout));
        } else {
            if (timeoutMap.get(parent) < System.nanoTime()) {
                parent.sendMessage(baseEmbed.addField("Warning:", text));
                timeoutMap.replace(parent, System.nanoTime() + timeUnit.toNanos(timeout));
            }
        }
    }
}
