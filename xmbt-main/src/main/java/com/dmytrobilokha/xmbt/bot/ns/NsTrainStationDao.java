package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.persistence.PersistenceService;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class NsTrainStationDao {

    @Nonnull
    private final PersistenceService persistenceService;

    NsTrainStationDao(@Nonnull PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Nonnull
    List<NsTrainStation> fetchAll() throws SQLException {
        return persistenceService.executeQuery(this::executeSelectAll);
    }

    @Nonnull
    private List<NsTrainStation> executeSelectAll(@Nonnull Connection connection) throws SQLException {
        try (
            var selectStatement = connection.prepareStatement(
                    "SELECT eva_code, code, name FROM ns_train_station");
            var resultSet = selectStatement.executeQuery();
        ) {
            return mapStations(resultSet);
        }
    }

    @Nonnull
    private List<NsTrainStation> mapStations(@Nonnull ResultSet resultSet) throws SQLException {
        var stations = new ArrayList<NsTrainStation>();
        while (resultSet.next()) {
            stations.add(new NsTrainStation(
                    resultSet.getLong("eva_code")
                    , resultSet.getString("code")
                    , resultSet.getString("name")
            ));
        }
        return stations;
    }

    void overwriteAll(@Nonnull Collection<NsTrainStation> stations) throws SQLException {
        persistenceService.executeTransaction(connection -> executeOverwrite(connection, stations));
    }

    private void executeOverwrite(
            @Nonnull Connection connection, @Nonnull Collection<NsTrainStation> stations) throws SQLException {
        try (var deleteStatement = connection.prepareStatement("DELETE FROM ns_train_station")) {
            deleteStatement.executeUpdate();
        }
        try (var insertStatement = connection.prepareStatement(
                "INSERT INTO ns_train_station (eva_code, code, name) VALUES (?, ?, ?)")) {
            for (NsTrainStation station : stations) {
                insertStatement.setLong(1, station.getEvaCode());
                insertStatement.setString(2, station.getCode());
                insertStatement.setString(3, station.getName());
                insertStatement.executeUpdate();
            }
        }
        connection.commit();
    }

}
