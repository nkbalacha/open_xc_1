package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.util.ArrayList;

public class TripMapReview extends Activity {

    public TextView testBox;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_map_review);

        dataRetrieved= (String)getIntent().getSerializableExtra("datasent");

        tripName= (String)getIntent().getSerializableExtra("name");
        testBox = (TextView)findViewById(R.id.output_box);
        testBox.setText(dataRetrieved);
        System.out.println(dataRetrieved);
        parseData(dataRetrieved);
        System.out.println(tLat.toString() + "\n" + tLong.toString() + "\n" + tRuleLat.toString()
                + "\n" + tRuleLong.toString() + "\n" + tErrorNames.toString() + "\n"
                + tErrorValues.toString() + "\n" + tErrorColors.toString());
    }

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
            System.out.println("CURRENT DATA RETRIEVED: " + dataRetrieved);
            dataRetrieved = dataRetrieved.substring(right + 1);
            System.out.println("CURRENT DATA RETRIEVED: " + dataRetrieved);
        }
    }
}
