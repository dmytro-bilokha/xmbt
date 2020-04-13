package com.dmytrobilokha.xmbt.bot.webgateway.config;

import com.dmytrobilokha.xmbt.api.service.config.ConfigProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class WebGatewayBindAddress extends ConfigProperty {

    public WebGatewayBindAddress(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("wg.bind.address", allProperties);
    }

}
