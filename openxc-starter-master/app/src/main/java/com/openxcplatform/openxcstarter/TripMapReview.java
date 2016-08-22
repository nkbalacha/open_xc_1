package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

public class TripMapReview extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // parsed data from storage
    public String tripName;
    public String dataRetrieved;

    private ArrayList<Double> tLat = new ArrayList<Double>();
    private ArrayList<Double> tLong = new ArrayList<Double>();
    private int polyColor;

    // lat/long of error places
    private ArrayList<Double> tRuleLat = new ArrayList<Double>();
    private ArrayList<Double> tRuleLong = new ArrayList<Double>();

    // error information
    private ArrayList<Integer> tErrorNames = new ArrayList<Integer>();
    private ArrayList<Double> tErrorValues = new ArrayList<Double>();
    private ArrayList<Integer> tErrorColors = new ArrayList<Integer>();

    // outputs for mistakes made in the error markers
    private final String ruleVeh = "Exceeded max vehicle speed";
    private final String ruleEng = "Exceeded max engine speed";
    private final String ruleAccel = "Accelerated too quickly";
    private final String ruleSteering = "Turned too quickly";
    private final String ruleSpeedSteer = "Started drifting";

    // when created, checks for data sent
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_map_review);

        dataRetrieved= (String)getIntent().getSerializableExtra("datasent");
        tripName= (String)getIntent().getSerializableExtra("name");

        // parses data and puts into the ArrayLists
        parseData(dataRetrieved);

        // creates and syncs map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.MyTripMap);
        mapFragment.getMapAsync(this);
    }

    // when parseData is finished, takes the arrayLists of values and redraws the map
    public void onMapReady(GoogleMap googleMap) {
        // setting up the google map, enabling location services
        mMap = googleMap;
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        // adding polylines if there are coordinates available
        if (tLat.size() == 0 || tLong.size() == 0) {
            // do nothing
        } else {
            // adds an azure marker for the start and end of the trip
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(tLat.get(0), tLong.get(0)))
                    .title("Start of trip")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(tLat.get(tLat.size() - 1), tLong.get(tLong.size() - 1)))
                    .title("End of trip")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            // moves camera to center on the end of the trip
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(tLat.get(tLat.size() - 1),
                    tLong.get(tLong.size() - 1)), 16));

            // makes polylines using latitude/longitude/color
            for (int i = 0; i < tLat.size() - 1; i++) {
                // algorithm for determining polyline color
                if (tErrorColors.get(i) < 128) {
                    polyColor = (Color.argb(255, tErrorColors.get(i) * 2, 255, 0));
                }
                if (tErrorColors.get(i) >= 128) {
                    polyColor = (Color.argb(255, 255, 256 - 2 * (tErrorColors.get(i) - 127), 0));
                }
                // actually making the polyline
                mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(new LatLng(tLat.get(i), tLong.get(i)))
                        .add(new LatLng(tLat.get(i + 1), tLong.get(i + 1)))
                        .color(polyColor)
                );
            }
        }

        // reorganizes the data into another structure that removes duplicate coordinates
        HashMap<Coordinate, ErrorInfo> errorData = new HashMap<Coordinate, ErrorInfo>();
        for (int i = 0; i < tRuleLat.size(); i++) {
            Coordinate newCoord = new Coordinate(tRuleLat.get(i), tRuleLong.get(i));
            ErrorInfo newError = new ErrorInfo(tErrorNames.get(i), tErrorValues.get(i));
            if (!errorData.containsKey(newCoord)) {
                errorData.put(newCoord, newError);
            } else {
                errorData.get(newCoord).add(tErrorNames.get(i), tErrorValues.get(i));
            }
        }

        // sets error markers based on rule #
        for (Coordinate current : errorData.keySet()) {
            String ruleBroken = "";
            for(int i = 0; i < errorData.get(current).errorNumber.size(); i++) {
                switch (errorData.get(current).errorNumber.get(i)) {
                    case InTransitActivity.MAX_ACCEL:
                        ruleBroken = ruleBroken + ruleAccel + ": " + errorData.get(current).errorValue.get(i);
                        break;
                    case InTransitActivity.MAX_ENG:
                        ruleBroken = ruleBroken + ruleEng + ": " + errorData.get(current).errorValue.get(i);
                        break;
                    case InTransitActivity.MAX_VEH:
                        ruleBroken = ruleBroken + ruleVeh + ": " + errorData.get(current).errorValue.get(i);
                        break;
                    case InTransitActivity.STEER:
                        ruleBroken = ruleBroken + ruleSteering + ": " + errorData.get(current).errorValue.get(i);
                        break;
                    case InTransitActivity.SPEED_STEER:
                        ruleBroken = ruleBroken + ruleSpeedSteer + ": " + errorData.get(current).errorValue.get(i);
                        break;
                    default:
                        ruleBroken = ruleBroken + "Unidentified error";
                        break;
                }
                ruleBroken = ruleBroken + "\n";
            }

            // adds markers
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(current.lat , current.lng))
                    .title(ruleBroken)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        }
    }

    // when back is pressed, goes to the home page
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TripMapReview.this, StartActivity.class));
        finish();
    }

    // script that parses the data and puts it into the arrayLists
    public void parseData(String dataRetrieved) {
        String currLine = "";

        for (int lineNum = 1; lineNum <= 7; lineNum++) {
            // made the single line into a substring
            int left = dataRetrieved.indexOf("<");
            int right = dataRetrieved.indexOf(">");
            currLine = dataRetrieved.substring(left + 1, right);

            while (currLine.indexOf("(") != -1) {
                int l = currLine.indexOf("(");
                int r = currLine.indexOf(")");
                Double parseDouble = Double.parseDouble(currLine.substring(l + 1, r));

                switch(lineNum) {
                    case 1:
                        tLat.add(parseDouble);
                        break;
                    case 2:
                        tLong.add(parseDouble);
                        break;
                    case 3:
                        tRuleLat.add(parseDouble);
                        break;
                    case 4:
                        tRuleLong.add(parseDouble);
                        break;
                    case 5:
                        tErrorValues.add(parseDouble);
                        break;
                    case 6:
                        tErrorNames.add(Integer.parseInt(currLine.substring(l + 1, r)));
                        break;
                    case 7:
                        tErrorColors.add(Integer.parseInt(currLine.substring(l + 1, r)));
                        break;
                }
                currLine = currLine.substring(r + 1);
            }
            dataRetrieved = dataRetrieved.substring(right + 1);
        }
    }
}
