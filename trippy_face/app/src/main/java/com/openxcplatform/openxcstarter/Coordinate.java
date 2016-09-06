package com.openxcplatform.openxcstarter;

/*
helper class for MapReviewActivity, used to store data
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
