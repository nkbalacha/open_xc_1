package com.openxcplatform.openxcstarter;

import android.app.Activity;

public class CustomRules extends Activity {

    public CustomRules() {}

    public void customMaxVehSpd(int maxSpd) {
        if (InTransitActivity.getVeh() > maxSpd) {
            InTransitActivity.setPlace(80, 1, InTransitActivity.getVeh());
        }
    }

    public void customMaxEngSpd(int maxEngSpd) {
        if (InTransitActivity.getEng() > maxEngSpd) {
            InTransitActivity.setPlace(40, 2, InTransitActivity.getEng());
        }
    }

    public void customMaxAccel(int maxAccel) {
        if (InTransitActivity.getAccel() > maxAccel) {
            InTransitActivity.setPlace(40, 3, InTransitActivity.getAccel());
        }
    }

}
