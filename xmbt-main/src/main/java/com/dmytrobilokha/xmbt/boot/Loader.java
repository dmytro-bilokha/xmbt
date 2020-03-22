package com.dmytrobilokha.xmbt.boot;

import com.dmytrobilokha.xmbt.api.BotFactory;
import com.dmytrobilokha.xmbt.bot.echo.EchoBotFactory;
import com.dmytrobilokha.xmbt.bot.ns.NsBotFactory;
import com.dmytrobilokha.xmbt.bot.nul.NullBotFactory;
import com.dmytrobilokha.xmbt.command.CommandFactory;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.fs.FsService;
import com.dmytrobilokha.xmbt.manager.BotManager;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import com.dmytrobilokha.xmbt.persistence.PersistenceService;
import com.dmytrobilokha.xmbt.xmpp.XmppConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class Loader {

    private static final List<Class> SERVICE_CLASSES = List.of(
            Cleaner.class, FsService.class, ConfigService.class, LoggerInitializer.class, PersistenceService.class
            , BotRegistry.class, XmppConnector.class, CommandFactory.class, BotManager.class
    );

    private static final List<Class<? extends BotFactory>> BOT_FACTORY_CLASSES = List.of(
            EchoBotFactory.class, NullBotFactory.class, NsBotFactory.class
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
        System.out.print(LocalDateTime.now() + ": Initializing...");
        try {
            beanRegistry.initServices(SERVICE_CLASSES);
            addShutdownHook(beanRegistry.getServiceBean(Cleaner.class));
            var cleaner = beanRegistry.getServiceBean(Cleaner.class);
            cleaner.registerThread(Thread.currentThread());
            System.out.println("OK");
        } catch (InitializationException | RuntimeException ex) {
            System.out.println(LocalDateTime.now() + ": FAIL! Check log for details");
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
        ServiceLoader<BotFactory> botFactoriesLoader = ServiceLoader.load(BotFactory.class);
        for (BotFactory botFactory : botFactoriesLoader) {
            botFactories.add(botFactory);
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

    private void addShutdownHook(@Nonnull Cleaner cleaner) {
        Runtime.getRuntime().addShutdownHook(cleaner);
    }

}
