package com.dmytrobilokha.xmbt.bot.rain;

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
import java.util.Iterator;

public class RainBotFactory implements BotFactory {

    @Override
    @Nonnull
    public String getBotName() {
        return "r";
    }

    @Override
    @Nonnull
    public RainBot produce(@Nonnull MessageBus connector, @Nonnull ServiceContainer serviceContainer) {
        return new RainBot(
                connector
                , new BuienRadarApiClient(HttpClient.newHttpClient())
                , initDictionary(serviceContainer)
        );
    }

    @Nonnull
    private FuzzyDictionary<City> initDictionary(@Nonnull ServiceContainer serviceContainer) {
        FuzzyDictionary<City> cityDictionary = serviceContainer
                .getFuzzyDictionaryFactory()
                .produceWithLatinAlphabet();
        try (InputStream csvStream = this.getClass().getModule().getResourceAsStream("NL_cities.csv");
             Reader reader = new InputStreamReader(csvStream);
             BufferedReader csvReader = new BufferedReader(reader);
        ) {
            for (Iterator<String> iterator = csvReader.lines().iterator(); iterator.hasNext();) {
               var line = iterator.next().strip();
               if (!line.isBlank()) {
                   String[] data = line.split(",");
                   if (data.length != 3) {
                       throw new IllegalStateException("Non valid cites CSV line: " + line);
                   }
                   cityDictionary.put(data[0], new City(data[0], data[1], data[2]));
               }
            }
            if (cityDictionary.size() == 0) {
                throw new IllegalStateException("Failed to load cities with GPS coordinates from the resource");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load cities with GPS coordinates from the resource", e);
        }
        return cityDictionary;
    }

}
