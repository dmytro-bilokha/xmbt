package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class LogFilePathProperty extends AbstractPathProperty {

    public LogFilePathProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("log.file", allProperties);
    }

}
