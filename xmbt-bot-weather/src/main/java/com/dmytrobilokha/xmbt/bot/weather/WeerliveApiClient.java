package com.dmytrobilokha.xmbt.bot.weather;

import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.bot.weather.config.WeerliveApiKeyProperty;
import com.dmytrobilokha.xmbt.bot.weather.config.WeerliveApiUrlProperty;
import com.dmytrobilokha.xmbt.bot.weather.dto.LiveWeather;
import com.dmytrobilokha.xmbt.bot.weather.dto.WeerliveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class WeerliveApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(WeerliveApiClient.class);

    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final HttpClient httpClient;
    @Nonnull
    private final Jsonb jsonb;

    WeerliveApiClient(@Nonnull ConfigService configService, @Nonnull HttpClient httpClient) {
        this.configService = configService;
        this.httpClient = httpClient;
        this.jsonb = JsonbBuilder.create();
    }

    @Nonnull
    LiveWeather fetchWeatherData(@Nonnull City city) throws WeatherApiException, InterruptedException {
        var apiResponse = queryWeerliveApi(city);
        return parseApiResponse(apiResponse);
    }

    @Nonnull
    private String queryWeerliveApi(@Nonnull City city) throws WeatherApiException, InterruptedException {
        String apiKey = configService.getProperty(WeerliveApiKeyProperty.class).getStringValue();
        String fullApiUrl = configService.getProperty(WeerliveApiUrlProperty.class).getStringValue()
                + "?key=" + apiKey + "&locatie=" + city.getLat() + "," + city.getLon();
        LOG.debug("Going to get data from the endpoint '{}'", fullApiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullApiUrl))
                .headers("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .get(5L, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException ex) {
            throw new WeatherApiException("Failed to get data from the Weerlive API endpoint '"
                    + fullApiUrl + "'", ex);
        }
        var responseString = response.body();
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new WeatherApiException("Got bad response code '" + response.statusCode()
                    + "' from the Weerlive API url: '" + fullApiUrl + "'. Response body: '" + responseString + "'");
        }
        LOG.debug("Got response from the endpoint: '{}'", responseString);
        return responseString;
    }

    @Nonnull
    private LiveWeather parseApiResponse(@Nonnull String responseString) throws WeatherApiException {
        WeerliveResponse convertedResponse;
        try {
            convertedResponse = jsonb.fromJson(responseString, WeerliveResponse.class);
            LOG.debug("Converted response to JSON: '{}'", convertedResponse);
        } catch (JsonbException ex) {
            throw new WeatherApiException("Failed to convert response to JSON object", ex);
        }
        var liveWeatherList = convertedResponse.getLiveWeather();
        if (liveWeatherList == null || liveWeatherList.isEmpty()) {
            throw new WeatherApiException("Got non valid response from the Weerlive API: " + convertedResponse);
        }
        return liveWeatherList.get(0);
    }

}
