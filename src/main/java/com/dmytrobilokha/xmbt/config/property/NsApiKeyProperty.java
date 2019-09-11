package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Properties;

public class NsApiKeyProperty extends ConfigProperty {

    public NsApiKeyProperty(@Nonnull Properties allProperties) throws InvalidConfigException {
        super("ns.api.key", allProperties);
    }

}
