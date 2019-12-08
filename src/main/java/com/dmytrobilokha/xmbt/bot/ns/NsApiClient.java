package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.bot.ns.dto.StationInfo;
import com.dmytrobilokha.xmbt.bot.ns.dto.StationsPayload;
import com.dmytrobilokha.xmbt.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NotThreadSafe
class NsApiClient {

    private static final String STATIONS_ENDPOINT = "/reisinformatie-api/api/v2/stations";
    private static final Logger LOG = LoggerFactory.getLogger(NsApiClient.class);

    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final HttpClient httpClient;
    @Nonnull
    private final Jsonb jsonb;

    NsApiClient(@Nonnull ConfigService configService, @Nonnull HttpClient httpClient) {
        this.configService = configService;
        this.httpClient = httpClient;
        this.jsonb = JsonbBuilder.create();
    }

    List<NsTrainStation> fetchAllNlStations() throws InterruptedException, NsApiException {
        var stationsPayload = getDataFromApi(STATIONS_ENDPOINT, StationsPayload.class);
        var stations = stationsPayload.getPayload()
                .stream()
                .filter(stationInfo -> "NL".equals(stationInfo.getCountryCode()))
                .map(this::mapFromDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (stations.isEmpty()) {
            throw new NsApiException("Got empty train stations list");
        }
        return stations;
    }

    @CheckForNull
    private NsTrainStation mapFromDto(@Nonnull StationInfo dto) {
        var evaCode = dto.getEvaCode();
        if (evaCode == null) {
            LOG.warn("Got null instead of EVA code for {}, the station will be skipped", dto);
            return null;
        }
        var code = dto.getCode();
        if (code == null) {
            LOG.warn("Got null instead of code for {}, the station will be skipped", dto);
            return null;
        }
        var names = dto.getNames();
        if (names == null) {
            LOG.warn("Got null instead of names object for {}, the station will be skipped", dto);
            return null;
        }
        String name = names.getFull();
        if (name == null) {
            LOG.warn("Got null instead of full name for {}, the station will be skipped", dto);
            return null;
        }
        return new NsTrainStation(evaCode, code, name);
    }

    private <T> T getDataFromApi(
            @Nonnull String relativeUrl, @Nonnull Class<T> responseClass) throws InterruptedException, NsApiException {
        String apiKey = configService.getProperty(NsApiKeyProperty.class).getStringValue();
        String fullApiUrl = configService.getProperty(NsApiUrlProperty.class).getStringValue() + relativeUrl;
        LOG.debug("Going to get data from the endpoint '{}'", fullApiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullApiUrl))
                .header("Ocp-Apim-Subscription-Key", apiKey)
                .headers("Accept", "application/json")
                .timeout(Duration.ofSeconds(120))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new NsApiException("Failed to issue get request to the NS API endpoint '"
                    + fullApiUrl + "'", ex);
        }
        var responseString = response.body();
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new NsApiException("Got bad response code '" + response.statusCode()
                    + "' from the NS API url: '" + fullApiUrl + "'. Response body: '" + responseString + "'");
        }
        LOG.debug("Got response from the endpoint: '{}'", responseString);
        try {
            T convertedResponse = jsonb.fromJson(responseString, responseClass);
            LOG.debug("Converted response to JSON: '{}'", convertedResponse);
            return convertedResponse;
        } catch (JsonbException ex) {
            throw new NsApiException("Failed to convert response to JSON object", ex);
        }
    }

}
