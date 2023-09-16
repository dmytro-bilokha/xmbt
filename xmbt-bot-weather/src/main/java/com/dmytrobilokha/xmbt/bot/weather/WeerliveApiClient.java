package com.dmytrobilokha.xmbt.bot.weather;

import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.bot.weather.config.WeerliveApiKeyProperty;
import com.dmytrobilokha.xmbt.bot.weather.config.WeerliveApiUrlProperty;
import com.dmytrobilokha.xmbt.bot.weather.dto.LiveWeather;
import com.dmytrobilokha.xmbt.bot.weather.dto.WeerliveResponse;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

class WeerliveApiClient extends AbstractWeatherApiClient<LiveWeather> {

    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final Jsonb jsonb;

    WeerliveApiClient(@Nonnull ConfigService configService, @Nonnull Supplier<CloseableHttpClient> httpClientSupplier) {
        super("Weerlive", httpClientSupplier);
        this.configService = configService;
        this.jsonb = JsonbBuilder.create();
    }

    @Nonnull
    @Override
    protected String getFullUrl(@Nonnull City city) {
        String apiKey = configService.getProperty(WeerliveApiKeyProperty.class).getStringValue();
        String fullApiUrl = configService.getProperty(WeerliveApiUrlProperty.class).getStringValue()
                + "?key=" + apiKey + "&locatie=" + city.getLat() + "," + city.getLon();
        return fullApiUrl;
    }

    @Nonnull
    @Override
    protected LiveWeather parseApiResponse(@Nonnull String responseString) throws WeatherApiException {
        WeerliveResponse convertedResponse;
        try {
            convertedResponse = jsonb.fromJson(responseString, WeerliveResponse.class);
            log.debug("Converted response to JSON: '{}'", convertedResponse);
        } catch (JsonbException ex) {
            throw new WeatherApiException("Failed to convert response to JSON object. Response: " + responseString, ex);
        }
        var liveWeatherList = convertedResponse.getLiveWeather();
        if (liveWeatherList == null || liveWeatherList.isEmpty()) {
            throw new WeatherApiException("Got non valid response from the Weerlive API: " + convertedResponse);
        }
        return liveWeatherList.get(0);
    }

}
