package com.dmytrobilokha.xmbt.bot.weather;

import com.dmytrobilokha.xmbt.bot.weather.dto.LiveWeather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class WeatherService {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherService.class);
    private static final String[] RAIN_SYMBOLS = new String[]{" ", "▁", "▂", "▃", "▄", "▅", "▆", "▇", "█"};

    @Nonnull
    private final WeerliveApiClient weerliveApiClient;
    @Nonnull
    private final BuienradarApiClient buienradarApiClient;

    WeatherService(@Nonnull WeerliveApiClient weerliveApiClient, @Nonnull BuienradarApiClient buienradarApiClient) {
        this.weerliveApiClient = weerliveApiClient;
        this.buienradarApiClient = buienradarApiClient;
    }

    @CheckForNull
    String fetchWeatherReport(@Nonnull City city) throws InterruptedException {
        LiveWeather weerliveResponse = null;
        try {
            weerliveResponse = weerliveApiClient.fetchWeatherData(city);
        } catch (WeatherApiException e) {
            LOG.error("Failed to fetch weather report from the Weerlive API for {}", city, e);
        }
        RainForecast buienradarResponse = null;
        try {
            buienradarResponse = buienradarApiClient.fetchRainForecast(city);
        } catch (WeatherApiException e) {
            LOG.error("Failed to fetch rain forecast from the BuienRadar API for {}", city, e);
        }
        if (weerliveResponse == null && buienradarResponse == null) {
            return null;
        }
        return "Weather in " + city.getName() + System.lineSeparator()
                + (weerliveResponse == null ? "" : buildWeerliveReport(weerliveResponse))
                + (buienradarResponse == null ? "" : buildBuienRadarReport(buienradarResponse));
    }

    @Nonnull
    private String buildWeerliveReport(@Nonnull LiveWeather liveWeather) {
        String alarmText;
        var apiAlarmText = liveWeather.getAlarmText();
        if (apiAlarmText == null || apiAlarmText.isBlank()) {
            alarmText = "";
        } else {
            alarmText = "ALARM! " + apiAlarmText + System.lineSeparator();
        }
        return alarmText
                + liveWeather.getTemperature() + "°C, like " + liveWeather.getFeelsLikeTemperature() + "°C, "
                + liveWeather.getRelativeHumidity() + "% "
                + liveWeather.getAirPressureMmHg() + " mmHg" + System.lineSeparator()
                + liveWeather.getWindDirection() + ' ' + liveWeather.getWindForceBeaufort() + " Bft "
                + liveWeather.getWindSpeedKmh() + " km/h" + System.lineSeparator()
                + "Expect: " + liveWeather.getD0MinTemperature() + '/' + liveWeather.getD0MaxTemperature()
                + "°C Psun/Prain=" + liveWeather.getD0SunProbability()
                + '/' + liveWeather.getD0PrecipitationProbability()
                + '%' + System.lineSeparator()
                + liveWeather.getD0WindDirection() + ' ' + liveWeather.getD0WindForceBeaufort() + " Bft "
                + liveWeather.getD0WindSpeedKmh() + " km/h" + System.lineSeparator()
                ;
    }

    @Nonnull
    private String buildBuienRadarReport(@Nonnull RainForecast forecast) {
        StringBuilder fb = new StringBuilder(forecast.getStartTime()).append('|');
        for (int level : forecast.getPrecipitationLevel()) {
            fb.append(RAIN_SYMBOLS[Math.min(RAIN_SYMBOLS.length - 1, Math.max(0, RAIN_SYMBOLS.length * level / 256))]);
        }
        return fb.append('|').append(forecast.getEndTime()).toString();
    }

}
