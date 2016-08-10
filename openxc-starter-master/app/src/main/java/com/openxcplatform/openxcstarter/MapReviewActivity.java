package com.openxcplatform.openxcstarter;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapReviewActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Double> tLat;
    private ArrayList<Double> tLong;
    private ArrayList<Double> tRuleLat;
    private ArrayList<Double> tRuleLong;
    private ArrayList<Integer> tErrorNames;
    private ArrayList<Double> tErrorValues;
    private Button homeButton;

    // rule strings
    private final String ruleOne = "Exceeded max vehicle speed";
    private final String ruleTwo = "Exceeded max engine speed";
    private final String ruleThree = "Accelerated too quickly";
    private final String ruleFour = "Turned too quickly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_review);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        goToHome();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // data transfer from InTransitActivity to this activity
        tLat = (ArrayList<Double>)getIntent().getSerializableExtra("latitude");
        tLong = (ArrayList<Double>)getIntent().getSerializableExtra("longitude");
        tRuleLat = (ArrayList<Double>)getIntent().getSerializableExtra("ruleLatitude");
        tRuleLong = (ArrayList<Double>)getIntent().getSerializableExtra("ruleLongitude");
        tErrorNames = (ArrayList<Integer>)getIntent().getSerializableExtra("errorNames");
        tErrorValues = (ArrayList<Double>)getIntent().getSerializableExtra("errorValues");

        // setting up the google map
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        // adding polylines if there are coordinates available
        if (tLat.size() == 0 || tLong.size() == 0) {
            // do nothing
        } else {
            mMap.addMarker(new MarkerOptions().position(new LatLng(tLat.get(tLat.size() - 1),
                    tLong.get(tLong.size() - 1))).title("End of trip"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(tLat.get(tLat.size() - 1),
                    tLong.get(tLong.size() - 1)), 16));

            for(int i = 0; i < tLat.size() - 1; i++) {
                mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(new LatLng(tLat.get(i), tLong.get(i)))
                        .add(new LatLng(tLat.get(i + 1), tLong.get(i + 1)))
                );
            }
        }

        // cases for broken rules
        for(int i = 0; i < tRuleLat.size() - 1; i++) {
            String ruleBroken;
            switch (tErrorNames.get(i)) {
                case 1: ruleBroken = ruleOne;
                        break;
                case 2: ruleBroken = ruleTwo;
                        break;
                case 3: ruleBroken = ruleThree;
                        break;
                case 4: ruleBroken = ruleFour;
                        break;
                default: ruleBroken = "You fucked up";
                        break;
            }

            // adding error markers to the polylines
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(tRuleLat.get(i), tRuleLong.get(i))).title(ruleBroken +
                            ": " + tErrorValues.get(i).toString()));
        }
        System.out.println("Latitudes: " + tRuleLat.toString());
        System.out.println("Longitude: " + tRuleLong.toString());
        System.out.println("Broken rule numbers: " + tErrorNames.toString());
        System.out.println("Broken rule values: " + tErrorValues.toString());
    }

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
