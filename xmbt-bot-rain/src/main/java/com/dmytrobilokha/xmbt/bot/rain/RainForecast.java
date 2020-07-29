package com.dmytrobilokha.xmbt.bot.rain;

import javax.annotation.Nonnull;
import java.util.List;

class RainForecast {

    @Nonnull
    private final String startTime;
    @Nonnull
    private final String endTime;
    @Nonnull
    private final List<Integer> precipitationLevel;

    RainForecast(@Nonnull String startTime, @Nonnull String endTime, @Nonnull List<Integer> precipitationLevel) {
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
    public List<Integer> getPrecipitationLevel() {
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
