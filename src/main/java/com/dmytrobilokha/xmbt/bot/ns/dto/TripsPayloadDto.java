package com.dmytrobilokha.xmbt.bot.ns.dto;

import java.util.List;

public class TripsPayloadDto {

    private List<TripInfoDto> trips;

    public List<TripInfoDto> getTrips() {
        return trips;
    }

    public void setTrips(List<TripInfoDto> trips) {
        this.trips = trips;
    }

}
