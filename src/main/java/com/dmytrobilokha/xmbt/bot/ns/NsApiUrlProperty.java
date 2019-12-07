package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.ConfigProperty;

import javax.annotation.Nonnull;
import java.util.Map;

public class NsApiUrlProperty extends ConfigProperty {

    public NsApiUrlProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("ns.api.url", allProperties);
    }

}
