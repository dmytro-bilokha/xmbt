package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.boot.Cleaner;
import com.dmytrobilokha.xmbt.bot.echo.EchoBot;
import com.dmytrobilokha.xmbt.xmpp.TextMessage;
import com.dmytrobilokha.xmbt.xmpp.XmppConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class BotManager {

    private static final char BOTNAME_PREFIX = '@';
    private static final Logger LOG = LoggerFactory.getLogger(BotManager.class);

    @Nonnull
    private final XmppConnector connector;
    @Nonnull
    private final Cleaner cleaner;
    @Nonnull
    private final BlockingQueue<TextMessage> toUserMessageQueue = new LinkedBlockingQueue<>();
    @Nonnull
    private final BlockingQueue<TextMessage> fromUserMessageQueue = new LinkedBlockingQueue<>();
    @Nonnull
    private final ConcurrentMap<String, BlockingQueue<TextMessage>> userToBotQueuesMap = new ConcurrentHashMap<>();

    public BotManager(@Nonnull XmppConnector connector, @Nonnull Cleaner cleaner) {
        this.connector = connector;
        this.cleaner = cleaner;
    }

    public void start() {
        connector.setIncomingMessagesQueue(fromUserMessageQueue);
        try {
            LOG.info("Trying to connect to the messaging server");
            connector.connect();
            LOG.info("Successfully connected");
        } catch (ConnectionException ex) {
            LOG.error("Failed to connect to the messaging server, exiting", ex);
            return;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.warn("Got interruption during connecting to the server, exiting", ex);
            return;
        }
        //Init echobot
        Bot bot = new EchoBot();
        String botName = BOTNAME_PREFIX + bot.getName();
        BlockingQueue<TextMessage> botsQueue = new LinkedBlockingQueue<>();
        userToBotQueuesMap.put(botName, botsQueue);
        DuplexMessageQueueClient messageQueueClient = new DuplexMessageQueueClient(botsQueue, toUserMessageQueue);
        bot.setMessageQueueClient(messageQueueClient);
        Thread echoBotThread = new Thread(bot);
        echoBotThread.setName(botName);
        cleaner.registerThread(echoBotThread);
        cleaner.registerThread(Thread.currentThread());
        echoBotThread.start();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                TextMessage messageForUser = toUserMessageQueue.poll();
                if (messageForUser != null) {
                    try {
                        connector.sendMessage(messageForUser);
                    } catch (InvalidAddressException ex) {
                        LOG.error("Failed to send {}", messageForUser, ex);
                        return;
                    } catch (InvalidConnectionStateException ex) {
                        LOG.error("Got invalid connection state during trying to send {}", messageForUser, ex);
                        return;
                    }
                }
                TextMessage messageForBot = fromUserMessageQueue.poll();
                if (messageForBot != null) {
                    dispatchMessage(messageForBot);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption signal", ex);
        } catch (RuntimeException ex) {
            LOG.error("Got unexpected exception", ex);
        } finally {
            LOG.info("Exiting...");
            connector.disconnect();
        }
    }

    private void dispatchMessage(@Nonnull TextMessage incomingMessage) throws InterruptedException {
        String messageText = incomingMessage.getText().trim();
        if (messageText.isEmpty()) {
            LOG.debug("Got message {} with empty payload, will ignore it", incomingMessage);
            return;
        }
        int spaceIndex = messageText.indexOf(' ');
        String firstWord;
        String restOfMessage;
        if (spaceIndex < 0) {
            //The message contains only one word
            firstWord = messageText;
            restOfMessage = "";
        } else {
            firstWord = messageText.substring(0, spaceIndex);
            restOfMessage = messageText.substring(spaceIndex);
        }
        if (firstWord.length() > 1 && firstWord.charAt(0) == BOTNAME_PREFIX) {
            passMessageToBot(firstWord, new TextMessage(incomingMessage.getAddress(), restOfMessage));
            return;
        }
        executeCommandFromMessage(incomingMessage);
        BlockingQueue<TextMessage> botInQueue = userToBotQueuesMap.get(firstWord);
        if (botInQueue == null) {
            LOG.warn("For '{}' got a message {}, but don't have such bot in the registry", firstWord, incomingMessage);
            return;
        }
        TextMessage rewritten = new TextMessage(incomingMessage.getAddress(), restOfMessage);
        botInQueue.put(rewritten);
    }

    private void passMessageToBot(@Nonnull String botName, @Nonnull TextMessage message) throws InterruptedException {
        BlockingQueue<TextMessage> botIncomingMessageQueue = userToBotQueuesMap.get(botName);
        if (botIncomingMessageQueue == null) {
            toUserMessageQueue.put(new TextMessage(message.getAddress()
                    , "Don't have any bot with name '" + botName + "'"));
            return;
        }
        botIncomingMessageQueue.put(message);
    }

    private void executeCommandFromMessage(TextMessage message) throws InterruptedException {
        String[] messageWords = message.getText().split(" +");
        String command = messageWords[0];
        String responseText;
        if ("list".equals(command)) {
            responseText = "Have following bots initialized:" + System.lineSeparator()
                + userToBotQueuesMap.keySet().stream().collect(Collectors.joining(System.lineSeparator()));
        } else {
            responseText = "Don't recognize command '" + command + "'";
        }
        toUserMessageQueue.put(new TextMessage(message.getAddress(), responseText));
    }

}
