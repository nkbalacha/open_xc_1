package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.test.InstrumentationTestRunner;

import java.util.Timer;
import java.util.TimerTask;

public class BasicRules extends Activity {

    private double[] angleQueue;

    public BasicRules(){}

    //engSpeed rule
    public void ruleOne()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (InTransitActivity.getEng() > 900) {
                    InTransitActivity.setPlace(20);
                }
            }
        });
    }

    // vehSpeed rule
    public void ruleTwo()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (InTransitActivity.getVeh() > 90) {
                    InTransitActivity.setPlace(80);
                }
            }
        });
    }

    // SW angle rule
    public void ruleThree()
    {
        angleQueue = new double[4];
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask()
        {
        int i = 0;
            @Override
            public void run()
            {
                angleQueue[i] = InTransitActivity.getSWAngle();
                i = (i + 1) % 4;
                if (angleQueue.length == 4) {
                    if(limit(angleQueue) > 180) {
                        InTransitActivity.setPlace(40);
                    }
                }
            }
        }, 0, 125);
    }

    public double limit(double[] angles) {
        double max = 0;
        for (int m = 0; m < 3; m++) {
            for (int n = m + 1; n < 4; n++) {
                if(Math.abs(angles[m] - angles[n]) > max) {
                    max = Math.abs(angles[m] - angles[n]);
                }
            }
        }
        return max;
    }


}
