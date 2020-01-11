// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.listeners;

import java.util.List;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.entity.message.Message;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.event.message.MessageDeleteEvent;

public final class MessageListeners
{
    public static void deleteCleanup(final MessageDeleteEvent messageDeleteEvent) {
        messageDeleteEvent.getMessage().ifPresent(message -> message.getMessageAttachableListeners().forEach((key, value) -> message.removeMessageAttachableListener((MessageAttachableListener)key)));
    }
}
