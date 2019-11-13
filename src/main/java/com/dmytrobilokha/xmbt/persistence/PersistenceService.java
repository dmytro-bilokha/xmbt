package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.api.Persistable;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.fs.FsService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

public class PersistenceService {

    @Nonnull
    private final FsService fsService;
    @Nonnull
    private final ConfigService configService;

    public PersistenceService(@Nonnull FsService fsService, @Nonnull ConfigService configService) {
        this.fsService = fsService;
        this.configService = configService;
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
