package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.api.Persistable;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.fs.FsService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class PersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceService.class);

    @Nonnull
    private final FsService fsService;
    @Nonnull
    private final ConfigService configService;
    @CheckForNull
    private HikariDataSource dataSource;

    public PersistenceService(@Nonnull FsService fsService, @Nonnull ConfigService configService) {
        this.fsService = fsService;
        this.configService = configService;
    }

    public void init() {
        LOG.info("Initializing DB connection pool");
        var dbPoolConfig = new HikariConfig();
        dbPoolConfig.setJdbcUrl(configService.getProperty(DbUrlProperty.class).getStringValue());
        dbPoolConfig.setUsername(configService.getProperty(DbUsernameProperty.class).getStringValue());
        dbPoolConfig.setPassword(configService.getProperty(DbPasswordProperty.class).getStringValue());
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

    @Nonnull
    private Connection getDbConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void loadState(@Nonnull Persistable persistable) throws IOException {
        Path persistenceFilePath = configService.getProperty(PersistencePathProperty.class).getValue()
                .resolve(persistable.getPersistenceKey() + ".state");
        if (fsService.isRegularFile(persistenceFilePath)) {
            fsService.inputFromFile(persistenceFilePath, persistable::load);
        }
    }

    public void saveState(@Nonnull Persistable persistable) throws IOException {
        Path persistenceFilePath = configService.getProperty(PersistencePathProperty.class).getValue()
                .resolve(persistable.getPersistenceKey() + ".state");
        fsService.outputToFile(persistenceFilePath, persistable::save);
    }

}
