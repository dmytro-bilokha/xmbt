package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.ConfigProperty;

import javax.annotation.Nonnull;
import java.util.Map;

public class DbUsernameProperty extends ConfigProperty {

    public DbUsernameProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("db.username", allProperties);
    }

}