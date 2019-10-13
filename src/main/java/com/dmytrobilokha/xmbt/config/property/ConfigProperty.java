package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Properties;

public abstract class ConfigProperty {

    @Nonnull
    protected final String stringValue;

    protected ConfigProperty(@Nonnull String key, @Nonnull Properties allProperties, @Nonnull String defaultValue) {
        String valueFromProperties = allProperties.getProperty(key);
        if (valueFromProperties == null) {
            this.stringValue = defaultValue;
            return;
        }
        this.stringValue = valueFromProperties;
    }

    protected ConfigProperty(@Nonnull String key, @Nonnull Properties allProperties) throws InvalidConfigException {
        String valueFromProperties = allProperties.getProperty(key);
        if (valueFromProperties == null || valueFromProperties.isBlank()) {
            throw new InvalidConfigException("Property '" + key + "' is mandatory but its value hasn't been provided");
        }
        this.stringValue = valueFromProperties;
    }

    public String getStringValue() {
        return stringValue;
    }

}
