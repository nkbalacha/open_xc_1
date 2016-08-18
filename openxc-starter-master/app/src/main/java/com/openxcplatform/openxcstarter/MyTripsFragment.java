package com.openxcplatform.openxcstarter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileInputStream;
import java.util.ArrayList;

public class MyTripsFragment extends Fragment {
    ArrayList<String> savedNames;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View tripsView = inflater.inflate(R.layout.fragment_my_trips, container, false);

        String newSave = (String) getActivity().getIntent().getSerializableExtra("savedTrip");
        savedNames.add(newSave);
        System.out.println(newSave.toString());
        return tripsView;
    }
}