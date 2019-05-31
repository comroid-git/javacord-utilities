package de.kaleidox.javacord.util.test.dummy;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;

public class DiscordApiDummy extends DiscordApiImpl {
    private final List<MessageCreateListener> messageCreateListenerList = new ArrayList<>();

    public DiscordApiDummy() {
        super("", null, Proxy.NO_PROXY, (route, request, response) -> new HashMap<>(), true);
    }

    @Override
    public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener listener) {
        messageCreateListenerList.add(listener);
        return null;
    }

    @Override
    public List<MessageCreateListener> getMessageCreateListeners() {
        return messageCreateListenerList;
    }
}
