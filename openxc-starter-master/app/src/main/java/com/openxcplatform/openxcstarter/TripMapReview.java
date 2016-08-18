package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TripMapReview extends Activity {

    public TextView bet;
    String tripname;
    public String dataretrieved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_map_review);

        dataretrieved= (String)getIntent().getSerializableExtra("datasent");

        tripname= (String)getIntent().getSerializableExtra("name");
        bet= (TextView)findViewById(R.id.tru);
        bet.setText(tripname);
        System.out.println(dataretrieved);
    }
}
