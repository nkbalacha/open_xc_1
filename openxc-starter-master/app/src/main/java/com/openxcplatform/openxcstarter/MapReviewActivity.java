package com.openxcplatform.openxcstarter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

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
        tLat = (ArrayList<Double>)getIntent().getSerializableExtra("latitude");
        tLong = (ArrayList<Double>)getIntent().getSerializableExtra("longitude");
        tRuleLat = (ArrayList<Double>)getIntent().getSerializableExtra("ruleLatitude");
        tRuleLong = (ArrayList<Double>)getIntent().getSerializableExtra("ruleLongitude");
        tErrorNames = (ArrayList<Integer>)getIntent().getSerializableExtra("errorNames");
        tErrorValues = (ArrayList<Double>)getIntent().getSerializableExtra("errorValues");
        tErrorColors = (ArrayList<Integer>)getIntent().getSerializableExtra("errorColors");

        // setting up the google map, enabling location services
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
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
            for(int i = 0; i < tLat.size() - 1; i++) {
                // algorithm for determining polyline color
                if (tErrorColors.get(i) < 128) {
                    polyColor = (Color.argb(255, tErrorColors.get(i) * 2, 255, 0));
                }
                if (tErrorColors.get(i) >= 128) {
                    polyColor = (Color.argb(255, 255, 256 - 2*(tErrorColors.get(i) - 127), 0));
                }
                // actually making the polyline
                mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(new LatLng(tLat.get(i), tLong.get(i)))
                        .add(new LatLng(tLat.get(i + 1), tLong.get(i + 1)))
                        .color(polyColor)
                );
            }
        }

        // cases for broken rules
        for(int i = 0; i < tRuleLat.size(); i++) {
            String ruleBroken;
            switch (tErrorNames.get(i)) {
                case InTransitActivity.MAX_ACCEL: ruleBroken = ruleAccel;
                        break;
                case InTransitActivity.MAX_ENG: ruleBroken = ruleEng;
                        break;
                case InTransitActivity.MAX_VEH: ruleBroken = ruleVeh;
                        break;
                case InTransitActivity.STEER: ruleBroken = ruleSteering;
                        break;
                default: ruleBroken = "You fucked up";
                        break;
            }

            // adding error markers to the polylines
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(tRuleLat.get(i), tRuleLong.get(i)))
                    .title(ruleBroken + ": " + tErrorValues.get(i).toString())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            }
        System.out.println("Latitudes: " + tRuleLat.toString());        // for debugging
        System.out.println("Longitude: " + tRuleLong.toString());
        System.out.println("Broken rule numbers: " + tErrorNames.toString());
        System.out.println("Broken rule values: " + tErrorValues.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MapReviewActivity.this, StartActivity.class));
        finish();
    }

    // script for the home button in the top left
    public void goToHome() {
        homeButton = (Button)findViewById(R.id.but_home);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changePage = new Intent(MapReviewActivity.this, StartActivity.class);

                startActivity(changePage);
            }
        });
    }
}
