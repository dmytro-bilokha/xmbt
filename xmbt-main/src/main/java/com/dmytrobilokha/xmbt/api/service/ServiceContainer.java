package com.dmytrobilokha.xmbt.api.service;

import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.api.service.persistence.PersistenceService;

import javax.annotation.Nonnull;

public interface ServiceContainer {

    @Nonnull
    ConfigService getConfigService();
    
    @Nonnull
    PersistenceService getPersistenceService();

}
