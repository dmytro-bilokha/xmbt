package com.dmytrobilokha.xmbt.config.property;

import ch.qos.logback.classic.Level;

import javax.annotation.Nonnull;
import java.util.Map;

public class LogLevelProperty extends ConfigProperty {

    public LogLevelProperty(@Nonnull Map<String, String> allProperties) {
        super("log.level", allProperties, "INFO");
    }

    public Level getValue() {
        return Level.toLevel(stringValue);
    }

}
