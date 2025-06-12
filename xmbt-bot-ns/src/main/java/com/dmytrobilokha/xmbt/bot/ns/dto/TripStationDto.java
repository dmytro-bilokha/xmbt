package com.dmytrobilokha.xmbt.bot.ns.dto;

import jakarta.json.bind.annotation.JsonbDateFormat;
import java.time.ZonedDateTime;

public class TripStationDto {

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ssx")
    private ZonedDateTime plannedDateTime;
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ssx")
    private ZonedDateTime actualDateTime;
    private String plannedTrack;
    private String actualTrack;
    private String name;
    private Long uicCode;

    public ZonedDateTime getPlannedDateTime() {
        return plannedDateTime;
    }

    public void setPlannedDateTime(ZonedDateTime plannedDateTime) {
        this.plannedDateTime = plannedDateTime;
    }

    public ZonedDateTime getActualDateTime() {
        return actualDateTime;
    }

    public void setActualDateTime(ZonedDateTime actualDateTime) {
        this.actualDateTime = actualDateTime;
    }

    public String getPlannedTrack() {
        return plannedTrack;
    }

    public void setPlannedTrack(String plannedTrack) {
        this.plannedTrack = plannedTrack;
    }

    public String getActualTrack() {
        return actualTrack;
    }

    public void setActualTrack(String actualTrack) {
        this.actualTrack = actualTrack;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUicCode() {
        return uicCode;
    }

    public void setUicCode(Long uicCode) {
        this.uicCode = uicCode;
    }

    @Override
    public String toString() {
        return "TripStationDto{"
                + "plannedDateTime=" + plannedDateTime
                + ", actualDateTime=" + actualDateTime
                + ", plannedTrack='" + plannedTrack + '\''
                + ", actualTrack='" + actualTrack + '\''
                + ", name='" + name + '\''
                + ", uicCode=" + uicCode
                + '}';
    }
}
