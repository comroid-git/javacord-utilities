// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.libs.listeners;

import java.util.List;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.entity.message.Message;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.listener.message.MessageDeleteListener;

public final class MessageListeners
{
    public static final MessageDeleteListener MESSAGE_DELETE_CLEANUP;
    
    static {
        MESSAGE_DELETE_CLEANUP = (event -> event.getMessage().ifPresent(message -> message.getMessageAttachableListeners().forEach((key, value) -> message.removeMessageAttachableListener((MessageAttachableListener)key))));
    }
}
