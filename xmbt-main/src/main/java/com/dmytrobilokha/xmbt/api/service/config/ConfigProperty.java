package com.dmytrobilokha.xmbt.api.service.config;

import javax.annotation.Nonnull;
import java.util.Map;

public abstract class ConfigProperty {

    @Nonnull
    protected final String stringValue;

    protected ConfigProperty(
            @Nonnull String key, @Nonnull Map<String, String> allProperties, @Nonnull String defaultValue) {
        this.stringValue = allProperties.getOrDefault(key, defaultValue);
    }

    protected ConfigProperty(
            @Nonnull String key, @Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        String valueFromProperties = allProperties.get(key);
        if (valueFromProperties == null || valueFromProperties.isBlank()) {
            throw new InvalidConfigException("Property '" + key + "' is mandatory but its value hasn't been provided");
        }
        this.stringValue = valueFromProperties;
    }

    public String getStringValue() {
        return stringValue;
    }

}
