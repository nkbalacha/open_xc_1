package com.openxcplatform.openxcstarter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MapReviewActivity extends FragmentActivity implements OnMapReadyCallback {

    // map initialization
    private GoogleMap mMap;

    // latitude and longitude to make polylines, with their color
    private ArrayList<Double> tLat;
    private ArrayList<Double> tLong;
    private int polyColor;

    // lat/long of error places
    private ArrayList<Double> tRuleLat;
    private ArrayList<Double> tRuleLong;

    // error information
    private ArrayList<Integer> tErrorNames;
    private ArrayList<Double> tErrorValues;
    private ArrayList<Integer> tErrorColors;

    // button in top left that takes you back to home screen
    private Button homeButton;

    // outputs for mistakes made in the error markers
    private final String ruleVeh = "Exceeded max vehicle speed";
    private final String ruleEng = "Exceeded max engine speed";
    private final String ruleAccel = "Accelerated too quickly";
    private final String ruleSteering = "Turned too quickly";
    private final String ruleSpeedSteer = "Started drifting";

    // button to save trip
    private Button saveButton;

    // variables for storing the saved data
    String tripInput = "<";
    String saveName = "";
    private static boolean dataSent = false;

    // on activity creation, gets and sets the view to a google map, then starts the home button script
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_review);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        goToHome();
    }

    // when the map is initialized, get all the relevant data for polylines/error markers from
    // InTransitActivity
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // data transfer from InTransitActivity to this activity
        tLat = (ArrayList<Double>) getIntent().getSerializableExtra("latitude");
        tLong = (ArrayList<Double>) getIntent().getSerializableExtra("longitude");
        tRuleLat = (ArrayList<Double>) getIntent().getSerializableExtra("ruleLatitude");
        tRuleLong = (ArrayList<Double>) getIntent().getSerializableExtra("ruleLongitude");
        tErrorNames = (ArrayList<Integer>) getIntent().getSerializableExtra("errorNames");
        tErrorValues = (ArrayList<Double>) getIntent().getSerializableExtra("errorValues");
        tErrorColors = (ArrayList<Integer>) getIntent().getSerializableExtra("errorColors");

        // setting up the google map, enabling location services
        mMap = googleMap;
        LocationManager locationManager = (LocationManager)     // I don't think these lines are necessary
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

        // restructures error data
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

        // plots every broken rule based on the rule #
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

            // adds marker of broken rule
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(current.lat , current.lng))
                    .title(ruleBroken)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        }

        saveTrip();

    /*    System.out.println("Latitudes: " + tRuleLat.toString());        // for debugging
        System.out.println("Longitude: " + tRuleLong.toString());
        System.out.println("Broken rule numbers: " + tErrorNames.toString());
        System.out.println("Broken rule values: " + tErrorValues.toString());*/
    }

    // clicking the back button returns to home page
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MapReviewActivity.this, StartActivity.class));
        finish();
    }

    // script for the home button in the top left
    public void goToHome() {
        homeButton = (Button) findViewById(R.id.but_home);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changePage = new Intent(MapReviewActivity.this, StartActivity.class);

                startActivity(changePage);
            }
        });
    }

    // saves data of trip and sends you to the MyTrips page
    public void saveTrip() {
        saveButton = (Button) findViewById(R.id.but_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // turns all the arrays and stores as one variable for parsing later
                for (Double i : tLat) {
                    tripInput = tripInput + "(" + i.toString() + ")";
                }
                tripInput = tripInput + ">\n<";

                for (Double i : tLong) {
                    tripInput = tripInput + "(" + i.toString() + ")";
                }
                tripInput = tripInput + ">\n<";

                for (Double i : tRuleLat) {
                    tripInput = tripInput + "(" + i.toString() + ")";
                }
                tripInput = tripInput + ">\n<";

                for (Double i : tRuleLong) {
                    tripInput = tripInput + "(" + i.toString() + ")";
                }
                tripInput = tripInput + ">\n<";

                for (Double i : tErrorValues) {
                    tripInput = tripInput + "(" + i.toString() + ")";
                }
                tripInput = tripInput + ">\n<";

                for (int i : tErrorNames) {
                    tripInput = tripInput + "(" + i + ")";
                }
                tripInput = tripInput + ">\n<";

                for (int i : tErrorColors) {
                    tripInput = tripInput + "(" + i + ")";
                }
                tripInput = tripInput + ">";

                // uses the input text to name the data being stored
                EditText saveNameInput = (EditText) findViewById(R.id.saveName);
                saveName = saveNameInput.getText().toString();

                //System.out.println(tripInput);

                // sends the data to MyTrips page
                Intent transferMapData = new Intent(MapReviewActivity.this, MyTripsActivity.class);
                transferMapData.putExtra("tripData", tripInput);
                transferMapData.putExtra("tripName", saveName);
                transferMapData.putExtra("testBox", 1);     //I don't think we need this

                // reset for the next save
                tripInput = "<";
                saveName = "";
                dataSent = true;
                startActivity(transferMapData);
            }
        });
    }

    // getters and setters
    public static boolean getDataSent() { return dataSent;}

    public static void setDataSent(boolean dataStatus) { dataSent = dataStatus;}

}
