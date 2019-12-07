package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.BotFactory;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.persistence.PersistenceService;

import javax.annotation.Nonnull;

public class NsBotFactory implements BotFactory {

    @Nonnull
    private final PersistenceService persistenceService;
    @Nonnull
    private final ConfigService configService;

    public NsBotFactory(
            @Nonnull PersistenceService persistenceService
            , @Nonnull ConfigService configService
    ) {
        this.persistenceService = persistenceService;
        this.configService = configService;
    }

    @Override
    @Nonnull
    public String getBotName() {
        return "ns";
    }

    @Override
    @Nonnull
    public NsBot produce(@Nonnull BotConnector connector) {
        return new NsBot(connector, persistenceService, configService);
    }

}
