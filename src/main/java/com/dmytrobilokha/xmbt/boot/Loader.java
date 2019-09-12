package com.dmytrobilokha.xmbt.boot;

import com.dmytrobilokha.xmbt.config.ConfigPropertyProducer;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.config.property.ConfigFilePathProperty;
import com.dmytrobilokha.xmbt.config.property.LogFilePathProperty;
import com.dmytrobilokha.xmbt.config.property.LogLevelProperty;
import com.dmytrobilokha.xmbt.config.property.NsApiKeyProperty;
import com.dmytrobilokha.xmbt.fs.FsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

public class Loader {

    private static final Logger LOG = LoggerFactory.getLogger(Loader.class);

    private static final List<ConfigPropertyProducer> SYSTEM_PROPERTY_PRODUCERS = List.of(
            ConfigFilePathProperty::new
            , LogFilePathProperty::new
            , LogLevelProperty::new
    );

    private static final List<ConfigPropertyProducer> CONFIGFILE_PROPERTY_PRODUCERS = List.of(
            NsApiKeyProperty::new
    );

    public static void main(@Nonnull String[] cliArgs) {
        FsService fsService = new FsService();
        ConfigService configService = new ConfigService(
                fsService, SYSTEM_PROPERTY_PRODUCERS, CONFIGFILE_PROPERTY_PRODUCERS);
        try {
            configService.init();
        } catch (InitializationException ex) {
            LOG.error("Failed to initialize config service", ex);
            System.exit(1);
        }
        new LoggerInitializer(configService).init();
        LOG.info("Api key={}", configService.getProperty(NsApiKeyProperty.class).getStringValue());
    }

    public String getName() {
        return "xmbt";
    }

}
