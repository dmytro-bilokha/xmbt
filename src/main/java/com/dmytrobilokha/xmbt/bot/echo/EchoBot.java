package com.dmytrobilokha.xmbt.bot.echo;

import com.dmytrobilokha.xmbt.manager.Bot;
import com.dmytrobilokha.xmbt.manager.DuplexMessageQueueClient;
import com.dmytrobilokha.xmbt.xmpp.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoBot implements Bot {

    private static final Logger LOG = LoggerFactory.getLogger(EchoBot.class);

    private DuplexMessageQueueClient messageQueueClient;

    @Override
    public String getName() {
        return "echo";
    }

    public void setMessageQueueClient(DuplexMessageQueueClient messageQueueClient) {
        this.messageQueueClient = messageQueueClient;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                TextMessage incomingMessage = messageQueueClient.getBlocking();
                messageQueueClient.sendBlocking(incomingMessage);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        }
    }

}
