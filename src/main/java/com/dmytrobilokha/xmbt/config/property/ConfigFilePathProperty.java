package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class ConfigFilePathProperty extends AbstractPathProperty {

    public ConfigFilePathProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("config.file", allProperties);
    }

}
