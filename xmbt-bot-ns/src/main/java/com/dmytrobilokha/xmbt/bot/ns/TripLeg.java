package com.dmytrobilokha.xmbt.bot.ns;

import javax.annotation.Nonnull;

class TripLeg {

    @Nonnull
    private final TripStation origin;
    @Nonnull
    private final TripStation destination;

    TripLeg(@Nonnull TripStation origin, @Nonnull TripStation destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Nonnull
    TripStation getOrigin() {
        return origin;
    }

    @Nonnull
    TripStation getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "TripLeg{"
                + "origin=" + origin
                + ", destination=" + destination
                + '}';
    }

}
