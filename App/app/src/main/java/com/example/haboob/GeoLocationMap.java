package com.example.haboob;

import android.location.Location;

import java.util.List;

public class GeoLocationMap {
    private List<Location> userLocations;

    public GeoLocationMap() {
        // Empty constructor
    }

    public GeoLocationMap(List<Location> userLocations) {
        this.userLocations = userLocations;
    }

    public List<Location> getUserLocations() {
        return userLocations;
    }

    public void setUserLocations(List<Location> userLocations) { this.userLocations = userLocations; }
}
