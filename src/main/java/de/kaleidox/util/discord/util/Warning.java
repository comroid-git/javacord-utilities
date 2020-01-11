// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class Warning {
    private static final ConcurrentHashMap<Messageable, Long> timeoutMap;

    static {
        timeoutMap = new ConcurrentHashMap<Messageable, Long>();
    }

    public Warning(final Messageable parent, final String text, final EmbedBuilder baseEmbed, final long timeout, final TimeUnit timeUnit) {
        if (!Warning.timeoutMap.containsKey(parent)) {
            parent.sendMessage(baseEmbed.addField("Warning:", text));
            Warning.timeoutMap.put(parent, System.nanoTime() + timeUnit.toNanos(timeout));
        } else if (Warning.timeoutMap.get(parent) < System.nanoTime()) {
            parent.sendMessage(baseEmbed.addField("Warning:", text));
            Warning.timeoutMap.replace(parent, System.nanoTime() + timeUnit.toNanos(timeout));
        }
    }
}
