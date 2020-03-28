package com.dmytrobilokha.xmbt.api.service.config;

import javax.annotation.Nonnull;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;

public abstract class AbstractPathProperty extends ConfigProperty {

    @Nonnull
    private final Path path;

    public AbstractPathProperty(
            @Nonnull String key, @Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super(key, allProperties);
        try {
            this.path = Path.of(stringValue);
        } catch (InvalidPathException ex) {
            throw new InvalidConfigException("Provided " + key + " property value '"
                    + stringValue + "' couldn't be converted to a valid filesystem path", ex);
        }
    }

    public Path getValue() {
        return path;
    }

}
