package com.dmytrobilokha.xmbt.config.property;

import ch.qos.logback.classic.Level;

import javax.annotation.Nonnull;
import java.util.Properties;

public class LogLevelProperty extends ConfigProperty {

    public LogLevelProperty(@Nonnull Properties allProperties) {
        super("log.level", allProperties, "INFO");
    }

    public Level getValue() {
        return Level.toLevel(stringValue);
    }

}
