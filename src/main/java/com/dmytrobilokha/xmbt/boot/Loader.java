package com.dmytrobilokha.xmbt.boot;

import com.dmytrobilokha.xmbt.command.CommandFactory;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.config.property.NsApiKeyProperty;
import com.dmytrobilokha.xmbt.config.property.PidFilePathProperty;
import com.dmytrobilokha.xmbt.fs.FsService;
import com.dmytrobilokha.xmbt.manager.BotManager;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import com.dmytrobilokha.xmbt.persistence.PersistenceService;
import com.dmytrobilokha.xmbt.xmpp.XmppConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

public final class Loader {

    private static final Logger LOG = LoggerFactory.getLogger(Loader.class);

    @Nonnull
    private final Cleaner cleaner;
    @Nonnull
    private final FsService fsService;
    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final PersistenceService persistenceService;
    @Nonnull
    private final XmppConnector xmppConnector;
    @Nonnull
    private final BotManager botManager;

    private Loader() {
        cleaner = new Cleaner();
        fsService = new FsService();
        configService = new ConfigService(fsService);
        persistenceService = new PersistenceService(configService);
        BotRegistry botRegistry = new BotRegistry(cleaner);
        xmppConnector = new XmppConnector(configService, botRegistry);
        botManager = new BotManager(
                xmppConnector
                , botRegistry
                , new CommandFactory(persistenceService)
        );
    }

    public static void main(@Nonnull String[] cliArgs) {
        Loader loader = new Loader();
        loader.init();
        loader.go();
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
            cleaner.registerThread(Thread.currentThread());
            persistenceService.init();
            System.out.println("OK");
            detachFromTerminal();
        } catch (Exception ex) {
            System.out.println("FAIL! Check log for details");
            LOG.error("Failed to initialize the service", ex);
            System.exit(1);
        }
        LOG.info("Api key={}", configService.getProperty(NsApiKeyProperty.class).getStringValue());
    }

    private void go() {
        try {
            botManager.go();
        } catch (RuntimeException ex) {
            LOG.error("Got unexpected runtime exception, shutting down", ex);
        }
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
