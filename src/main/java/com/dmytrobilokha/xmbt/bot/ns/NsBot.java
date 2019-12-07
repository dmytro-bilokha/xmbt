package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

class NsBot implements Runnable {

    private static final String STATIONS_ENDPOINT = "/reisinformatie-api/api/v2/stations";
    private static final Logger LOG = LoggerFactory.getLogger(NsBot.class);

    @Nonnull
    private final BotConnector messageQueueClient;
    @Nonnull
    private final PersistenceService persistenceService;
    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final HttpClient httpClient;

    NsBot(
            @Nonnull BotConnector messageQueueClient
            , @Nonnull PersistenceService persistenceService
            , @Nonnull ConfigService configService
    ) {
        this.messageQueueClient = messageQueueClient;
        this.persistenceService = persistenceService;
        this.configService = configService;
        this.httpClient = HttpClient.newHttpClient();
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

    //TODO: refactor, move all communication logic to the separate class, db logic as well
    private void syncStations() throws InterruptedException {
        String apiKey = configService.getProperty(NsApiKeyProperty.class).getStringValue();
        String stationsApiUrl = configService.getProperty(NsApiUrlProperty.class).getStringValue() + STATIONS_ENDPOINT;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stationsApiUrl))
                .header("Ocp-Apim-Subscription-Key", apiKey)
                .headers("Accept", "application/json")
                .timeout(Duration.ofSeconds(120))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            LOG.error("Failed to get list of stations from the NS API", ex);
            return;
        }
        LOG.debug(response.body());
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            LOG.error("Got bad response code '{}' from the NS Stations API", response.statusCode());
            return;
        }
    }

}
