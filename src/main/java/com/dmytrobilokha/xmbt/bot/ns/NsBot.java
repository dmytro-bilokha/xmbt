package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.dictionary.FuzzyDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Collection;

class NsBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NsBot.class);

    @Nonnull
    private final BotConnector messageQueueClient;
    @Nonnull
    private final NsTrainStationDao dao;
    @Nonnull
    private final NsApiClient apiClient;
    @Nonnull
    private final FuzzyDictionary<NsTrainStation> stationDictionary;

    NsBot(
            @Nonnull BotConnector messageQueueClient
            , @Nonnull NsTrainStationDao dao
            , @Nonnull NsApiClient apiClient
    ) {
        this.messageQueueClient = messageQueueClient;
        this.dao = dao;
        this.apiClient = apiClient;
        this.stationDictionary = FuzzyDictionary.withLatinLetters();
    }

    @Override
    public void run() {
        initStationDictionary();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestMessage incomingMessage = messageQueueClient.getBlocking();
                LOG.debug("Got from queue incoming {}", incomingMessage);
                var messageText = incomingMessage.getTextMessage().getText().strip();
                if (messageText.contains("sync")) {
                    syncStations(incomingMessage);
                } else {
                    var matchingStations = stationDictionary.get(messageText);
                    sendResponse(incomingMessage, "Stations matching your request '" + messageText + "': "
                            + matchingStations);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        } catch (RuntimeException ex) {
            LOG.error("Got unexpected exception, shutting down", ex);
        }
    }

    private void initStationDictionary() {
        try {
            var stations = dao.fetchAll();
            refreshStationDictionary(stations);
        } catch (SQLException ex) {
            LOG.error("Failed to read the stations list. Dictionary won't be initialized", ex);
        }
    }

    private void refreshStationDictionary(@Nonnull Collection<NsTrainStation> stations) {
        stationDictionary.clear();
        stations.forEach(station -> stationDictionary.put(station.getName(), station));
    }

    private void sendResponse(@Nonnull RequestMessage request, @Nonnull String responseText) {
        messageQueueClient.send(new ResponseMessage(
                request, Response.OK, responseText));
    }

    private void syncStations(@Nonnull RequestMessage incomingRequest) throws InterruptedException {
        try {
            var stations = apiClient.fetchAllNlStations();
            dao.overwriteAll(stations);
            refreshStationDictionary(stations);
            sendResponse(incomingRequest, "The station list has been synchronized");
        } catch (NsApiException ex) {
            LOG.error("Failed to fetch stations to sync", ex);
            sendResponse(incomingRequest, "Failed to synchronize stations because of API issue");
        } catch (SQLException ex) {
            LOG.error("Failed to update stations in the DB", ex);
            sendResponse(incomingRequest, "Failed to synchronize stations because of internal DB issue");
        }
    }

}
