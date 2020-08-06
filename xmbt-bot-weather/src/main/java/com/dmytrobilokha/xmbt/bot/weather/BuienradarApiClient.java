package com.dmytrobilokha.xmbt.bot.weather;

import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.bot.weather.config.BuienradarApiUrlProperty;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

class BuienradarApiClient extends AbstractWeatherApiClient<RainForecast> {

    @Nonnull
    private final ConfigService configService;

    BuienradarApiClient(
            @Nonnull ConfigService configService, @Nonnull Supplier<CloseableHttpClient> httpClientSupplier) {
        super("Buienradar", httpClientSupplier);
        this.configService = configService;
    }

    @Nonnull
    @Override
    protected String getFullUrl(@Nonnull City city) {
        var fullApiUrl = configService.getProperty(BuienradarApiUrlProperty.class).getStringValue()
                + "?lat=" + city.getLat() + "&lon=" + city.getLon();
        return fullApiUrl;
    }

    @Nonnull
    @Override
    protected RainForecast parseApiResponse(@Nonnull String responseString) throws WeatherApiException {
        String startTime = null;
        String endTime = null;
        List<Double> precipitationLevel = new ArrayList<>();
        for (Iterator<String> lineIterator = responseString.lines().iterator(); lineIterator.hasNext();) {
            var line = lineIterator.next();
            String timeString;
            try {
                var level = Integer.parseInt(line.substring(0, 3), 10);
                precipitationLevel.add(Math.pow(10, (level - 109) / 32.0));
                timeString = line.substring(4);
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                throw new WeatherApiException(
                        "Unable to parse the line '" + line + "' from the response '" + responseString + "'", ex);
            }
            if (startTime == null) {
                startTime = timeString;
            }
            endTime = timeString;
        }
        if (startTime == null) {
            throw new WeatherApiException("Got non-valid response from the Buienradar API '" + responseString + '\'');
        }
        return new RainForecast(startTime, endTime, precipitationLevel);
    }

}
