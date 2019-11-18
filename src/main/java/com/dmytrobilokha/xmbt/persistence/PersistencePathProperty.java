package com.dmytrobilokha.xmbt.persistence;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.AbstractPathProperty;

import javax.annotation.Nonnull;
import java.util.Map;

public class PersistencePathProperty extends AbstractPathProperty {

    public PersistencePathProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("persistence.dir", allProperties);
    }

}
