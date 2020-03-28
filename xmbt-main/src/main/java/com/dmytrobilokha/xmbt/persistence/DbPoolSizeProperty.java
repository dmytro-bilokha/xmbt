package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.api.service.config.AbstractIntProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class DbPoolSizeProperty extends AbstractIntProperty {

    public DbPoolSizeProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("db.poolsize", allProperties, 1);
    }

}
