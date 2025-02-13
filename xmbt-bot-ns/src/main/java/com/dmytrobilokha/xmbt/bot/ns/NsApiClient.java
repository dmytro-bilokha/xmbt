package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.bot.ns.config.NsApiKeyProperty;
import com.dmytrobilokha.xmbt.bot.ns.config.NsApiUrlProperty;
import com.dmytrobilokha.xmbt.bot.ns.dto.StationInfoDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.StationsPayloadDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.TripInfoDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.TripLegDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.TripStationDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.TripsPayloadDto;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@NotThreadSafe
class NsApiClient {

    static final ZoneId DEFAULT_TIMEZONE_ID = ZoneId.of("Europe/Amsterdam");

    private static final String STATIONS_ENDPOINT = "/reisinformatie-api/api/v2/stations";
    private static final String TRIPS_ENDPOINT = "/reisinformatie-api/api/v3/trips";
    private static final int MAX_API_RESPONSE_LENGTH = 500_000;
    private static final Logger LOG = LoggerFactory.getLogger(NsApiClient.class);

    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final Supplier<CloseableHttpClient> httpClientSupplier;
    @Nonnull
    private final Jsonb jsonb;

    NsApiClient(@Nonnull ConfigService configService, @Nonnull Supplier<CloseableHttpClient> httpClientSupplier) {
        this.configService = configService;
        this.httpClientSupplier = httpClientSupplier;
        this.jsonb = JsonbBuilder.create();
    }

    @Nonnull
    List<NsTrainStation> fetchAllNlStations() throws InterruptedException, NsApiException {
        var stationsPayload = getDataFromApi(
                STATIONS_ENDPOINT, StationsPayloadDto.class, Collections.emptyMap());
        if (stationsPayload == null || stationsPayload.getPayload() == null) {
            throw new NsApiException("Got invalid response from stations info NS API");
        }
        var stations = stationsPayload.getPayload()
                .stream()
                .filter(Objects::nonNull)
                .filter(stationInfo -> "NL".equals(stationInfo.getCountryCode()))
                .map(this::mapNsTrainStation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (stations.isEmpty()) {
            throw new NsApiException("Got empty train stations list");
        }
        return stations;
    }

    @CheckForNull
    private NsTrainStation mapNsTrainStation(@Nonnull StationInfoDto dto) {
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

    @Nonnull
    List<List<TripLeg>> fetchTripPlans(
            @Nonnull NsTrainStation origin
            , @Nonnull NsTrainStation destination
    ) throws NsApiException {
        var queryParams = Map.of(
                "fromStation", origin.getName()
                , "toStation", destination.getName()
        );
        var tripsPayload = getDataFromApi(TRIPS_ENDPOINT, TripsPayloadDto.class, queryParams);
        return mapTrips(tripsPayload);
    }

    @Nonnull
    private List<List<TripLeg>> mapTrips(@CheckForNull TripsPayloadDto tripsPayload) throws NsApiException {
        if (tripsPayload == null || tripsPayload.getTrips() == null || tripsPayload.getTrips().isEmpty()) {
            throw new NsApiException("Got invalid response from route planning NS API with no trips: " + tripsPayload);
        }
        return tripsPayload.getTrips()
                .stream()
                .filter(Objects::nonNull)
                .map(this::mapTripInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @CheckForNull
    private List<TripLeg> mapTripInfo(@Nonnull TripInfoDto tripInfo) {
        var legDtos = tripInfo.getLegs();
        if (legDtos == null || legDtos.isEmpty()) {
            LOG.error("Got invalid trip info response with no legs: {}", tripInfo);
            return null;
        }
        var tripLegs = new ArrayList<TripLeg>();
        for (TripLegDto dto : legDtos) {
            boolean canceled = Boolean.TRUE.equals(dto.getCanceled()) || Boolean.TRUE.equals(dto.getPartCanceled());
            var origin = mapTripStation(dto.getOrigin(), canceled);
            var destination = mapTripStation(dto.getDestination(), false);
            if (origin == null || destination == null) {
                LOG.error("Got invalid trip leg: {}", dto);
                return null;
            }
            tripLegs.add(new TripLeg(origin, destination));
        }
        return tripLegs;
    }

    @CheckForNull
    private TripStation mapTripStation(@CheckForNull TripStationDto dto, boolean indicateDoubt) {
        if (dto == null) {
            return null;
        }
        var zonedDateTime = dto.getActualDateTime() == null ? dto.getPlannedDateTime() : dto.getActualDateTime();
        if (zonedDateTime == null) {
            return null;
        }
        var name = dto.getName() == null ? "?" : dto.getName();
        var track = Objects.requireNonNullElse(dto.getActualTrack(),
                Objects.requireNonNullElse(dto.getPlannedTrack(), "?"));
        if (indicateDoubt) {
            track = "?" + track + "?";
        }
        // If we are interested in the NS API, probably, we are in the Amsterdam timezone
        var localDateTime = zonedDateTime.withZoneSameInstant(DEFAULT_TIMEZONE_ID).toLocalDateTime();
        return new TripStation(name, track, localDateTime);
    }

    @CheckForNull
    private <T> T getDataFromApi(
            @Nonnull String relativeEndpointUrl
            , @Nonnull Class<T> responseClass
            , @Nonnull Map<String, String> queryParams
    ) throws NsApiException {
        String fullEndpointUrl = configService.getProperty(NsApiUrlProperty.class).getStringValue()
                + relativeEndpointUrl;
        LOG.debug("Going to get data from the endpoint '{}'", fullEndpointUrl);
        URI apiUri;
        try {
            var uriBuilder = new URIBuilder(fullEndpointUrl);
            for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
                uriBuilder.addParameter(queryParam.getKey(), queryParam.getValue());
            }
            apiUri = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new NsApiException("Failed to build API URI from fullEndpointUrl=" + fullEndpointUrl
                + ", queryParams=" + queryParams, e);
        }
        var httpGet = new HttpGet(apiUri);
        String apiKey = configService.getProperty(NsApiKeyProperty.class).getStringValue();
        httpGet.addHeader("Ocp-Apim-Subscription-Key", apiKey);
        httpGet.addHeader("Accept", "application/json");
        String responseString;
        try (var httpClient = httpClientSupplier.get()) {
            responseString = fetchApiResponse(httpClient, httpGet);
        } catch (IOException ex) {
            throw new NsApiException("Failed to close http client after querying the API endpoint '"
                    + fullEndpointUrl + "'", ex);
        }
        LOG.debug("Got response from the endpoint: '{}'", responseString);
        try {
            T convertedResponse = jsonb.fromJson(responseString, responseClass);
            LOG.debug("Converted response to JSON: '{}'", convertedResponse);
            return convertedResponse;
        } catch (JsonbException ex) {
            throw new NsApiException("Failed to convert response to JSON object. Response: "
                    + responseString, ex);
        }
    }

    @Nonnull
    private String fetchApiResponse(
            @Nonnull CloseableHttpClient httpClient, @Nonnull HttpGet httpGet) throws NsApiException {
        try (var response = httpClient.execute(httpGet)) {
            if (response.getCode() != HttpURLConnection.HTTP_OK) {
                throw new NsApiException("Got bad response code '" + response.getCode()
                        + "' from the API url: '" + httpGet.getRequestUri()
                        + "'. Response reason: '" + response.getReasonPhrase() + "'");
            }
            var responseString = EntityUtils.toString(
                    response.getEntity(), StandardCharsets.UTF_8, MAX_API_RESPONSE_LENGTH);
            validateResponseString(responseString, httpGet);
            return responseString;
        } catch (ParseException | IOException ex) {
            throw new NsApiException("Unable to fetch the response from the API url: " + httpGet.getRequestUri(), ex);
        }
    }

    private void validateResponseString(
            @CheckForNull String responseString, @Nonnull HttpGet httpGet) throws NsApiException {
        if (responseString == null || responseString.isBlank()) {
            throw new NsApiException("Got no text in the response from the API url: " + httpGet.getRequestUri());
        }
        if (responseString.length() == MAX_API_RESPONSE_LENGTH) {
            throw new NsApiException("API response exceeded maximum allowed length " + MAX_API_RESPONSE_LENGTH
                    + ". Response: " + responseString);
        }
    }

}
