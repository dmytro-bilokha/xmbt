package com.dmytrobilokha.xmbt.bot.ns.dto;

import java.util.List;

public class StationsPayload {

    private List<StationInfo> payload;

    public List<StationInfo> getPayload() {
        return payload;
    }

    public void setPayload(List<StationInfo> payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "StationsPayload{"
                + "payload=" + payload
                + '}';
    }
}
