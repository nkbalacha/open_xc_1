package com.openxcplatform.openxcstarter;

import android.app.Activity;

public class CustomRules extends Activity {

    public CustomRules() {}

    public double customMaxVehSpd(double newSpeed, int maxSpd) {
        if (newSpeed > maxSpd) {
            InTransitActivity.setSpeedBreakTime();
            return newSpeed;
        }
        return 0;
    }

    public double customMaxEngSpd(double newSpeed, int maxEngSpd) {
        if (newSpeed > maxEngSpd) {
            InTransitActivity.setEngBreakTime();
            return newSpeed;
        }
        return 0;
    }

    public double customMaxAccel(double newSpeed, int maxAccel) {
        if (newSpeed > maxAccel) {
            InTransitActivity.setEngBreakTime();
            return newSpeed;
        }
        return 0;
    }

}
