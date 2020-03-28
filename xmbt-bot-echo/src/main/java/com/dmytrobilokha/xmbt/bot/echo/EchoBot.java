package com.dmytrobilokha.xmbt.bot.echo;

import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

class EchoBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(EchoBot.class);

    @Nonnull
    private final MessageBus messageQueueClient;

    EchoBot(@Nonnull MessageBus messageQueueClient) {
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
