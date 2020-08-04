package com.dmytrobilokha.xmbt.bot.weather;

import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.bot.weather.config.BuienradarApiUrlProperty;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

class BuienradarApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(BuienradarApiClient.class);
    private static final int MAX_RAIN_LEVEL = 255;

    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final Supplier<CloseableHttpClient> httpClientSupplier;

    BuienradarApiClient(
            @Nonnull ConfigService configService, @Nonnull Supplier<CloseableHttpClient> httpClientSupplier) {
        this.configService = configService;
        this.httpClientSupplier = httpClientSupplier;
    }

    @Nonnull
    RainForecast fetchRainForecast(@Nonnull City city) throws WeatherApiException, InterruptedException {
        var apiResponse = queryRainForecastApi(city);
        LOG.debug("Got response from the endpoint: '{}'", apiResponse);
        var parsedResponse = parseApiResponse(apiResponse);
        LOG.debug("Parsed API response: {}", parsedResponse);
        return parsedResponse;
    }

    @Nonnull
    private String queryRainForecastApi(@Nonnull City city) throws WeatherApiException {
        var fullApiUrl = configService.getProperty(BuienradarApiUrlProperty.class).getStringValue()
                + "?lat=" + city.getLat() + "&lon=" + city.getLon();
        LOG.debug("Going to send a request to '{}'", fullApiUrl);
        try (var httpClient = httpClientSupplier.get()) {
            var httpGet = new HttpGet(fullApiUrl);
            //Trying to mimic curl to avoid hanging connections
            httpGet.addHeader("Accept", "*/*");
            httpGet.addHeader("User-Agent", "curl/7.71.1");
            return fetchApiResponse(httpClient, httpGet);
        } catch (IOException ex) {
            throw new WeatherApiException("Failed to close http client after querying the Buienradar API endpoint '"
                    + fullApiUrl + "'", ex);
        }
    }

    @Nonnull
    private String fetchApiResponse(
            @Nonnull CloseableHttpClient httpClient, @Nonnull HttpGet httpGet) throws WeatherApiException {
        try (var response = httpClient.execute(httpGet)) {
            if (response.getCode() != HttpURLConnection.HTTP_OK) {
                throw new WeatherApiException("Got bad response code '" + response.getCode()
                        + "' from the Buienradar API url: '" + httpGet.getRequestUri()
                        + "'. Response reason: '" + response.getReasonPhrase() + "'");
            }
            var responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8, 500);
            if (responseString == null || responseString.isBlank()) {
                throw new WeatherApiException(
                        "Got no text in the response from the Buienradar API url: '" + httpGet.getRequestUri());
            }
            return responseString;
        } catch (ParseException | IOException ex) {
            throw new WeatherApiException(
                    "Unable to fetch the response from the Buienradar API url: '" + httpGet.getRequestUri(), ex);
        }
    }

    @Nonnull
    private RainForecast parseApiResponse(@Nonnull String responseString) throws WeatherApiException {
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
                throw new WeatherApiException(
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
            , @Nonnull String responseString) throws WeatherApiException {
        if (startTime == null || endTime == null || precipitationLevel.isEmpty()) {
            throw new WeatherApiException("Failed to extract rain data from the response '" + responseString + "'");
        }
        for (int level : precipitationLevel) {
            if (level > MAX_RAIN_LEVEL) {
                throw new WeatherApiException("The precipitation level should always be less than 256, but got "
                        + level + " in response '" + responseString);
            }
        }
        return new RainForecast(startTime, endTime, precipitationLevel);
    }

}
