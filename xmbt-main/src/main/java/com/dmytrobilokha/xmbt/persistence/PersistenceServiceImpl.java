package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.api.service.ThrowingConsumer;
import com.dmytrobilokha.xmbt.api.service.ThrowingFunction;
import com.dmytrobilokha.xmbt.api.service.ThrowingIntFunction;
import com.dmytrobilokha.xmbt.api.service.persistence.PersistenceService;
import com.dmytrobilokha.xmbt.boot.Initializable;
import com.dmytrobilokha.xmbt.config.ConfigServiceImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;

public class PersistenceServiceImpl implements Initializable, PersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceServiceImpl.class);

    @Nonnull
    private final ConfigServiceImpl configService;
    @CheckForNull
    private HikariDataSource dataSource;

    public PersistenceServiceImpl(@Nonnull ConfigServiceImpl configService) {
        this.configService = configService;
    }

    @Override
    public void init() {
        LOG.info("Initializing DB connection pool");
        var dbPoolConfig = new HikariConfig();
        dbPoolConfig.setJdbcUrl(configService.getProperty(DbUrlProperty.class).getStringValue());
        dbPoolConfig.setUsername(configService.getProperty(DbUsernameProperty.class).getStringValue());
        dbPoolConfig.setPassword(configService.getProperty(DbPasswordProperty.class).getStringValue());
        dbPoolConfig.setMaximumPoolSize(configService.getProperty(DbPoolSizeProperty.class).getValue());
        dbPoolConfig.addDataSourceProperty("cachePrepStmts", "true");
        dbPoolConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        dbPoolConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource  = new HikariDataSource(dbPoolConfig);
        LOG.info("Initializing flyway DB migration");
        var flywayConfig = new FluentConfiguration()
                .dataSource(dataSource)
                .baselineVersion("0.0.0")
                .baselineOnMigrate(true)
                .locations("classpath:db-script");
        Flyway flyway = new Flyway(flywayConfig);
        flyway.migrate();
    }

    @Override
    public <R> R executeQuery(
            @Nonnull ThrowingFunction<Connection, R, SQLException> query) throws SQLException {
        try (var connection = getDbConnection()) {
            connection.setReadOnly(true);
            return query.apply(connection);
        }
    }

    @Override
    public int executeUpdateAutoCommitted(
            @Nonnull ThrowingIntFunction<Connection, SQLException> operator) throws SQLException {
        try (var connection = getDbConnection()) {
            return operator.apply(connection);
        }
    }

    @Override
    public void executeTransaction(
            @Nonnull ThrowingConsumer<Connection, SQLException> transactionConsumer) throws SQLException {
        Connection connection = null;
        try {
            connection = getDbConnection();
            connection.setAutoCommit(false);
            transactionConsumer.accept(connection);
        } catch (SQLException | RuntimeException ex) {
            rollback(connection);
            throw ex;
        } finally {
            close(connection);
        }
    }

    private void rollback(@CheckForNull Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException ex) {
            LOG.error("Error during trying to rollback a transaction", ex);
        }
    }

    private void close(@CheckForNull Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            LOG.error("Error during trying to close a connection", ex);
        }
    }

    @Nonnull
    private Connection getDbConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("Unable to get a DB connection, the datasource hasn't been initialized");
        }
        return dataSource.getConnection();
    }

}
