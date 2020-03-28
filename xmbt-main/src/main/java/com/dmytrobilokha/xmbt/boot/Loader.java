package com.dmytrobilokha.xmbt.boot;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.command.CommandFactory;
import com.dmytrobilokha.xmbt.config.ConfigServiceImpl;
import com.dmytrobilokha.xmbt.dictionary.FuzzyDictionaryFactoryImpl;
import com.dmytrobilokha.xmbt.fs.FsService;
import com.dmytrobilokha.xmbt.manager.BotManager;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import com.dmytrobilokha.xmbt.manager.ServiceContainerImpl;
import com.dmytrobilokha.xmbt.persistence.PersistenceServiceImpl;
import com.dmytrobilokha.xmbt.xmpp.XmppConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public final class Loader {

    private static final List<Class> SERVICE_CLASSES = List.of(
            Cleaner.class, FsService.class, ConfigServiceImpl.class, LoggerInitializer.class
            , PersistenceServiceImpl.class, BotRegistry.class, XmppConnector.class, CommandFactory.class
            , BotManager.class, ServiceContainerImpl.class, FuzzyDictionaryFactoryImpl.class
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
        ServiceLoader<BotFactory> botFactoriesLoader = ServiceLoader.load(BotFactory.class);
        for (BotFactory botFactory : botFactoriesLoader) {
            botFactories.add(botFactory);
        }
        if (botFactories.isEmpty()) {
            LOG.error("Failed to find any bot factory, shutting down");
            return;
        }
        LOG.info("Found following bots: {}", botFactories.stream()
                .map(BotFactory::getBotName)
                .collect(Collectors.joining(", "))
        );
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
