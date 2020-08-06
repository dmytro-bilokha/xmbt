package com.dmytrobilokha.xmbt.bot.weather;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

abstract class AbstractWeatherApiClient<T> {

    @Nonnull
    protected final Logger log;
    @Nonnull
    protected final String apiProviderName;
    @Nonnull
    private final Supplier<CloseableHttpClient> httpClientSupplier;

    AbstractWeatherApiClient(
            @Nonnull String apiProviderName, @Nonnull Supplier<CloseableHttpClient> httpClientSupplier) {
        this.log = LoggerFactory.getLogger(this.getClass());
        this.apiProviderName = apiProviderName;
        this.httpClientSupplier = httpClientSupplier;
    }

    @Nonnull
    T fetch(@Nonnull City city) throws WeatherApiException {
        var apiResponse = queryApi(city);
        log.debug("Got response from the endpoint: '{}'", apiResponse);
        T parsedResponse = parseApiResponse(apiResponse);
        log.debug("Parsed API response: {}", parsedResponse);
        return parsedResponse;
    }

    @Nonnull
    protected String queryApi(@Nonnull City city) throws WeatherApiException {
        var fullApiUrl = getFullUrl(city);
        log.debug("Going to send a request to '{}'", fullApiUrl);
        try (var httpClient = httpClientSupplier.get()) {
            var httpGet = new HttpGet(fullApiUrl);
            return fetchApiResponse(httpClient, httpGet);
        } catch (IOException ex) {
            throw new WeatherApiException("Failed to close http client after querying the " + apiProviderName 
                    + " API endpoint '" + fullApiUrl + "'", ex);
        }
    }
    
    @Nonnull
    protected abstract String getFullUrl(@Nonnull City city);

    @Nonnull
    private String fetchApiResponse(
            @Nonnull CloseableHttpClient httpClient, @Nonnull HttpGet httpGet) throws WeatherApiException {
        try (var response = httpClient.execute(httpGet)) {
            if (response.getCode() != HttpURLConnection.HTTP_OK) {
                throw new WeatherApiException("Got bad response code '" + response.getCode()
                        + "' from the " + apiProviderName + " API url: '" + httpGet.getRequestUri()
                        + "'. Response reason: '" + response.getReasonPhrase() + "'");
            }
            var responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8, 500);
            if (responseString == null || responseString.isBlank()) {
                throw new WeatherApiException("Got no text in the response from the " 
                        + apiProviderName + " API url: '" + httpGet.getRequestUri());
            }
            return responseString;
        } catch (ParseException | IOException ex) {
            throw new WeatherApiException("Unable to fetch the response from the " 
                    + apiProviderName + " API url: '" + httpGet.getRequestUri(), ex);
        }
    }

    @Nonnull
    protected abstract T parseApiResponse(@Nonnull String responseString) throws WeatherApiException;
    
}
