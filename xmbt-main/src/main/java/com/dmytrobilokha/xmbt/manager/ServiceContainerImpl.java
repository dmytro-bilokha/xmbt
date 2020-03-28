package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.api.service.ServiceContainer;
import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.api.service.dictionary.FuzzyDictionaryFactory;
import com.dmytrobilokha.xmbt.api.service.persistence.PersistenceService;

import javax.annotation.Nonnull;

public class ServiceContainerImpl implements ServiceContainer {

    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final PersistenceService persistenceService;
    @Nonnull
    private final FuzzyDictionaryFactory fuzzyDictionaryFactory;

    public ServiceContainerImpl(
            @Nonnull ConfigService configService
            , @Nonnull PersistenceService persistenceService
            , @Nonnull FuzzyDictionaryFactory fuzzyDictionaryFactory
    ) {
        this.configService = configService;
        this.persistenceService = persistenceService;
        this.fuzzyDictionaryFactory = fuzzyDictionaryFactory;
    }

    @Override
    @Nonnull
    public ConfigService getConfigService() {
        return configService;
    }

    @Override
    @Nonnull
    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    @Override
    @Nonnull
    public FuzzyDictionaryFactory getFuzzyDictionaryFactory() {
        return fuzzyDictionaryFactory;
    }

}
