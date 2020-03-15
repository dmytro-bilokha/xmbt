package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.AbstractIntProperty;

import javax.annotation.Nonnull;
import java.util.Map;

public class DbPoolSizeProperty extends AbstractIntProperty {

    public DbPoolSizeProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("db.poolsize", allProperties, 1);
    }

}
