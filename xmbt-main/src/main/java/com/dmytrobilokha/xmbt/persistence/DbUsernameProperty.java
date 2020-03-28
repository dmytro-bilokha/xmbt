package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.api.service.config.ConfigProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class DbUsernameProperty extends ConfigProperty {

    public DbUsernameProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("db.username", allProperties);
    }

}
