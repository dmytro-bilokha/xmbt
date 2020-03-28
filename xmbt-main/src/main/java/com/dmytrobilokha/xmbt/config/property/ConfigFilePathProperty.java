package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.api.service.config.AbstractPathProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class ConfigFilePathProperty extends AbstractPathProperty {

    public ConfigFilePathProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("config.file", allProperties);
    }

}
