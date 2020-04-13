package com.dmytrobilokha.xmbt.bot.webgateway.config;

import com.dmytrobilokha.xmbt.api.service.config.AbstractIntProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class WebGatewayBindPort extends AbstractIntProperty {

    public WebGatewayBindPort(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("wg.bind.port", allProperties);
    }

}
