package com.dmytrobilokha.xmbt.bot.ns.dto;

import javax.annotation.CheckForNull;

public class TripLegDto {

    @CheckForNull
    private Integer idx;
    @CheckForNull
    private String name;
    @CheckForNull
    private String direction;
    @CheckForNull
    private TripStationDto origin;
    @CheckForNull
    private TripStationDto destination;

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public TripStationDto getOrigin() {
        return origin;
    }

    public void setOrigin(TripStationDto origin) {
        this.origin = origin;
    }

    public TripStationDto getDestination() {
        return destination;
    }

    public void setDestination(TripStationDto destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "TripLegDto{"
                + "idx=" + idx
                + ", name='" + name + '\''
                + ", direction='" + direction + '\''
                + ", origin=" + origin
                + ", destination=" + destination
                + '}';
    }

}
