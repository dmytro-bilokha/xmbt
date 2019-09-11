package com.dmytrobilokha.xmbt.config;

import com.dmytrobilokha.xmbt.config.property.ConfigProperty;

import java.util.Properties;

@FunctionalInterface
public interface ConfigPropertyProducer {

    ConfigProperty produce(Properties properties) throws InvalidConfigException;

}
