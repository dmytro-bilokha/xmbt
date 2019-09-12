package com.dmytrobilokha.xmbt.config;

import com.dmytrobilokha.xmbt.boot.InitializationException;
import com.dmytrobilokha.xmbt.config.property.ConfigFilePathProperty;
import com.dmytrobilokha.xmbt.config.property.ConfigProperty;
import com.dmytrobilokha.xmbt.fs.FsService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigService {

    @Nonnull
    private final FsService fsService;
    @Nonnull
    private final Map<Class<? extends ConfigProperty>, ConfigProperty> configMap;
    @Nonnull
    private final Collection<ConfigPropertyProducer> systemPropertyProducers;
    @Nonnull
    private final Collection<ConfigPropertyProducer> configfilePropertyProducers;

    public ConfigService(
            @Nonnull FsService fsService
            , @Nonnull Collection<ConfigPropertyProducer> systemPropertyProducers
            , @Nonnull Collection<ConfigPropertyProducer> configfilePropertyProducers
    ) {
        this.fsService = fsService;
        this.systemPropertyProducers = systemPropertyProducers;
        this.configfilePropertyProducers = configfilePropertyProducers;
        this.configMap = new HashMap<>();
    }

    public void init() throws InitializationException {
        var systemPropertyErrors = loadConfigProperties(System.getProperties(), systemPropertyProducers);
        if (!systemPropertyErrors.isEmpty()) {
            throw new InitializationException("Failed to load command line properties: " + systemPropertyErrors);
        }
        if (!configMap.containsKey(ConfigFilePathProperty.class)) {
            throw new IllegalStateException("Config file path property must be among system properties, but it is not");
        }
        var configFilePath = getProperty(ConfigFilePathProperty.class).getValue();
        var rawProperties = new Properties();
        try {
            fsService.consumeFile(configFilePath, rawProperties::load);
        } catch (IOException ex) {
            throw new InitializationException("Failed to read config file '" + configFilePath + "'", ex);
        }
        var configPropertyErrors = loadConfigProperties(rawProperties, configfilePropertyProducers);
        if (!configPropertyErrors.isEmpty()) {
            throw new InitializationException("Config file contains errors: " + configPropertyErrors);
        }
    }

    @Nonnull
    private List<String> loadConfigProperties(@Nonnull Properties rawProperties
            , @Nonnull Collection<ConfigPropertyProducer> producers) {
        List<String> propertyErrors = new ArrayList<>();
        for (ConfigPropertyProducer propertyProducer : producers) {
            try {
                parseConfigProperty(propertyProducer, rawProperties);
            } catch (InvalidConfigException ex) {
                propertyErrors.add(ex.getMessage());
            }
        }
        return propertyErrors;
    }

    private void parseConfigProperty(@Nonnull ConfigPropertyProducer propertyProducer
            , @Nonnull Properties rawProperties) throws InvalidConfigException {
        var configProperty = propertyProducer.produce(rawProperties);
        configMap.put(configProperty.getClass(), configProperty);
    }

    @Nonnull
    public <T extends ConfigProperty> T getProperty(@Nonnull Class<T> propertyClass) {
        T property = propertyClass.cast(configMap.get(propertyClass));
        if (property == null) {
            throw new IllegalStateException("Requested property '" + propertyClass + "' not found");
        }
        return property;
    }

}
