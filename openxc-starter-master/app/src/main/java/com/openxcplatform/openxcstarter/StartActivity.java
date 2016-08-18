package com.openxcplatform.openxcstarter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class StartActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> listAdapter;
    String fragmentArray[] = {"Profile", "Custom Rules", "My Trips", "Stats"};
    DrawerLayout myDL;

    private static final String TAG = "WE NEED A NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        myDL = (DrawerLayout)findViewById(R.id.drawer_layout);

        listView = (ListView)findViewById(R.id.listview);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1,
                fragmentArray);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Fragment fragment;
                switch (position) {
                    case 0:
                        fragment = new ProfileFragment();
                        break;
                    case 1:
                        fragment = new RulesFragment();
                        break;
                    case 2:
                        fragment = new RulesFragment();
                        Intent changePage = new Intent(StartActivity.this, MyTripsActivity.class);

                        startActivity(changePage);
                        break;
                    case 3:
                        fragment = new StatsFragment();
                        break;
                    default:
                        fragment = new ProfileFragment();
                        break;
                }
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.relativeLayout, fragment).commit();
                myDL.closeDrawers();
            }
        });

        goToTrip();
    }

    public Button SmartTripButton;
    public void goToTrip() {
        SmartTripButton = (Button)findViewById(R.id.but_smartTrip);

        SmartTripButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent changePage = new Intent(StartActivity.this, InTransitActivity.class);

                startActivity(changePage);
            }
        });
    }
}
