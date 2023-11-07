package com.dmytrobilokha.xmbt.bot.webgateway;

import com.dmytrobilokha.xmbt.api.service.persistence.PersistenceService;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WebGatewayDao {

    @Nonnull
    private final PersistenceService persistenceService;

    public WebGatewayDao(@Nonnull PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @CheckForNull
    public WebGateway findWebGateway(@Nonnull String userAddress) throws SQLException {
        return persistenceService.executeQuery(con -> executeFindWebGateway(con, userAddress));
    }

    @CheckForNull
    private WebGateway executeFindWebGateway(
            @Nonnull Connection connection, @Nonnull String userAddress) throws SQLException {
        try (
                var selectStatement = connection.prepareStatement(
                        "SELECT user_address, path_id FROM web_gateway WHERE user_address = ? ORDER BY path_id")
        ) {
            selectStatement.setString(1, userAddress);
            try (var resultSet = selectStatement.executeQuery()) {
                var gateways = mapWebGateways(resultSet);
                if (gateways.isEmpty()) {
                    return null;
                }
                return gateways.get(0);
            }
        }
    }

    @Nonnull
    private List<WebGateway> mapWebGateways(@Nonnull ResultSet resultSet) throws SQLException {
        var webGateways = new ArrayList<WebGateway>();
        while (resultSet.next()) {
            webGateways.add(new WebGateway(
                    resultSet.getString("user_address"),
                    resultSet.getLong("path_id")
            ));
        }
        return webGateways;
    }

    public int insertWebGateway(@Nonnull WebGateway webGateway) throws SQLException {
        return persistenceService.executeUpdateAutoCommitted(con -> executeInsertWebGateway(con, webGateway));
    }

    private int executeInsertWebGateway(
            @Nonnull Connection connection, @Nonnull WebGateway webGateway) throws SQLException {
        try (var insertStatement = connection.prepareStatement(
                "INSERT INTO web_gateway (user_address, path_id) VALUES (?, ?)"
        )) {
            insertStatement.setString(1, webGateway.userAddress());
            insertStatement.setLong(2, webGateway.pathId());
            return insertStatement.executeUpdate();
        }
    }

    public int deleteWebGateway(@Nonnull WebGateway webGateway) throws SQLException {
        return persistenceService.executeUpdateAutoCommitted(con -> executeDeleteWebGateway(con, webGateway));
    }

    private int executeDeleteWebGateway(
            @Nonnull Connection connection, @Nonnull WebGateway webGateway) throws SQLException {
        try (var deleteStatement = connection.prepareStatement(
                "DELETE FROM web_gateway WHERE user_address = ? AND path_id = ?"
        )) {
            deleteStatement.setString(1, webGateway.userAddress());
            deleteStatement.setLong(2, webGateway.pathId());
            return deleteStatement.executeUpdate();
        }
    }

}
