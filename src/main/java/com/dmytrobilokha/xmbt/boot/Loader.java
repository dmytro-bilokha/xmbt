package com.dmytrobilokha.xmbt.boot;

import com.dmytrobilokha.xmbt.config.ConfigPropertyProducer;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.config.property.ConfigFilePathProperty;
import com.dmytrobilokha.xmbt.config.property.LogFilePathProperty;
import com.dmytrobilokha.xmbt.config.property.LogLevelProperty;
import com.dmytrobilokha.xmbt.config.property.NsApiKeyProperty;
import com.dmytrobilokha.xmbt.config.property.PidFilePathProperty;
import com.dmytrobilokha.xmbt.fs.FsService;
import com.dmytrobilokha.xmbt.manager.BotManager;
import com.dmytrobilokha.xmbt.xmpp.XmppConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Loader {

    private static final Logger LOG = LoggerFactory.getLogger(Loader.class);

    private static final List<ConfigPropertyProducer> SYSTEM_PROPERTY_PRODUCERS = List.of(
            ConfigFilePathProperty::new
            , LogFilePathProperty::new
            , LogLevelProperty::new
            , PidFilePathProperty::new
    );

    private static final List<ConfigPropertyProducer> CONFIGFILE_PROPERTY_PRODUCERS = List.of(
            NsApiKeyProperty::new
    );

    @Nonnull
    private final Cleaner cleaner;
    @Nonnull
    private final FsService fsService;
    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final XmppConnector xmppConnector;
    @Nonnull
    private final BotManager botManager;

    private Loader() {
        cleaner = new Cleaner();
        fsService = new FsService();
        List<ConfigPropertyProducer> propertyProducers = new ArrayList<>(CONFIGFILE_PROPERTY_PRODUCERS);
        propertyProducers.addAll(XmppConnector.getPropertyProducers());
        configService = new ConfigService(
                fsService, SYSTEM_PROPERTY_PRODUCERS, propertyProducers);
        xmppConnector = new XmppConnector(configService);
        botManager = new BotManager(xmppConnector, cleaner);
    }

    public static void main(@Nonnull String[] cliArgs) {
        new Loader().start();
    }

    private void start() {
        init();
        run();
    }

    private void init() {
        System.out.print("Initializing...");
        addShutdownHook();
        try {
            configService.init();
            new LoggerInitializer(configService).init();
            Path pidFilePath = configService.getProperty(PidFilePathProperty.class).getValue();
            writePidToFile(pidFilePath);
            cleaner.registerFile(pidFilePath);
            System.out.println("OK");
            detachFromTerminal();
        } catch (Exception ex) {
            System.out.println("FAIL! Check log for details");
            LOG.error("Failed to initialize the service", ex);
            System.exit(1);
        }
        LOG.info("Api key={}", configService.getProperty(NsApiKeyProperty.class).getStringValue());
    }

    //TODO: rename run() and start(), to not associate with Thread
    private void run() {
        botManager.start();
    }

    private void writePidToFile(@Nonnull Path pidFilePath) throws InitializationException {
        try {
            fsService.writeFile(pidFilePath, writer -> writer.write(Long.toString(ProcessHandle.current().pid())));
        } catch (IOException ex) {
            throw new InitializationException("Failed to save PID to file '" + pidFilePath + "'", ex);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(cleaner);
    }

    private void detachFromTerminal() throws IOException {
        System.in.close();
        System.out.close();
        System.err.close();
    }

}
