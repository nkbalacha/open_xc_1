package com.openxcplatform.openxcstarter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class RulesFragment extends Fragment {

    private Switch ruleSwitch;
    private static boolean customRules = false;
    private Button doneButton;
    private static int vSMax = 0;
    private static int accelMax = 0;
    private static int engMax = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //first inflate the fragment view
        View ruleView = inflater.inflate(R.layout.fragment_rules, container, false);

        //set on/off switch and done button
        ruleSwitch = (Switch)ruleView.findViewById(R.id.switch_rules);
        doneButton = (Button)ruleView.findViewById(R.id.but_done);

        //initial check for the top-right switch
        ruleSwitch.setChecked(false);
        ruleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    customRules = true;
                } else {
                    customRules = false;
                }
            }
        });

        //check for the button
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        return ruleView;
    }

    //helper method for the Done button
    public void save() {
        EditText temp;
        if (getView().findViewById(R.id.vSpeedInput) != null) {
            temp = (EditText)getView().findViewById(R.id.vSpeedInput);
            vSMax = Integer.parseInt(temp.getText().toString());
        }
        if (getView().findViewById(R.id.accelInput) != null) {
            temp = (EditText)getView().findViewById(R.id.accelInput);
            if ((Integer.parseInt(temp.getText().toString()) > 0
                    && Integer.parseInt(temp.getText().toString()) <= 100)) {
                accelMax = Integer.parseInt(temp.getText().toString());
            }
        }
        if (getView().findViewById(R.id.engInput) != null) {
            temp = (EditText)getView().findViewById(R.id.engInput);
            engMax = Integer.parseInt(temp.getText().toString());
        }
    }

    // getters
    public static int getvSMax() { return vSMax;}

    public static int getEngMax() { return engMax;}

    public static int getAccelMax() { return accelMax;}

    public static boolean getRulesChecked(){
        return customRules;
    }
}
