package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Properties;

public class LogFilePathProperty extends AbstractPathProperty {

    public LogFilePathProperty(@Nonnull Properties allProperties) throws InvalidConfigException {
        super("log.file", allProperties);
    }

}
