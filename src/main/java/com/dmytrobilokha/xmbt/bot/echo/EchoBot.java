package com.dmytrobilokha.xmbt.bot.echo;

import com.dmytrobilokha.xmbt.api.Bot;
import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class EchoBot implements Bot {

    private static final Logger LOG = LoggerFactory.getLogger(EchoBot.class);

    private BotConnector messageQueueClient;

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public void setConnector(@Nonnull BotConnector messageQueueClient) {
        this.messageQueueClient = messageQueueClient;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestMessage incomingMessage = messageQueueClient.getBlocking();
                LOG.debug("Got from queue incoming {}", incomingMessage);
                messageQueueClient.sendBlocking(new ResponseMessage(
                        incomingMessage, Response.OK, incomingMessage.getTextMessage().getText()));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        }
    }

}
