package de.comroid.javacord.util.commands;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.core.util.logging.LoggerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResponseManager implements Consumer<Message>, BiConsumer<Message, Duration>, Closeable {
    private static final Logger LOG = LoggerUtil.getLogger(ResponseManager.class);

    private final DiscordApi api;

    private final Collection<Long> deleteOnShutdown = new HashSet<>();
    private final Map<Long, Long> dependencyMap = new ConcurrentHashMap<>();

    public ResponseManager(DiscordApi api) {
        this.api = api;

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public void accept(final @NotNull Message message, @NotNull Message dependency) {
        dependency.addMessageDeleteListener(event -> message.delete().whenComplete((nil, thr) -> {
            if (thr != null)
                LOG.warn("Could not delete message on-dependency");
        }));
    }

    @Override
    public void accept(final @NotNull Message message, Duration timeToLive) {
        if (timeToLive.isZero())
            deleteOnShutdown.add(message.getId());
        else if (timeToLive.isNegative())
            message.delete().whenComplete((nil, thr) -> {
                if (thr != null)
                    LOG.warn("Could not delete message instantly", thr);
            });
        else {
            api.getThreadPool().getScheduler()
                    .schedule(() -> message.delete().whenComplete((nil, thr) -> {
                        if (thr != null)
                            LOG.warn("Could not delete message on-timeout", thr);
                    }), timeToLive.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void accept(@NotNull Message message) {
        accept(message, Duration.ZERO);
    }

    @Override
    public void close() {
        if (deleteOnShutdown.size() == 0)
            return;

        deleteOnShutdown.stream()
                .map(api::getCachedMessageById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(msg -> (Runnable) () -> msg.delete().whenComplete((nil, thr) -> {
                    if (thr != null)
                        LOG.warn("Could not delete message on-shutdown", thr);
                }))
                .forEach(api.getThreadPool().getDaemonScheduler()::execute);

        deleteOnShutdown.clear();
    }
}
