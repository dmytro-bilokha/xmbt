package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.dictionary.FuzzyDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class NsBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NsBot.class);
    private static final Pattern SPLIT_PATTERN = Pattern.compile(" +- +");

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
                if (messageText.contains("sync_stations")) {
                    syncStations(incomingMessage);
                } else {
                    planTrip(incomingMessage);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        } catch (RuntimeException ex) {
            LOG.error("Got unexpected exception, shutting down", ex);
        }
    }

    private void planTrip(@Nonnull RequestMessage requestMessage) throws InterruptedException {
        var messageText = requestMessage.getTextMessage().getText().strip();
        String[] stations = SPLIT_PATTERN.split(messageText);
        if (stations.length != 2) {
            sendResponse(requestMessage, "Invalid request format."
                    + " Should be 'originStation - destinationStation'");
            return;
        }
        var originStation = findUniqueStation(requestMessage, stations[0]);
        var destinationStation = findUniqueStation(requestMessage, stations[1]);
        if (originStation == null || destinationStation == null) {
            return;
        }
        List<List<TripLeg>> tripPlans;
        try {
            tripPlans = apiClient.fetchTripPlans(originStation, destinationStation);
        } catch (NsApiException ex) {
            LOG.error("Exception during issuing plan trip request to the NS API", ex);
            sendResponse(requestMessage, "Failed to get response from NS API");
            return;
        }
        responseWithTripPlans(requestMessage, tripPlans);
    }

    @CheckForNull
    private NsTrainStation findUniqueStation(@Nonnull RequestMessage incomingMessage, @Nonnull String stationQuery) {
        var stationsFound = stationDictionary.get(stationQuery);
        if (stationsFound.isEmpty()) {
            sendResponse(incomingMessage, "Cannot find any station matching '" + stationQuery + '\'');
            return null;
        }
        if (stationsFound.size() > 1) {
            sendResponse(incomingMessage, "Found more than one station matching '" + stationQuery
                + "': " + stationsFound.stream().map(NsTrainStation::getName).collect(Collectors.joining(",")));
            return null;
        }
        return stationsFound.get(0);
    }

    private void responseWithTripPlans(
            @Nonnull RequestMessage requestMessage, @Nonnull List<List<TripLeg>> tripPlans) {
        var formattedPlans = tripPlans.stream()
                .map(this::formatTripPlan)
                .collect(Collectors.joining("\n---\n"));
        sendResponse(requestMessage, formattedPlans);
    }

    @Nonnull
    private String formatTripPlan(@Nonnull List<TripLeg> tripPlan) {
        var outputBuilder = new StringBuilder();
        var lastDestination = "";
        for (TripLeg leg : tripPlan) {
            var origin = leg.getOrigin();
            if (!lastDestination.equals(origin.getName())) {
                outputBuilder.append(origin.getName());
            }
            outputBuilder.append(' ')
                    .append(origin.getDateTime().toLocalTime())
                    .append(" (")
                    .append(origin.getTrack())
                    .append(") -> ");
            var destination = leg.getDestination();
            lastDestination = destination.getName();
            outputBuilder.append(destination.getDateTime().toLocalTime())
                    .append(" (")
                    .append(destination.getTrack())
                    .append(") ")
                    .append(lastDestination);
        }
        return outputBuilder.toString();
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
