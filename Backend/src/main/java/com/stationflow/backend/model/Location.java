package com.stationflow.backend.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Location {
    @jakarta.validation.constraints.Min(value = -90, message = "Enlem -90 ile 90 arasında olmalıdır")
    @jakarta.validation.constraints.Max(value = 90, message = "Enlem -90 ile 90 arasında olmalıdır")
    private double lat;

    @jakarta.validation.constraints.Min(value = -180, message = "Boylam -180 ile 180 arasında olmalıdır")
    @jakarta.validation.constraints.Max(value = 180, message = "Boylam -180 ile 180 arasında olmalıdır")
    private double lng;

    public Location() {
    }

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
