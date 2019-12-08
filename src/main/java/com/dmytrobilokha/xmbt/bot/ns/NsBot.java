package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

class NsBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NsBot.class);

    @Nonnull
    private final BotConnector messageQueueClient;
    @Nonnull
    private final PersistenceService persistenceService;
    @Nonnull
    private final NsApiClient apiClient;

    NsBot(
            @Nonnull BotConnector messageQueueClient
            , @Nonnull PersistenceService persistenceService
            , @Nonnull NsApiClient apiClient
    ) {
        this.messageQueueClient = messageQueueClient;
        this.persistenceService = persistenceService;
        this.apiClient = apiClient;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestMessage incomingMessage = messageQueueClient.getBlocking();
                LOG.debug("Got from queue incoming {}", incomingMessage);
                if (incomingMessage.getTextMessage().getText().contains("sync")) {
                    syncStations();
                    messageQueueClient.send(new ResponseMessage(
                            incomingMessage, Response.OK, "The station list has been synchronized"));
                } else {
                    messageQueueClient.send(new ResponseMessage(
                            incomingMessage, Response.OK, "Unrecognizable command message"));
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        } catch (RuntimeException ex) {
            LOG.error("Got unexpected exception, shutting down", ex);
        }
    }

    private void syncStations() throws InterruptedException {
        try {
            apiClient.fetchAllNlStations();
        } catch (NsApiException ex) {
            LOG.error("Failed to sync stations", ex);
        }
    }

}
