package com.dmytrobilokha.xmbt.bot.ns.dto;

import java.util.List;

public class StationsPayloadDto {

    private List<StationInfoDto> payload;

    public List<StationInfoDto> getPayload() {
        return payload;
    }

    public void setPayload(List<StationInfoDto> payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "StationsPayloadDto{"
                + "payload=" + payload
                + '}';
    }
}
