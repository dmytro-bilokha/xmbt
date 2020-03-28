package com.dmytrobilokha.xmbt.api.service.config;

import javax.annotation.Nonnull;
import java.util.Map;

public abstract class AbstractIntProperty extends ConfigProperty {

    private final int value;

    protected AbstractIntProperty(
            @Nonnull String key, @Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super(key, allProperties);
        value = parsePropertyValue(key);
    }

    protected AbstractIntProperty(
            @Nonnull String key
            , @Nonnull Map<String, String> allProperties
            , int defaultValue
    ) throws InvalidConfigException {
        super(key, allProperties, Integer.toString(defaultValue));
        value = parsePropertyValue(key);
    }

    private int parsePropertyValue(@Nonnull String key) throws InvalidConfigException {
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ex) {
            throw new InvalidConfigException("Provided " + key + " property value '"
                    + stringValue + "' couldn't be converted to a valid int", ex);
        }
    }

    public int getValue() {
        return value;
    }

}

