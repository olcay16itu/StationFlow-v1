package com.stationflow.backend.model;

public enum StationStatus {
    ACTIVE("active"),
    MAINTENANCE("maintenance"),
    EMPTY("empty"),
    FULL("full");

    private final String value;

    StationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
