package com.dmytrobilokha.xmbt.bot.weather;

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

class WeatherBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherBot.class);

    @Nonnull
    private final MessageBus messageQueueClient;
    @Nonnull
    private final WeatherService weatherService;
    @Nonnull
    private final FuzzyDictionary<City> citiesDictionary;

    WeatherBot(
            @Nonnull MessageBus messageQueueClient
            , @Nonnull WeatherService weatherService
            , @Nonnull FuzzyDictionary<City> citiesDictionary
    ) {
        this.messageQueueClient = messageQueueClient;
        this.weatherService = weatherService;
        this.citiesDictionary = citiesDictionary;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestMessage incomingMessage = messageQueueClient.getBlocking();
                LOG.debug("Got from queue incoming {}", incomingMessage);
                processRequest(incomingMessage);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        }
    }

    private void processRequest(@Nonnull RequestMessage incomingMessage) {
        var city = findUniqueCity(incomingMessage);
        if (city == null) {
            return;
        }
        var weatherReport = weatherService.fetchWeatherReport(city);
        if (weatherReport == null) {
            sendInternalErrorResponse(incomingMessage, "Failed to fetch the weather report");
            return;
        }
        sendOkResponse(incomingMessage, weatherReport);
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
