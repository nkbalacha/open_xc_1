package com.openxcplatform.openxcstarter;

/**
 * Created by Jeffrey on 8/16/2016.
 */
public class Coordinate {
    public Double lat;
    public Double lng;

    public Coordinate(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public boolean equals(Coordinate coordinate) {
        if (lat.equals(coordinate.lat) && lng.equals(coordinate.lng)) {
            return true;
        } else {
            return false;
        }
    }
}
