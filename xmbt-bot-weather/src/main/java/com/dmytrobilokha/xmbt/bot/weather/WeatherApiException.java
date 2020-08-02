package com.dmytrobilokha.xmbt.bot.weather;

import javax.annotation.Nonnull;

class WeatherApiException extends Exception {

    WeatherApiException(@Nonnull String message) {
        super(message);
    }

    WeatherApiException(@Nonnull String message, @Nonnull Exception ex) {
        super(message, ex);
    }

}
