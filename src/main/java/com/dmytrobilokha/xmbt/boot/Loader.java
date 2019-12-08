package com.dmytrobilokha.xmbt.boot;

import com.dmytrobilokha.xmbt.api.BotFactory;
import com.dmytrobilokha.xmbt.bot.echo.EchoBotFactory;
import com.dmytrobilokha.xmbt.bot.ns.NsBotFactory;
import com.dmytrobilokha.xmbt.command.CommandFactory;
import com.dmytrobilokha.xmbt.config.ConfigService;
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
import java.util.ArrayList;
import java.util.List;

public final class Loader {

    private static final List<Class> SERVICE_CLASSES = List.of(
            Cleaner.class, FsService.class, ConfigService.class, LoggerInitializer.class, PersistenceService.class
            , BotRegistry.class, XmppConnector.class, CommandFactory.class, BotManager.class
    );

    private static final List<Class<? extends BotFactory>> BOT_FACTORY_CLASSES = List.of(
            EchoBotFactory.class, NsBotFactory.class
    );

    private static final Logger LOG = LoggerFactory.getLogger(Loader.class);

    @Nonnull
    private final BeanRegistry beanRegistry;

    private Loader() {
        beanRegistry = new BeanRegistry();
    }

    public static void main(@Nonnull String[] cliArgs) {
        Loader loader = new Loader();
        loader.init();
        loader.go();
    }

    private void init() {
        System.out.print("Initializing...");
        try {
            beanRegistry.initServices(SERVICE_CLASSES);
            addShutdownHook(beanRegistry.getServiceBean(Cleaner.class));
            var configService = beanRegistry.getServiceBean(ConfigService.class);
            Path pidFilePath = configService.getProperty(PidFilePathProperty.class).getValue();
            writePidToFile(beanRegistry.getServiceBean(FsService.class), pidFilePath);
            var cleaner = beanRegistry.getServiceBean(Cleaner.class);
            cleaner.registerFile(pidFilePath);
            cleaner.registerThread(Thread.currentThread());
            System.out.println("OK");
            detachFromTerminal();
        } catch (InitializationException | IOException | RuntimeException ex) {
            System.out.println("FAIL! Check log for details");
            LOG.error("Failed to initialize the service", ex);
            System.exit(1);
        }
        LOG.info("Initialized");
    }

    private void go() {
        var botFactories = new ArrayList<BotFactory>();
        for (Class<? extends BotFactory> factoryClass : BOT_FACTORY_CLASSES) {
            try {
                var botFactory = beanRegistry.initBean(factoryClass);
                if (botFactory == null) {
                    LOG.error("Failed to find all required dependencies to init bot factory {}. "
                            + "Will proceed without this bot", factoryClass);
                } else {
                    botFactories.add(botFactory);
                }
            } catch (InitializationException ex) {
                LOG.error("Failed to init bot factory {}. Will proceed without this bot", factoryClass);
            }
        }
        if (botFactories.isEmpty()) {
            LOG.error("Failed to initialize any bot factory, shutting down");
            return;
        }
        try {
            beanRegistry.getServiceBean(BotManager.class).go(botFactories);
        } catch (InitializationException | RuntimeException ex) {
            LOG.error("Got unexpected exception, shutting down", ex);
        }
    }

    private void writePidToFile(
            @Nonnull FsService fsService, @Nonnull Path pidFilePath) throws InitializationException {
        try {
            fsService.writeFile(pidFilePath, writer -> writer.write(Long.toString(ProcessHandle.current().pid())));
        } catch (IOException ex) {
            throw new InitializationException("Failed to save PID to file '" + pidFilePath + "'", ex);
        }
    }

    private void addShutdownHook(@Nonnull Cleaner cleaner) {
        Runtime.getRuntime().addShutdownHook(cleaner);
    }

    private void detachFromTerminal() throws IOException {
        System.in.close();
        System.out.close();
        System.err.close();
    }

}
