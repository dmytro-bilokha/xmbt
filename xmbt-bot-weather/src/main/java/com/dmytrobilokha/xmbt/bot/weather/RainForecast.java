package com.dmytrobilokha.xmbt.bot.weather;

import javax.annotation.Nonnull;
import java.util.List;

class RainForecast {

    @Nonnull
    private final String startTime;
    @Nonnull
    private final String endTime;
    @Nonnull
    private final List<Double> precipitationLevel;

    RainForecast(@Nonnull String startTime, @Nonnull String endTime, @Nonnull List<Double> precipitationLevel) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.precipitationLevel = List.copyOf(precipitationLevel);
    }

    @Nonnull
    public String getStartTime() {
        return startTime;
    }

    @Nonnull
    public String getEndTime() {
        return endTime;
    }

    @Nonnull
    public List<Double> getPrecipitationLevel() {
        return precipitationLevel;
    }

    @Override
    public String toString() {
        return "RainForecast{"
                + "startTime='" + startTime + '\''
                + ", endTime='" + endTime + '\''
                + ", precipitationLevel=" + precipitationLevel
                + '}';
    }

}
