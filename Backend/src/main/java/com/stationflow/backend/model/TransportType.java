package com.stationflow.backend.model;

public enum TransportType {
    BUS("bus"),
    METRO("metro"),
    BIKE("bike"),
    SCOOTER("scooter");

    private final String value;

    TransportType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
