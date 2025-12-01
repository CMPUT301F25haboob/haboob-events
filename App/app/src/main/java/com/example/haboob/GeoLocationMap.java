package com.example.haboob;

import android.location.Location;

import java.util.List;

/**
 * A lightweight container class for storing and managing a list of user
 * geographic locations represented by {@link android.location.Location}.
 * <p>
 * This class is intended to act as a simple data model for map-related features
 * within the application—for example, displaying entrant positions on a map
 * or performing distance-based calculations elsewhere in the system.
 * It does not perform any coordinate processing on its own; instead, it
 * allows other components to retrieve and update the location list as needed.
 * </p>
 */

public class GeoLocationMap {
    private List<Location> userLocations;

    /**
     * Creates an empty {@code GeoLocationMap} instance with no preset user locations.
     * <p>
     * Useful when locations will be added later through {@link #setUserLocations(List)}.
     * </p>
     */
    public GeoLocationMap() {
        // Empty constructor
    }

    /**
     * Creates an empty {@code GeoLocationMap} with no stored user locations.
     */
    public GeoLocationMap(List<Location> userLocations) {
        this.userLocations = userLocations;
    }

    /**
     * Returns the list of stored user locations.
     *
     * @return list of locations, or null if not set
     */
    public List<Location> getUserLocations() {
        return userLocations;
    }

    /**
     * Updates the stored list of user locations.
     *
     * @param userLocations new list of locations
     */
    public void setUserLocations(List<Location> userLocations) { this.userLocations = userLocations; }
}
