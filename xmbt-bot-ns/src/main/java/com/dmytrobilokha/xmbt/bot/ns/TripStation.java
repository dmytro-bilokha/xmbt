package com.dmytrobilokha.xmbt.bot.ns;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

class TripStation {

    @Nonnull
    private final String name;
    @Nonnull
    private final String track;
    @Nonnull
    private final LocalDateTime dateTime;

    TripStation(@Nonnull String name, @Nonnull String track, @Nonnull LocalDateTime dateTime) {
        this.name = name;
        this.track = track;
        this.dateTime = dateTime;
    }

    @Nonnull
    String getName() {
        return name;
    }

    @Nonnull
    String getTrack() {
        return track;
    }

    @Nonnull
    LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "TripStation{"
                + "name='" + name + '\''
                + ", track='" + track + '\''
                + ", time=" + dateTime
                + '}';
    }

}
