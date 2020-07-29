package com.dmytrobilokha.xmbt.bot.rain;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class BuienRadarApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(BuienRadarApiClient.class);

    @Nonnull
    private final HttpClient httpClient;

    BuienRadarApiClient(@Nonnull HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nonnull
    RainForecast getForecast(@Nonnull City city) throws RainApiException, InterruptedException {
        var apiResponse = fetchRainForecast(city);
        LOG.debug("Got response from the endpoint: '{}'", apiResponse);
        return parseApiResponse(apiResponse);
    }

    @Nonnull
    private String fetchRainForecast(@Nonnull City city) throws RainApiException, InterruptedException {
        var fullApiUrl = "https://gpsgadget.buienradar.nl/data/raintext?lat=" + city.getLat() + "&lon=" + city.getLon();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(fullApiUrl))
                .headers("Accept", "text/plain")
                .timeout(Duration.ofSeconds(120))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new RainApiException("Failed to issue get request to the BuienRadar API endpoint '"
                    + fullApiUrl + "'", ex);
        }
        var responseString = response.body();
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new RainApiException("Got bad response code '" + response.statusCode()
                    + "' from the BuienRadar API url: '" + fullApiUrl + "'. Response body: '" + responseString + "'");
        }
        if (responseString == null || responseString.isBlank()) {
            throw new RainApiException("Got no text in the response from the BuienRadar API url: '" + fullApiUrl);
        }
        return responseString;
    }

    @Nonnull
    private RainForecast parseApiResponse(@Nonnull String responseString) throws RainApiException {
        String startTime = null;
        String endTime = null;
        List<Integer> precipitationLevel = new ArrayList<>();
        for (Iterator<String> lineIterator = responseString.lines().iterator(); lineIterator.hasNext();) {
            var line = lineIterator.next();
            String timeString;
            try {
                var level = Integer.parseInt(line.substring(0, 3), 10);
                if (level > 255) {
                    throw new RainApiException("The precipitation level should always be less than 256, but for line '"
                            + line + "' in response '" + responseString + "' it is " + level);
                }
                precipitationLevel.add(level);
                timeString = line.substring(4);
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                throw new RainApiException(
                        "Unable to parse the line '" + line + "' from the response '" + responseString + "'");
            }
            if (startTime == null) {
                startTime = timeString;
            }
            endTime = timeString;
        }
        if (startTime == null || precipitationLevel.isEmpty()) {
            throw new RainApiException("Failed to extract rain data from the response '" + responseString + "'");
        }
        return new RainForecast(startTime, endTime, precipitationLevel);
    }

}
