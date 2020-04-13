package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import com.dmytrobilokha.xmbt.api.messaging.TextMessage;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;
import com.dmytrobilokha.xmbt.boot.Cleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.dmytrobilokha.xmbt.manager.BotManager.BOT_NAME_PREFIX;

public class BotRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(BotRegistry.class);

    @Nonnull
    private final Cleaner cleaner;
    @Nonnull
    private final ServiceContainer serviceContainer;
    @Nonnull
    private final BlockingQueue<TextMessage> fromUserMessageQueue = new LinkedBlockingQueue<>();
    @Nonnull
    private final BlockingQueue<ResponseMessage> fromBotsMessageQueue = new LinkedBlockingQueue<>();
    @Nonnull
    private final ConcurrentMap<String, BlockingQueue<RequestMessage>> toBotQueuesMap = new ConcurrentHashMap<>();

    public BotRegistry(@Nonnull ServiceContainer serviceContainer, @Nonnull Cleaner cleaner) {
        this.serviceContainer = serviceContainer;
        this.cleaner = cleaner;
    }

    void startBots(@Nonnull Collection<BotFactory> botFactories) {
        for (BotFactory botFactory : botFactories) {
            var botName = "?";
            try {
                botName = BOT_NAME_PREFIX + botFactory.getBotName();
                var toBotQueue = new LinkedBlockingQueue<RequestMessage>();
                toBotQueuesMap.put(botName, toBotQueue);
                var messageQueueClient = new BotConnector(toBotQueue, fromBotsMessageQueue);
                var botThread = new Thread(botFactory.produce(messageQueueClient, serviceContainer));
                botThread.setName(botName);
                cleaner.registerThread(botThread);
                botThread.start();
            } catch (RuntimeException ex) {
                LOG.error("Failed to initialize bot '{}', skip it", botName, ex);
            }
        }
    }

    @CheckForNull
    TextMessage pollMessageFromUser() {
        return fromUserMessageQueue.poll();
    }

    @CheckForNull
    ResponseMessage pollMessageFromBots() {
        return fromBotsMessageQueue.poll();
    }

    public boolean enqueueRequestMessage(@Nonnull RequestMessage commandMessage) throws InterruptedException {
        BlockingQueue<RequestMessage> botQueue = toBotQueuesMap.get(commandMessage.getReceiver());
        if (botQueue == null) {
            return false;
        }
        botQueue.put(commandMessage);
        return true;
    }

    public boolean enqueueResponseMessage(@Nonnull ResponseMessage responseMessage) throws InterruptedException {
        fromBotsMessageQueue.put(responseMessage);
        return true;
    }

    public void enqueueMessageFromUser(@Nonnull TextMessage message) throws InterruptedException {
        fromUserMessageQueue.put(message);
    }

    public Set<String> getBotNames() {
        return Set.copyOf(toBotQueuesMap.keySet());
    }

}
