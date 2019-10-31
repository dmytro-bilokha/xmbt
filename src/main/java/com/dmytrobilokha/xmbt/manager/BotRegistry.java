package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.api.Bot;
import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.api.TextMessage;
import com.dmytrobilokha.xmbt.boot.Cleaner;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.dmytrobilokha.xmbt.manager.BotManager.BOT_NAME_PREFIX;

public class BotRegistry {

    @Nonnull
    private final Cleaner cleaner;
    @Nonnull
    private final BlockingQueue<TextMessage> fromUserMessageQueue = new LinkedBlockingQueue<>();
    @Nonnull
    private final BlockingQueue<ResponseMessage> fromBotsMessageQueue = new LinkedBlockingQueue<>();
    @Nonnull
    private final ConcurrentMap<String, BlockingQueue<RequestMessage>> toBotQueuesMap = new ConcurrentHashMap<>();

    public BotRegistry(@Nonnull Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    void startBots(@Nonnull Bot... bots) {
        for (Bot bot : bots) {
            var botName = BOT_NAME_PREFIX + bot.getName();
            var toBotQueue = new LinkedBlockingQueue<RequestMessage>();
            toBotQueuesMap.put(botName, toBotQueue);
            var messageQueueClient = new BotConnector(toBotQueue, fromBotsMessageQueue);
            bot.setConnector(messageQueueClient);
            var botThread = new Thread(bot);
            botThread.setName(botName);
            cleaner.registerThread(botThread);
            botThread.start();
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

    boolean enqueueRequestMessage(@Nonnull RequestMessage commandMessage) throws InterruptedException {
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
