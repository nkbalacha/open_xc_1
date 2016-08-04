package com.openxcplatform.openxcstarter;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_review);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        tLat = (ArrayList<Double>)getIntent().getSerializableExtra("latitude");
        tLong = (ArrayList<Double>)getIntent().getSerializableExtra("longitude");
        tRuleLat = (ArrayList<Double>)getIntent().getSerializableExtra("ruleLatitude");
        tRuleLong = (ArrayList<Double>)getIntent().getSerializableExtra("ruleLongitude");

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
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

        for(int i = 0; i < tRuleLat.size() - 1; i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(tRuleLat.get(i), tRuleLong.get(i))).title("broken rule"));
        }
    //    System.out.println("Latitudes: " + tRuleLat);
    //    System.out.println("Longitude: " + tRuleLong);
    }
}
