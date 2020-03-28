package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.service.config.ConfigProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class NsApiUrlProperty extends ConfigProperty {

    public NsApiUrlProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("ns.api.url", allProperties);
    }

}
