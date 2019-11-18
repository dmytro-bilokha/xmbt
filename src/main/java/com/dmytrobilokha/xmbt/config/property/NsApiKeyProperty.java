package com.dmytrobilokha.xmbt.config.property;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class NsApiKeyProperty extends ConfigProperty {

    public NsApiKeyProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("ns.api.key", allProperties);
    }

}
