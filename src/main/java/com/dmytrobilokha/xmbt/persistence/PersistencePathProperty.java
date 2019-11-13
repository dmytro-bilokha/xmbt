package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.AbstractPathProperty;

import javax.annotation.Nonnull;
import java.util.Properties;

public class PersistencePathProperty extends AbstractPathProperty {

    public PersistencePathProperty(@Nonnull Properties allProperties) throws InvalidConfigException {
        super("persistence.dir", allProperties);
    }

}
