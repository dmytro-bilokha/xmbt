package com.dmytrobilokha.xmbt.bot.ns.dto;

import java.util.List;

public class TripInfoDto {

    private boolean optimal;
    private Integer transfers;
    private List<TripLegDto> legs;

    public boolean isOptimal() {
        return optimal;
    }

    public void setOptimal(boolean optimal) {
        this.optimal = optimal;
    }

    public Integer getTransfers() {
        return transfers;
    }

    public void setTransfers(Integer transfers) {
        this.transfers = transfers;
    }

    public List<TripLegDto> getLegs() {
        return legs;
    }

    public void setLegs(List<TripLegDto> legs) {
        this.legs = legs;
    }

    @Override
    public String toString() {
        return "TripInfoDto{"
                + "optimal=" + optimal
                + ", transfers=" + transfers
                + ", legs=" + legs
                + '}';
    }

}
