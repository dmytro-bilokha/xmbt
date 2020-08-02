package com.dmytrobilokha.xmbt.bot.weather;

import javax.annotation.Nonnull;

class City {

    @Nonnull
    private final String name;
    @Nonnull
    private final String lat;
    @Nonnull
    private final String lon;

    City(@Nonnull String name, @Nonnull String lat, @Nonnull String lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getLat() {
        return lat;
    }

    @Nonnull
    public String getLon() {
        return lon;
    }

    @Override
    public String toString() {
        return "City{"
                + "name='" + name + '\''
                + ", lat='" + lat + '\''
                + ", lng='" + lon + '\''
                + '}';
    }

}
