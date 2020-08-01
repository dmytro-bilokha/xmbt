package com.dmytrobilokha.xmbt.bot.rain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class BuienRadarApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(BuienRadarApiClient.class);
    private static final int MAX_RAIN_LEVEL = 255;

    @Nonnull
    private final HttpClient httpClient;

    BuienRadarApiClient(@Nonnull HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nonnull
    RainForecast getForecast(@Nonnull City city) throws RainApiException, InterruptedException {
        var apiResponse = fetchRainForecast(city);
        LOG.debug("Got response from the endpoint: '{}'", apiResponse);
        var parsedResponse = parseApiResponse(apiResponse);
        LOG.debug("Parsed API response: {}", parsedResponse);
        return parsedResponse;
    }

    @Nonnull
    private String fetchRainForecast(@Nonnull City city) throws RainApiException, InterruptedException {
        var fullApiUrl = "https://gadgets.buienradar.nl/data/raintext?lat=" + city.getLat() + "&lon=" + city.getLon();
        LOG.debug("Going to send a request to '{}'", fullApiUrl);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(fullApiUrl))
                .headers("Accept", "text/plain")
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response;
        try {
            var asyncResponse = httpClient.sendAsync(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            response = asyncResponse.get(10L, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException ex) {
            throw new RainApiException("Failed to get response from the BuienRadar API endpoint '"
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
                precipitationLevel.add(level);
                timeString = line.substring(4);
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                throw new RainApiException(
                        "Unable to parse the line '" + line + "' from the response '" + responseString + "'", ex);
            }
            if (startTime == null) {
                startTime = timeString;
            }
            endTime = timeString;
        }
        return validateBuildRainForecast(startTime, endTime, precipitationLevel, responseString);
    }

    @Nonnull
    private RainForecast validateBuildRainForecast(
            @CheckForNull String startTime
            , @CheckForNull String endTime
            , @Nonnull List<Integer> precipitationLevel
            , @Nonnull String responseString) throws RainApiException {
        if (startTime == null || endTime == null || precipitationLevel.isEmpty()) {
            throw new RainApiException("Failed to extract rain data from the response '" + responseString + "'");
        }
        for (int level : precipitationLevel) {
            if (level > MAX_RAIN_LEVEL) {
                throw new RainApiException("The precipitation level should always be less than 256, but got "
                        + level + " in response '" + responseString);
            }
        }
        return new RainForecast(startTime, endTime, precipitationLevel);
    }

}
