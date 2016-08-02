package com.openxcplatform.openxcstarter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

public class RulesFragment extends Fragment {

    private Switch ruleSwitch;
    private static boolean customRules = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ruleView = inflater.inflate(R.layout.fragment_rules, container, false);
        ruleSwitch = (Switch)ruleView.findViewById(R.id.switch_rules);

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

        return ruleView;
    }

    public static boolean getRulesChecked(){
        return customRules;
    }

}
