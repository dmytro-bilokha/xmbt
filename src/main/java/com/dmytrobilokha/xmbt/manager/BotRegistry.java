package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.boot.Cleaner;
import com.dmytrobilokha.xmbt.xmpp.TextMessage;

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
    private final BlockingQueue<TextMessage> toUserMessageQueue = new LinkedBlockingQueue<>();
    @Nonnull
    private final BlockingQueue<TextMessage> fromUserMessageQueue = new LinkedBlockingQueue<>();
    @Nonnull
    private final ConcurrentMap<String, BlockingQueue<TextMessage>> userToBotQueuesMap = new ConcurrentHashMap<>();

    public BotRegistry(@Nonnull Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    void startBots(@Nonnull Bot... bots) {
        for (Bot bot : bots) {
            var botName = BOT_NAME_PREFIX + bot.getName();
            var botsQueue = new LinkedBlockingQueue<TextMessage>();
            userToBotQueuesMap.put(botName, botsQueue);
            var botMessagePrefix = botName + " says:" + System.lineSeparator();
            var messageQueueClient = new BotConnector(botsQueue, toUserMessageQueue, botMessagePrefix);
            bot.setConnector(messageQueueClient);
            var botThread = new Thread(bot);
            botThread.setName(botName);
            cleaner.registerThread(botThread);
            botThread.start();
        }
    }

    @Nonnull
    BlockingQueue<TextMessage> getIncomingMessagesQueue() {
        return fromUserMessageQueue;
    }

    @CheckForNull
    TextMessage pollIncomingMessagesQueue() {
        return fromUserMessageQueue.poll();
    }

    @CheckForNull
    TextMessage pollOutgoingMessagesQueue() {
        return toUserMessageQueue.poll();
    }

    boolean enqueueMessageForBot(
            @Nonnull String botname, @Nonnull TextMessage message) throws InterruptedException {
        BlockingQueue<TextMessage> botQueue = userToBotQueuesMap.get(botname);
        if (botQueue == null) {
            return false;
        }
        botQueue.put(message);
        return true;
    }

    public void enqueueMessageForUser(@Nonnull TextMessage message) throws InterruptedException {
        toUserMessageQueue.put(message);
    }

    public void enqueueIncomingMessage(@Nonnull TextMessage message) throws InterruptedException {
        fromUserMessageQueue.put(message);
    }

    public Set<String> getBotNames() {
        return Set.copyOf(userToBotQueuesMap.keySet());
    }

}
