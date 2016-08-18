package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyTripsActivity extends ListActivity {

    private static final String PREFS_NAME = "MyAPP_Settings";
    MySQLiteHelper db;
    private CommentsDataSource datasource;

    ArrayAdapter<Comment> adapter = (ArrayAdapter<Comment>) getListAdapter();
    Comment comment = null;
    Comment comment2 = null;
    int click=0;
    String trip_name;
    String trip_data;
    int j;


    JSONObject json= new JSONObject();
    SharedPreferences pref;

    public ArrayList<Double> coordinates= new ArrayList<Double>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        datasource = new CommentsDataSource(this);
        datasource.open();

        List<Comment> values = datasource.getAllComments();

        j=(Integer)getIntent().getSerializableExtra("bet");
        trip_name=(String)getIntent().getSerializableExtra("tripName");
        trip_data = (String)getIntent().getSerializableExtra("tripData");
        coordinates=(ArrayList<Double>)getIntent().getSerializableExtra("points");

        //System.out.println("***************************************************************** " +
          //      "Print 2 \n" + trip_data);


        pref = getApplicationContext().getSharedPreferences("database", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        //System.out.println("Shared preference:" + trip_name +  "\nData:" + trip_data);
        editor.putString(trip_name,trip_data);
        editor.commit();
        //System.out.println("Mapping trip_name to trip_data: " + pref.getString(trip_name, null));


        ArrayAdapter<Comment> adapter = new ArrayAdapter<Comment>(this,
                R.layout.simplerow, values);
        setListAdapter(adapter);

        if(j>=1){
            comment = datasource.createComment(trip_name/*comments[nextInt]*/);
            adapter.add(comment);
            j++;
        }

    }

    // Will be called via the onClick attribute
    // of the buttons in main.xml
    public void onClick(View view) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<Comment> adapter = (ArrayAdapter<Comment>) getListAdapter();
        Comment comment = null;

        switch (view.getId()) {
            case R.id.addtrip:
                // String[] comments = new String[] { "Cool", "Very nice", "Hate it" };
                //int nextInt = new Random().nextInt(3);
                // save the new comment to the database
                comment = datasource.createComment(Integer.toString(j)/*comments[nextInt]*/);
                adapter.add(comment);
                j++;
                break;
            case R.id.deletetrip:
                if (getListAdapter().getCount() > 0) {
                    comment = (Comment) getListAdapter().getItem(0);
                    datasource.deleteComment(comment);
                    adapter.remove(comment);
                }
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }
    public void back (View v){
        Intent n= new Intent(this, StartActivity.class);
        startActivity(n);
    }

    public void next(View v) throws JSONException {
        Button b = (Button)v;
        String buttonText = b.getText().toString();
        //System.out.println("ButtonText should = tripName :::" + buttonText);
        Intent p= new Intent(this, TripMapReview.class);

       // System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " +
        //        "button is clicked: \n " + pref.getString(buttonText, null));

        String data = null;
        String datatobesent=null;

        data= pref.getString(buttonText,null);
        p.putExtra("name", buttonText);
        p.putExtra("datasent",data);
        //System.out.println(data);
        startActivity(p);
    }

}
