package com.dmytrobilokha.xmbt.api.service.config;

import javax.annotation.Nonnull;

public interface ConfigService {

    @Nonnull
    <T extends ConfigProperty> T getProperty(@Nonnull Class<T> propertyClass);

}
