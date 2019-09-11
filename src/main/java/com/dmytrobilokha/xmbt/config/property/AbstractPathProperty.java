package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Properties;

abstract class AbstractPathProperty extends ConfigProperty {

    @Nonnull
    private final Path path;

    AbstractPathProperty(@Nonnull String key, @Nonnull Properties allProperties) throws InvalidConfigException {
        super(key, allProperties);
        try {
            path = Path.of(stringValue);
        } catch (InvalidPathException ex) {
            throw new InvalidConfigException("Provided " + key + " property value '"
                    + stringValue + "' couldn't be converted to a valid filesystem path", ex);
        }
    }

    public Path getValue() {
        return path;
    }

}
