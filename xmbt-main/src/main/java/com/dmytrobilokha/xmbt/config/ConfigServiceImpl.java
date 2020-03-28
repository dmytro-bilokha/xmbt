package com.dmytrobilokha.xmbt.config;

import com.dmytrobilokha.xmbt.api.service.config.ConfigProperty;
import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.boot.Initializable;
import com.dmytrobilokha.xmbt.boot.InitializationException;
import com.dmytrobilokha.xmbt.config.property.ConfigFilePathProperty;
import com.dmytrobilokha.xmbt.fs.FsService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigServiceImpl implements ConfigService, Initializable {

    @Nonnull
    private final FsService fsService;
    @Nonnull
    private final Map<String, String> rawPropertiesMap;
    @Nonnull
    private final Map<Class<? extends ConfigProperty>, ConfigProperty> parsedPropertiesMap;
    @Nonnull
    private final Object lock;

    public ConfigServiceImpl(@Nonnull FsService fsService) {
        this.fsService = fsService;
        this.rawPropertiesMap = new HashMap<>();
        this.parsedPropertiesMap = new HashMap<>();
        this.lock = new Object();
    }

    @Override
    public void init() throws InitializationException {
        synchronized (lock) {
            rawPropertiesMap.clear();
            parsedPropertiesMap.clear();
            var systemProperties = System.getProperties();
            fillRawPropertiesMap(systemProperties);
            ConfigFilePathProperty configFilePathProperty;
            try {
                configFilePathProperty = new ConfigFilePathProperty(rawPropertiesMap);
            } catch (InvalidConfigException ex) {
                throw new InitializationException("Failed to get config file path property", ex);
            }
            var configFilePath = configFilePathProperty.getValue();
            var configFileProperties = new Properties();
            try {
                fsService.readFile(configFilePath, configFileProperties::load);
            } catch (IOException ex) {
                throw new InitializationException("Failed to read config file '" + configFilePath + "'", ex);
            }
            fillRawPropertiesMap(configFileProperties);
            //Fill again, because system properties should override properties from config file
            fillRawPropertiesMap(systemProperties);
        }
    }

    private void fillRawPropertiesMap(@Nonnull Properties properties) {
        for (String propertyKey : properties.stringPropertyNames()) {
            rawPropertiesMap.put(propertyKey, properties.getProperty(propertyKey));
        }
    }

    private <T extends ConfigProperty> T parseAndCacheConfigProperty(
            @Nonnull Class<T> propertyClass) throws InvalidConfigException {
        try {
            Constructor<T> propertyConstructor = propertyClass.getConstructor(Map.class);
            T configProperty = propertyConstructor.newInstance(rawPropertiesMap);
            parsedPropertiesMap.put(propertyClass, configProperty);
            return configProperty;
        } catch (InstantiationException ex) {
            throw new InvalidConfigException("Could not parse config property of class '" + propertyClass
                    + "', because it is of abstract class", ex);
        } catch (InvocationTargetException ex) {
            throw new InvalidConfigException("Could not parse config property of class '" + propertyClass
                    + "', because its constructor has thrown an exception", ex);
        } catch (NoSuchMethodException ex) {
            throw new InvalidConfigException("Could not parse config property of class '" + propertyClass
                    + "', because it has no constructor accepting Map<String, String>", ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidConfigException("Could not parse config property of class '" + propertyClass
                    + "', because its constructor accepting Map<String, String> isn't accessible", ex);
        }
    }

    @Override
    @Nonnull
    public <T extends ConfigProperty> T getProperty(@Nonnull Class<T> propertyClass) {
        synchronized (lock) {
            T property = propertyClass.cast(parsedPropertiesMap.get(propertyClass));
            if (property != null) {
                return property;
            }
            try {
                property = parseAndCacheConfigProperty(propertyClass);
            } catch (InvalidConfigException ex) {
                throw new IllegalStateException(ex);
            }
            return property;
        }
    }

}
