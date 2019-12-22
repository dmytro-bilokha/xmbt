package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.bot.ns.dto.StationInfoDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.StationsPayloadDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.TripInfoDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.TripLegDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.TripStationDto;
import com.dmytrobilokha.xmbt.bot.ns.dto.TripsPayloadDto;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NotThreadSafe
class NsApiClient {

    static final ZoneId DEFAULT_TIMEZONE_ID = ZoneId.of("Europe/Amsterdam");

    private static final String STATIONS_ENDPOINT = "/reisinformatie-api/api/v2/stations";
    private static final String TRIPS_ENDPOINT = "/reisinformatie-api/api/v3/trips";
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

    @Nonnull
    List<NsTrainStation> fetchAllNlStations() throws InterruptedException, NsApiException {
        var stationsPayload = getDataFromApi(STATIONS_ENDPOINT, StationsPayloadDto.class);
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
    ) throws InterruptedException, NsApiException {
        var queryUrl = TRIPS_ENDPOINT
                + "?originEVACode=" + origin.getEvaCode()
                + "&destinationEVACode=" + destination.getEvaCode();
        var tripsPayload = getDataFromApi(queryUrl, TripsPayloadDto.class);
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
            var origin = mapTripStation(dto.getOrigin());
            var destination = mapTripStation(dto.getDestination());
            if (origin == null || destination == null) {
                LOG.error("Got invalid trip leg: {}", dto);
                return null;
            }
            tripLegs.add(new TripLeg(origin, destination));
        }
        return tripLegs;
    }

    @CheckForNull
    private TripStation mapTripStation(@CheckForNull TripStationDto dto) {
        if (dto == null) {
            return null;
        }
        var zonedDateTime = dto.getActualDateTime() == null ? dto.getPlannedDateTime() : dto.getActualDateTime();
        if (zonedDateTime == null) {
            return null;
        }
        var name = dto.getName() == null ? "?" : dto.getName();
        var track = dto.getPlannedTrack() == null ? "?" : dto.getPlannedTrack();
        // If we are interested in the NS API, probably, we are in the Amsterdam timezone
        var localDateTime = zonedDateTime.withZoneSameInstant(DEFAULT_TIMEZONE_ID).toLocalDateTime();
        return new TripStation(name, track, localDateTime);
    }

    @CheckForNull
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
