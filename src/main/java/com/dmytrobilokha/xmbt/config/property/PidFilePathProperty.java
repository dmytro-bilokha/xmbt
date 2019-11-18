package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class PidFilePathProperty extends AbstractPathProperty {

    public PidFilePathProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("pid.file", allProperties);
    }

}
