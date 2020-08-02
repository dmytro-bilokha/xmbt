package com.dmytrobilokha.xmbt.bot.weather.config;

import com.dmytrobilokha.xmbt.api.service.config.ConfigProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class WeerliveApiKeyProperty extends ConfigProperty {

    public WeerliveApiKeyProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("weerlive.api.key", allProperties);
    }

}
