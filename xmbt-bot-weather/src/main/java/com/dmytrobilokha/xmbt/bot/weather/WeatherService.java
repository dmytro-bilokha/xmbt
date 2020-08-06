package com.dmytrobilokha.xmbt.bot.weather;

import com.dmytrobilokha.xmbt.bot.weather.dto.LiveWeather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class WeatherService {

    private static final double NO_RAIN_LIMIT = 0.2;
    private static final double LIGHT_RAIN_LIMIT = 2.5;
    private static final double MODERATE_RAIN_LIMIT = 10.0;
    private static final double HEAVY_RAIN_LIMIT = 50.0;

    private static final Logger LOG = LoggerFactory.getLogger(WeatherService.class);

    @Nonnull
    private final WeerliveApiClient weerliveApiClient;
    @Nonnull
    private final BuienradarApiClient buienradarApiClient;

    WeatherService(@Nonnull WeerliveApiClient weerliveApiClient, @Nonnull BuienradarApiClient buienradarApiClient) {
        this.weerliveApiClient = weerliveApiClient;
        this.buienradarApiClient = buienradarApiClient;
    }

    @CheckForNull
    String fetchWeatherReport(@Nonnull City city) {
        LiveWeather weerliveResponse = null;
        try {
            weerliveResponse = weerliveApiClient.fetch(city);
        } catch (WeatherApiException e) {
            LOG.error("Failed to fetch weather report from the Weerlive API for {}", city, e);
        }
        RainForecast buienradarResponse = null;
        try {
            buienradarResponse = buienradarApiClient.fetch(city);
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
        for (double level : forecast.getPrecipitationLevel()) {
            if (level < NO_RAIN_LIMIT) {
                // x < 0.2 mm/h -> no rain
                fb.append('_');
            } else if (level < LIGHT_RAIN_LIMIT) {
                // 0.2 < x < 2.5 mm/h -> light rain
                fb.append('L');
            } else if (level < MODERATE_RAIN_LIMIT) {
                // 2.5 < x < 10 mm/h -> moderate rain
                fb.append('M');
            } else if (level < HEAVY_RAIN_LIMIT) {
                // 10 < x < 50 mm/h -> heavy rain
                fb.append('H');
            } else {
                // x > 50 mm/h -> violent rain
                fb.append('V');
            }
        }
        return fb.append('|').append(forecast.getEndTime()).toString();
    }

}
