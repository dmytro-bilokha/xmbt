package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Properties;

public class PidFilePathProperty extends AbstractPathProperty {

    public PidFilePathProperty(@Nonnull Properties allProperties) throws InvalidConfigException {
        super("pid.file", allProperties);
    }

}
