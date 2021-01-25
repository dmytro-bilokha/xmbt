package com.dmytrobilokha.xmbt.bot.ns;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;
import com.dmytrobilokha.xmbt.api.service.dictionary.FuzzyDictionary;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.TimeValue;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

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
        var apiClient = new NsApiClient(
                serviceContainer.getConfigService()
                , this::produceApacheHttpClient
        );
        FuzzyDictionary<NsTrainStation> stationsDictionary = serviceContainer
                .getFuzzyDictionaryFactory()
                .produceWithLatinAlphabet();
        var service = new NsService(dao, apiClient, stationsDictionary);
        return new NsBot(connector, service);
    }

    @Nonnull
    private CloseableHttpClient produceApacheHttpClient() {
        var requestConfig = RequestConfig
                .copy(RequestConfig.DEFAULT)
                .setConnectionRequestTimeout(15L, TimeUnit.SECONDS)
                .setConnectTimeout(15L, TimeUnit.SECONDS)
                .setResponseTimeout(15L, TimeUnit.SECONDS)
                .setConnectionKeepAlive(TimeValue.ofSeconds(10))
                .build();
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

}
