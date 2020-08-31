package com.ldtteam.aequivaleo.network.splitting;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.Lists;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.network.messages.IMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NetworkSplittingManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final NetworkSplittingManager INSTANCE = new NetworkSplittingManager();

    public static NetworkSplittingManager getInstance()
    {
        return INSTANCE;
    }

    private final AtomicInteger                  messageCounter = new AtomicInteger();
    private final Cache<Integer, List<IMessage>> messageCache   = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build();


    private NetworkSplittingManager()
    {
    }

    public <T, M extends IMessage, E extends IMessage> void sendSplit(
      @NotNull final List<T> source,
      @NotNull final BiFunction< Integer, List<T>, M> messageBuilder,
      @NotNull final Function<Integer, E> terminationMessageProducer,
      @NotNull final Consumer<IMessage> messageSender
    ) {
        final int communicationId = messageCounter.incrementAndGet();
        if (Aequivaleo.getInstance().getConfiguration().getCommon().networkBatchingSize.get() <= 0) {
            messageSender.accept(messageBuilder.apply(communicationId, source));
            messageSender.accept(terminationMessageProducer.apply(communicationId));
        }

        final int batchSize = Aequivaleo.getInstance().getConfiguration().getCommon().networkBatchingSize.get();
        final int messageCount = (int) Math.ceil(source.size() / (double) batchSize);
        for (int listIndex = 0; listIndex < messageCount; listIndex++)
        {
            List<T> subSource = source.subList(batchSize * listIndex, Math.min(source.size(), batchSize * (listIndex + 1)));
            messageSender.accept(messageBuilder.apply(communicationId, subSource));
        }
        messageSender.accept(terminationMessageProducer.apply(communicationId));
    }

    public void receivedPartialMessage(
      final int communicationId,
      final IMessage message
    ) {
        try
        {
            messageCache.get(communicationId, Lists::newArrayList).add(message);
        }
        catch (ExecutionException e)
        {
            LOGGER.error("Failed to store a partial received message. Some things might not work as expected.", e);
        }
    }

    public <T extends IMessage, D> List<D> onMessageFinalized(
      final int communicationId,
      final Class<T> dataType,
      final Function<T, List<D>> dataExtractor
    ) {
        try
        {
            final List<T> partialPackages = messageCache.get(communicationId, Lists::newArrayList).stream()
              .filter(dataType::isInstance)
              .map(dataType::cast)
              .collect(Collectors.toList());

            messageCache.invalidate(communicationId);
            return partialPackages.stream().map(dataExtractor).flatMap(List::stream).collect(Collectors.toList());
        }
        catch (ExecutionException e)
        {
            LOGGER.error("Failed to finalize a partial received message. Some things might not work as expected.", e);
            return Lists.newArrayList();
        }
    }
}
