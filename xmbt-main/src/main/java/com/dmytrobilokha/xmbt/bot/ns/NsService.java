package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.dictionary.FuzzyDictionary;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.dmytrobilokha.xmbt.bot.ns.NsApiClient.DEFAULT_TIMEZONE_ID;

@NotThreadSafe
class NsService {

    @Nonnull
    private final NsTrainStationDao dao;
    @Nonnull
    private final NsApiClient apiClient;
    @Nonnull
    private final FuzzyDictionary<NsTrainStation> stationDictionary;

    NsService(@Nonnull NsTrainStationDao dao, @Nonnull NsApiClient apiClient) {
        this.dao = dao;
        this.apiClient = apiClient;
        this.stationDictionary = FuzzyDictionary.withLatinLetters();
    }

    void initStationDictionary() throws NsServiceException {
        try {
            var stations = dao.fetchAll();
            refreshStationDictionary(stations);
        } catch (SQLException ex) {
            throw new NsServiceException("Failed to read the stations list. Dictionary won't be initialized", ex);
        }
    }

    void syncStations() throws NsServiceException, InterruptedException {
        try {
            var stations = apiClient.fetchAllNlStations();
            dao.overwriteAll(stations);
            refreshStationDictionary(stations);
        } catch (SQLException ex) {
            throw new NsServiceException("Failed to update stations in the DB", ex);
        }
    }

    private void refreshStationDictionary(@Nonnull Collection<NsTrainStation> stations) {
        stationDictionary.clear();
        stations.forEach(station -> stationDictionary.put(station.getName(), station));
    }

    List<NsTrainStation> findStation(@Nonnull String stationQuery) {
        return stationDictionary.get(stationQuery);
    }

    List<List<TripLeg>> planTrip(
            @Nonnull NsTrainStation originStation
            , @Nonnull NsTrainStation destinationStation
    ) throws InterruptedException, NsApiException {
        var now = ZonedDateTime.now().withZoneSameInstant(DEFAULT_TIMEZONE_ID).toLocalDateTime();
        return apiClient.fetchTripPlans(originStation, destinationStation)
                .stream()
                .filter(legsList -> !legsList.isEmpty())
                .filter(legsList -> now.isBefore(legsList.get(0).getOrigin().getDateTime()))
                .sorted(this::compareTripPlans)
                .collect(Collectors.toList());
    }

    private int compareTripPlans(@Nonnull List<TripLeg> planA, @Nonnull List<TripLeg> planB) {
        //First priority in sorting: earlier arrivals first
        int result = planA.get(planA.size() - 1).getDestination().getDateTime()
                .compareTo(planB.get(planB.size() - 1).getDestination().getDateTime());
        if (result != 0) {
            return result;
        }
        //Second priority in sorting: less changes first
        result = planA.size() - planB.size();
        if (result != 0) {
            return result;
        }
        //Third priority in sorting: later departure first
        return planB.get(0).getOrigin().getDateTime()
                .compareTo(planA.get(0).getOrigin().getDateTime());
    }

}
