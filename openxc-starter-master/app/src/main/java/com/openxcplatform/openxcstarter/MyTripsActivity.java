package com.openxcplatform.openxcstarter;

import android.app.ListActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

public class MyTripsActivity extends ListActivity {

    private CommentsDataSource dataSource;

    ArrayAdapter<Comment> adapter = (ArrayAdapter<Comment>) getListAdapter();
    Comment comment = null;
    String tripName;
    String tripData;
    int buttonBoolean;

    JSONObject json= new JSONObject();
    SharedPreferences pref;

    public ArrayList<Double> coordinates= new ArrayList<Double>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        dataSource = new CommentsDataSource(this);
        dataSource.open();

        List<Comment> values = dataSource.getAllComments();
        //System.out.println(getIntent().getSerializableExtra("textBox") != null);
        if (MapReviewActivity.getDataSent() == true) {
            buttonBoolean =(Integer)getIntent().getSerializableExtra("testBox");
            tripName =(String)getIntent().getSerializableExtra("tripName");
            tripData = (String)getIntent().getSerializableExtra("tripData");
            coordinates=(ArrayList<Double>)getIntent().getSerializableExtra("points");
        }

        pref = getApplicationContext().getSharedPreferences("database", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(tripName, tripData);
        editor.commit();

        ArrayAdapter<Comment> adapter = new ArrayAdapter<Comment>(this,
                R.layout.simplerow, values);
        setListAdapter(adapter);

        if(buttonBoolean >=1){
            comment = dataSource.createComment(tripName/*comments[nextInt]*/);
            adapter.add(comment);
            buttonBoolean++;
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
                comment = dataSource.createComment(Integer.toString(buttonBoolean));
                adapter.add(comment);
                buttonBoolean++;
                break;
            case R.id.deletetrip:
                if (getListAdapter().getCount() > 0) {
                    comment = (Comment) getListAdapter().getItem(0);
                    dataSource.deleteComment(comment);
                    adapter.remove(comment);
                }
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        dataSource.close();
        super.onPause();
    }
    public void back (View v){
        Intent n= new Intent(this, StartActivity.class);
        startActivity(n);
    }

    public void next(View v) throws JSONException {
        Button b = (Button)v;
        String buttonText = b.getText().toString();
        Intent p= new Intent(this, TripMapReview.class);

        String data = null;

        data= pref.getString(buttonText,null);
        p.putExtra("name", buttonText);
        p.putExtra("datasent",data);
        startActivity(p);
    }

}
