package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.api.Persistable;
import com.dmytrobilokha.xmbt.api.Request;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.api.TextMessage;
import com.dmytrobilokha.xmbt.boot.Cleaner;
import com.dmytrobilokha.xmbt.bot.echo.EchoBot;
import com.dmytrobilokha.xmbt.command.Command;
import com.dmytrobilokha.xmbt.command.CommandFactory;
import com.dmytrobilokha.xmbt.persistence.PersistenceService;
import com.dmytrobilokha.xmbt.xmpp.XmppConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BotManager {

    public static final char BOT_NAME_PREFIX = '@';
    private static final String ROOT_NAME = "ROOT";
    private static final Pattern WORDS_SPLIT_PATTERN = Pattern.compile(" +");
    private static final Logger LOG = LoggerFactory.getLogger(BotManager.class);

    private long currentMessageId = 1;

    @Nonnull
    private final XmppConnector connector;
    @Nonnull
    private final BotRegistry botRegistry;
    @Nonnull
    private final PersistenceService persistenceService;
    @Nonnull
    private final Map<String, Command> commandMap;

    public BotManager(
            @Nonnull XmppConnector connector
            , @Nonnull BotRegistry botRegistry
            , @Nonnull CommandFactory commandFactory
            , @Nonnull PersistenceService persistenceService
            , @Nonnull Cleaner cleaner
    ) {
        this.connector = connector;
        this.botRegistry = botRegistry;
        this.persistenceService = persistenceService;
        this.commandMap = new HashMap<>();
        for (Command command : commandFactory.produceAll(botRegistry)) {
            commandMap.put(command.getName(), command);
        }
        cleaner.registerHook(this::dumpCommandStates);
    }

    public void go() {
        for (Command command : commandMap.values()) {
            if (command instanceof Persistable) {
                try {
                    LOG.info("Loading state for '{}'", command);
                    persistenceService.loadState((Persistable) command);
                } catch (IOException ex) {
                    LOG.error("Failed to load state for command '{}'", command, ex);
                }
            }
        }
        boolean connectedOk = connectToMessagingServer();
        if (!connectedOk) {
            return;
        }
        botRegistry.startBots(new EchoBot());
        try {
            while (!Thread.currentThread().isInterrupted()) {
                processOutgoingQueue();
                processIncomingQueue();
                for (Command command : commandMap.values()) {
                    command.tick();
                }
                Thread.sleep(100L);
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

    //TODO: is this thread-safe??? Make commands map immutable, and control that commands are also safe
    private void dumpCommandStates() {
        for (Command command : commandMap.values()) {
            if (command instanceof Persistable) {
                try {
                    LOG.info("Persisting state for '{}'", command);
                    persistenceService.saveState((Persistable) command);
                } catch (IOException ex) {
                    LOG.error("Failed to save state for command '{}'", command, ex);
                }
            }
        }
    }

    private boolean connectToMessagingServer() {
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

    private void processOutgoingQueue() throws InterruptedException {
        var messageFromBot = botRegistry.pollMessageFromBots();
        if (messageFromBot == null) {
            return;
        }
        if (ROOT_NAME.equals(messageFromBot.getReceiver())) {
            try {
                var messageToUser = messageFromBot.getSender().indexOf(BOT_NAME_PREFIX) == 0
                        ? messageFromBot.getTextMessage().withNewText(
                        messageFromBot.getSender() + " says:" + System.lineSeparator()
                                + messageFromBot.getTextMessage().getText())
                        : messageFromBot.getTextMessage();
                connector.sendMessage(messageToUser);
            } catch (InvalidAddressException ex) {
                Thread.currentThread().interrupt();
                LOG.error("Failed to send {}", messageFromBot, ex);
            } catch (InvalidConnectionStateException ex) {
                Thread.currentThread().interrupt();
                LOG.error("Got invalid connection state during trying to send {}", messageFromBot, ex);
            }
            return;
        }
        Command command = commandMap.get(messageFromBot.getReceiver());
        if (command == null) {
            LOG.error("Cannot find command {} which should receive message {}"
                    , messageFromBot.getReceiver(), messageFromBot);
            return;
        }
        command.acceptResponse(messageFromBot);
    }

    private void processIncomingQueue() throws InterruptedException {
        TextMessage incomingMessage = botRegistry.pollMessageFromUser();
        if (incomingMessage != null) {
            dispatchMessage(incomingMessage);
        }
    }

    private void dispatchMessage(@Nonnull TextMessage incomingMessage) throws InterruptedException {
        var messageText = incomingMessage.getText().stripLeading();
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
        var messageParts = WORDS_SPLIT_PATTERN.split(message.getText().stripLeading(), 2);
        var botName = messageParts[0];
        var restOfMessage = messageParts.length > 1 ? messageParts[1] : "";
        var requestMessage = new RequestMessage(
                currentMessageId++
                , ROOT_NAME
                , botName
                , Request.RESPOND
                , new TextMessage(message.getAddress(), restOfMessage)
        );
        if (!botRegistry.enqueueRequestMessage(requestMessage)) {
            botRegistry.enqueueResponseMessage(
                    new ResponseMessage(requestMessage, Response.INVALID_RECEIVER
                            , "Don't have any bot with name '" + botName + "'"));
        }
    }

    private void executeCommandFromMessage(@Nonnull TextMessage message) throws InterruptedException {
        var commandName = WORDS_SPLIT_PATTERN.split(message.getText(), 2)[0];
        var requestMessage = new RequestMessage(
                currentMessageId++
                , ROOT_NAME
                , commandName
                , Request.RESPOND
                , message
        );
        Command command = commandMap.get(commandName);
        if (command == null) {
            botRegistry.enqueueResponseMessage(
                    new ResponseMessage(requestMessage, Response.INVALID_RECEIVER
                            , "Don't recognize command '" + commandName + "'"));
            return;
        }
        command.acceptRequest(requestMessage);
    }

}
