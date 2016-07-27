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
                if (InTransitActivity.getEng() > 4000) {
                    InTransitActivity.setPlace(40);
                } else {
                    System.out.println("running ruleOne");
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
                    if(limit(angleQueue) > 90) {
                        InTransitActivity.setPlace(30);
                    }
                }
            }
        }, 0, 62);
    }

    // helper for rule three
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

    // only acceleration
    public void ruleFour() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (InTransitActivity.getAccel() > 97) {
                    InTransitActivity.setPlace(30);
                }
            }
        });
    }

    // engine speed/steering angle/throttle more complex stuff
    public void ruleFive() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (InTransitActivity.getEng() > 3000 && (InTransitActivity.getSWAngle() > 60 ||
                        InTransitActivity.getSWAngle() < -60) && InTransitActivity.getAccel() > 5) {
                    InTransitActivity.setPlace(100);
                }
            }
        });
    }
}
