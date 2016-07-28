package com.example.jeffrey.testproject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity{

    ListView listView;
    ArrayAdapter<String> listAdapter;
    String fragmentArray[] = {"FRAGMENT 1", "FRAGMENT 2"};
    DrawerLayout myDL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDL = (DrawerLayout)findViewById(R.id.drawerLayout);

        listView = (ListView) findViewById(R.id.listview);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, fragmentArray);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment;
                switch (position) {
                    case 0:
                        fragment = new FragmentOne();
                        break;
                    case 1:
                        fragment = new FragmentTwo();
                        break;
                    default:
                        fragment = new FragmentOne();
                        break;
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.relativeLayout, fragment).commit();
                myDL.closeDrawers();
            }
        });

    }
}
