package com.dmytrobilokha.xmbt.bot.rain;

import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import com.dmytrobilokha.xmbt.api.service.dictionary.FuzzyDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

class RainBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RainBot.class);

    @Nonnull
    private final MessageBus messageQueueClient;
    @Nonnull
    private final BuienRadarApiClient apiClient;
    @Nonnull
    private final FuzzyDictionary<City> citiesDictionary;

    RainBot(
            @Nonnull MessageBus messageQueueClient
            , @Nonnull BuienRadarApiClient apiClient
            , @Nonnull FuzzyDictionary<City> citiesDictionary
    ) {
        this.messageQueueClient = messageQueueClient;
        this.apiClient = apiClient;
        this.citiesDictionary = citiesDictionary;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestMessage incomingMessage = messageQueueClient.getBlocking();
                LOG.debug("Got from queue incoming {}", incomingMessage);
                try {
                    processRequest(incomingMessage);
                } catch (RuntimeException ex) {
                    LOG.error("Unexpected exception during processing {}", incomingMessage, ex);
                    sendInternalErrorResponse(incomingMessage, "Unexpected internal error");
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        }
    }

    private void processRequest(@Nonnull RequestMessage incomingMessage) throws InterruptedException {
        var city = findUniqueCity(incomingMessage);
        if (city == null) {
            return;
        }
        RainForecast forecast;
        try {
            forecast = apiClient.getForecast(city);
        } catch (RainApiException ex) {
            LOG.error("Failed to fetch the rain forecast for {}", city, ex);
            sendInternalErrorResponse(incomingMessage, "Failed to fetch the rain forecast");
            return;
        }
        sendOkResponse(incomingMessage, formatForecast(city, forecast));
    }

    @Nonnull
    private String formatForecast(@Nonnull City city, @Nonnull RainForecast forecast) {
        StringBuilder fb = new StringBuilder(city.getName())
                .append(":")
                .append(System.lineSeparator())
                .append(forecast.getStartTime())
                .append('|');
        String[] rainSymbols = new String[]{
            "_", "\u2581", "\u2582", "\u2583", "\u2584", "\u2585", "\u2586", "\u2587", "\u2588"};
        for (int level : forecast.getPrecipitationLevel()) {
            fb.append(rainSymbols[Math.min(rainSymbols.length - 1, Math.max(0, rainSymbols.length * level / 256))]);
        }
        return fb.append('|').append(forecast.getEndTime()).toString();
    }

    @CheckForNull
    private City findUniqueCity(@Nonnull RequestMessage incomingMessage) {
        var requestedCity = incomingMessage.getTextMessage().getText().strip();
        if (requestedCity.isBlank()) {
            sendUserErrorResponse(incomingMessage, "Give me the city name");
            return null;
        }
        List<City> cities = citiesDictionary.get(requestedCity);
        if (cities.isEmpty()) {
            sendUserErrorResponse(incomingMessage, "Cannot find any city matching '" + requestedCity + '\'');
            return null;
        }
        if (cities.size() > 1) {
            sendUserErrorResponse(incomingMessage, "Found more than one city matching '" + requestedCity
                    + "': " + cities.stream().map(City::getName).collect(Collectors.joining(",")));
            return null;
        }
        return cities.get(0);
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

}
