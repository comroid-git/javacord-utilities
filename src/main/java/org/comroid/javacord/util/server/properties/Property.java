package org.comroid.javacord.util.server.properties;

import java.util.concurrent.atomic.AtomicReference;

import org.comroid.javacord.util.model.ValueContainer;

import org.intellij.lang.annotations.Language;
import org.javacord.api.entity.Nameable;

public final class Property implements Nameable {
    private final AtomicReference<ValueContainer>
    private final @Language("RegExp") String pattern;

    public static class Builder {
        Builder(GuildSettings parent) {

        }

        Property build() {
            return null;
        }
    }
}
