package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Properties;

public class ConfigFilePathProperty extends AbstractPathProperty {

    public ConfigFilePathProperty(@Nonnull Properties allProperties) throws InvalidConfigException {
        super("config.file", allProperties);
    }

}
