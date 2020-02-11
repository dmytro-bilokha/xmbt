package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.api.BotFactory;
import com.dmytrobilokha.xmbt.api.Request;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.api.TextMessage;
import com.dmytrobilokha.xmbt.command.Command;
import com.dmytrobilokha.xmbt.command.CommandFactory;
import com.dmytrobilokha.xmbt.xmpp.XmppConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final Map<String, Command> commandMap;

    public BotManager(
            @Nonnull XmppConnector connector
            , @Nonnull BotRegistry botRegistry
            , @Nonnull CommandFactory commandFactory
    ) {
        this.connector = connector;
        this.botRegistry = botRegistry;
        this.commandMap = commandFactory
                .produceAll(botRegistry)
                .stream()
                .collect(Collectors.toUnmodifiableMap(Command::getName, command -> command));
    }

    public void go(@Nonnull Collection<BotFactory> botFactories) {
        boolean connectedOk = connectToMessagingServer();
        if (!connectedOk) {
            return;
        }
        botRegistry.startBots(botFactories);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                connector.ensureConnected();
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
        } catch (ConnectionException ex) {
            LOG.error("Failed to reconnect to the server", ex);
        } finally {
            connector.disconnect();
            LOG.info("Exiting...");
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
