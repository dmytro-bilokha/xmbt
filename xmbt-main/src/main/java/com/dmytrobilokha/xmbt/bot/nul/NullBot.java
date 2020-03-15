package com.dmytrobilokha.xmbt.bot.nul;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

class NullBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NullBot.class);

    @Nonnull
    private final BotConnector messageQueueClient;

    NullBot(@Nonnull BotConnector messageQueueClient) {
        this.messageQueueClient = messageQueueClient;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestMessage incomingMessage = messageQueueClient.getBlocking();
                LOG.debug("Got from queue incoming {}", incomingMessage);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        }
    }

}

