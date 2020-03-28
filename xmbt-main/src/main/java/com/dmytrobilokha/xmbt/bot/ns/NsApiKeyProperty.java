package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.service.config.ConfigProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class NsApiKeyProperty extends ConfigProperty {

    public NsApiKeyProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("ns.api.key", allProperties);
    }

}
