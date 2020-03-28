package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;

import javax.annotation.Nonnull;
import java.net.http.HttpClient;

public class NsBotFactory implements BotFactory {

    @Override
    @Nonnull
    public String getBotName() {
        return "ns";
    }

    @Override
    @Nonnull
    public NsBot produce(@Nonnull MessageBus connector, @Nonnull ServiceContainer serviceContainer) {
        var dao = new NsTrainStationDao(serviceContainer.getPersistenceService());
        var apiClient = new NsApiClient(serviceContainer.getConfigService(), HttpClient.newHttpClient());
        var service = new NsService(dao, apiClient);
        return new NsBot(connector, service);
    }

}
