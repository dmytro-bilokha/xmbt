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

    private static final char BOT_NAME_PREFIX = '@';
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
        boolean connectedOk = connectToMessagingServer();
        if (!connectedOk) {
            return;
        }
        cleaner.registerThread(Thread.currentThread());
        startBots(new EchoBot());
        try {
            while (!Thread.currentThread().isInterrupted()) {
                processOutgoingQueue();
                processIncomingQueue();
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

    private boolean connectToMessagingServer() {
        connector.setIncomingMessagesQueue(fromUserMessageQueue);
        try {
            LOG.info("Trying to connect to the messaging server");
            connector.connect();
            LOG.info("Successfully connected");
        } catch (ConnectionException ex) {
            LOG.error("Failed to connect to the messaging server, exiting", ex);
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.warn("Got interruption during connecting to the server, exiting", ex);
            return false;
        }
        return true;
    }

    private void startBots(@Nonnull Bot... bots) {
        for (Bot bot : bots) {
            String botName = BOT_NAME_PREFIX + bot.getName();
            BlockingQueue<TextMessage> botsQueue = new LinkedBlockingQueue<>();
            userToBotQueuesMap.put(botName, botsQueue);
            DuplexMessageQueueClient messageQueueClient = new DuplexMessageQueueClient(botsQueue, toUserMessageQueue);
            bot.setMessageQueueClient(messageQueueClient);
            Thread botThread = new Thread(bot);
            botThread.setName(botName);
            cleaner.registerThread(botThread);
            botThread.start();
        }
    }

    private void processOutgoingQueue() {
        TextMessage messageForUser = toUserMessageQueue.poll();
        if (messageForUser != null) {
            try {
                connector.sendMessage(messageForUser);
            } catch (InvalidAddressException ex) {
                Thread.currentThread().interrupt();
                LOG.error("Failed to send {}", messageForUser, ex);
            } catch (InvalidConnectionStateException ex) {
                Thread.currentThread().interrupt();
                LOG.error("Got invalid connection state during trying to send {}", messageForUser, ex);
            }
        }
    }

    private void processIncomingQueue() throws InterruptedException {
        TextMessage messageForBot = fromUserMessageQueue.poll();
        if (messageForBot != null) {
            dispatchMessage(messageForBot);
        }
    }

    private void dispatchMessage(@Nonnull TextMessage incomingMessage) throws InterruptedException {
        String messageText = incomingMessage.getText().trim();
        if (messageText.isEmpty()) {
            LOG.debug("Got message {} with empty payload, will ignore it", incomingMessage);
            return;
        }
        if (messageText.charAt(0) == BOT_NAME_PREFIX) {
            passMessageToBot(incomingMessage);
        } else {
            executeCommandFromMessage(incomingMessage);
        }
    }

    private void passMessageToBot(@Nonnull TextMessage message) throws InterruptedException {
        String messageText = message.getText().trim();
        int spaceIndex = messageText.indexOf(' ');
        String botName;
        String restOfMessage;
        if (spaceIndex < 0) {
            //The message contains only one word
            botName = messageText;
            restOfMessage = "";
        } else {
            botName = messageText.substring(0, spaceIndex);
            restOfMessage = messageText.substring(spaceIndex);
        }
        BlockingQueue<TextMessage> botIncomingMessageQueue = userToBotQueuesMap.get(botName);
        if (botIncomingMessageQueue == null) {
            toUserMessageQueue.put(new TextMessage(message.getAddress()
                    , "Don't have any bot with name '" + botName + "'"));
            return;
        }
        botIncomingMessageQueue.put(new TextMessage(message.getAddress(), restOfMessage));
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
