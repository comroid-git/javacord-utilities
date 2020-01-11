// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.listeners;

import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.listener.message.MessageAttachableListener;

public final class MessageListeners {
    public static void deleteCleanup(final MessageDeleteEvent messageDeleteEvent) {
        messageDeleteEvent.getMessage().ifPresent(message -> message.getMessageAttachableListeners().forEach((key, value) -> message.removeMessageAttachableListener((MessageAttachableListener) key)));
    }
}
