package com.stationflow.backend.model;

public enum TransportType {
    BUS("bus"),
    METRO("metro"),
    BIKE("bike"),
    SCOOTER("scooter"),
    MINIBUS("minibus"),
    TAXI("taxi"),
    DOLMUS("dolmus");

    private final String value;

    TransportType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
