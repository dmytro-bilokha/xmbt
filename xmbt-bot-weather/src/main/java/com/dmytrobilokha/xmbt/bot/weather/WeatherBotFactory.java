package com.dmytrobilokha.xmbt.bot.weather;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;
import com.dmytrobilokha.xmbt.api.service.dictionary.FuzzyDictionary;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;

public class WeatherBotFactory implements BotFactory {

    private static final int COLUMNS_IN_CITIES_FILE = 3;

    @Override
    @Nonnull
    public String getBotName() {
        return "wr";
    }

    @Override
    @Nonnull
    public WeatherBot produce(@Nonnull MessageBus connector, @Nonnull ServiceContainer serviceContainer) {
        var weerliveApiClient = new WeerliveApiClient(
                serviceContainer.getConfigService()
                , HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(5))
                .build()
        );
        var buienradarApiClient = new BuienradarApiClient(
                serviceContainer.getConfigService()
                , HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(5))
                .build());
        var weatherService = new WeatherService(weerliveApiClient, buienradarApiClient);
        return new WeatherBot(
                connector
                , weatherService
                , initDictionary(serviceContainer)
        );
    }

    @Nonnull
    private FuzzyDictionary<City> initDictionary(@Nonnull ServiceContainer serviceContainer) {
        FuzzyDictionary<City> cityDictionary = serviceContainer
                .getFuzzyDictionaryFactory()
                .produceWithLatinAlphabet();
        try (InputStream csvStream = this.getClass().getModule().getResourceAsStream("NL_cities.csv");
             Reader reader = new InputStreamReader(csvStream, StandardCharsets.UTF_8);
             BufferedReader csvReader = new BufferedReader(reader);
        ) {
            for (Iterator<String> iterator = csvReader.lines().iterator(); iterator.hasNext();) {
                var line = iterator.next().strip();
                if (!line.isBlank()) {
                    String[] data = line.split(",");
                    if (data.length != COLUMNS_IN_CITIES_FILE) {
                        throw new IllegalStateException("Non valid cites CSV line: " + line);
                    }
                    cityDictionary.put(data[0], new City(data[0], data[1], data[2]));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load cities with GPS coordinates from the resource", e);
        }
        if (cityDictionary.size() == 0) {
            throw new IllegalStateException("Failed to load cities with GPS coordinates from the resource");
        }
        return cityDictionary;
    }

}
