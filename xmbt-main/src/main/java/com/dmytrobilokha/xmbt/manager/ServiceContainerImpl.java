package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.api.service.ServiceContainer;
import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.api.service.persistence.PersistenceService;

import javax.annotation.Nonnull;

public class ServiceContainerImpl implements ServiceContainer {

    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final PersistenceService persistenceService;

    public ServiceContainerImpl(@Nonnull ConfigService configService, @Nonnull PersistenceService persistenceService) {
        this.configService = configService;
        this.persistenceService = persistenceService;
    }

    @Nonnull
    @Override
    public ConfigService getConfigService() {
        return configService;
    }

    @Nonnull
    @Override
    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

}
