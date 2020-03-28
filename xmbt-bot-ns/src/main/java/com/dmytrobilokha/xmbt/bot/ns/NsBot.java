package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class NsBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(NsBot.class);
    private static final Pattern SPLIT_PATTERN = Pattern.compile(" +- +");

    @Nonnull
    private final MessageBus messageQueueClient;
    @Nonnull
    private final NsService nsService;

    NsBot(
            @Nonnull MessageBus messageQueueClient
            , @Nonnull NsService nsService
    ) {
        this.messageQueueClient = messageQueueClient;
        this.nsService = nsService;
    }

    @Override
    public void run() {
        try {
            nsService.initStationDictionary();
        } catch (NsServiceException ex) {
            LOG.error("Failed to initialize stations list, exiting", ex);
            return;
        }
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
            sendUserErrorResponse(requestMessage, "Invalid request format."
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
            tripPlans = nsService.planTrip(originStation, destinationStation);
        } catch (NsApiException ex) {
            LOG.error("Exception during issuing plan trip request to the NS API", ex);
            sendInternalErrorResponse(requestMessage, "Failed to get response from NS API");
            return;
        }
        responseWithTripPlans(requestMessage, tripPlans);
    }

    @CheckForNull
    private NsTrainStation findUniqueStation(@Nonnull RequestMessage incomingMessage, @Nonnull String stationQuery) {
        var stationsFound = nsService.findStation(stationQuery);
        if (stationsFound.isEmpty()) {
            sendUserErrorResponse(incomingMessage, "Cannot find any station matching '" + stationQuery + '\'');
            return null;
        }
        if (stationsFound.size() > 1) {
            sendUserErrorResponse(incomingMessage, "Found more than one station matching '" + stationQuery
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
        sendOkResponse(requestMessage, formattedPlans);
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

    private void sendOkResponse(@Nonnull RequestMessage request, @Nonnull String responseText) {
        messageQueueClient.send(new ResponseMessage(
                request, Response.OK, responseText));
    }

    private void sendUserErrorResponse(@Nonnull RequestMessage request, @Nonnull String responseText) {
        messageQueueClient.send(new ResponseMessage(
                request, Response.INVALID_COMMAND, responseText));
    }

    private void sendInternalErrorResponse(@Nonnull RequestMessage request, @Nonnull String responseText) {
        messageQueueClient.send(new ResponseMessage(
                request, Response.INTERNAL_ERROR, responseText));
    }

    private void syncStations(@Nonnull RequestMessage incomingRequest) throws InterruptedException {
        try {
            nsService.syncStations();
            sendOkResponse(incomingRequest, "The station list has been synchronized");
        } catch (NsApiException ex) {
            LOG.error("Sync stations NS API call failed", ex);
            sendInternalErrorResponse(incomingRequest, "Failed to synchronize stations because of API issue");
        } catch (NsServiceException ex) {
            LOG.error("Failed to update stations", ex);
            sendInternalErrorResponse(incomingRequest, "Failed to synchronize stations because of internal error");
        }
    }

}
