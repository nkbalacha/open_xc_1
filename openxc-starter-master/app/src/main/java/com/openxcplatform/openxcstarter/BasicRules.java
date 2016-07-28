package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.test.InstrumentationTestRunner;

import java.util.Timer;
import java.util.TimerTask;

public class BasicRules extends Activity {

    private double[] angleQueue;

    public BasicRules() {

		/*
         * What if we start a thread inside this constructor, and just have all of the
		 * rules run inside the same thread? And then we could create an instance of
		 * BasicRules inside the onCreate method of InTransit activity, which would
		 * start the thread.
		 */

    }

    /**
     * Enforces the maximum engine speed to be 4000 RPM.
     */
    public void ruleMaxEngSpd() {
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

    //TODO vehicle speed units are km/hr, right?

    /**
     * Enforce the maximum vehicle speed as 90 km/hr.
     */
    public void ruleMaxVehSpd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (InTransitActivity.getVeh() > 90) {
                    InTransitActivity.setPlace(80);
                }
            }
        });
    }

    /**
     * Enforces that the driver should not rotate the steering wheel by 90 degrees or more during
     * any period of 0.5 seconds.
     */
    public void ruleSteering() {
        angleQueue = new double[4];
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                angleQueue[i] = InTransitActivity.getSWAngle();
                i = (i + 1) % 4;
                if (angleQueue.length == 4) {
                    if (limit(angleQueue) > 90) {
                        InTransitActivity.setPlace(30);
                    }
                }
            }
        }, 0, 62);
    }

    /**
     * Helper for ruleSteering()
     *
     * @param angles a queue of steering angle values
     * @return  the maximum angle difference during this time period
     */
    public double limit(double[] angles) {
        double max = 0;
        for (int m = 0; m < 3; m++) {
            for (int n = m + 1; n < 4; n++) {
                if (Math.abs(angles[m] - angles[n]) > max) {
                    max = Math.abs(angles[m] - angles[n]);
                }
            }
        }
        return max;
    }

    /**
     * Limits the maximum acceleration pedal value to 97.
     */
    public void ruleMaxAccel() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (InTransitActivity.getAccel() > 97) {
                    InTransitActivity.setPlace(30);
                }
            }
        });
    }

    /**
     * Enforces this situation: if the engine speed is above 3000 RPM, and the accelerator pedal
     * is pressed in at least 5%, and the steering wheel is rotated at least 60 deg from center
     * in either direction, the driver has committed a violation.
     */
    public void ruleSpeedSteering() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (InTransitActivity.getEng() > 3000
                        && (InTransitActivity.getSWAngle() > 60 || InTransitActivity
                        .getSWAngle() < -60)
                        && InTransitActivity.getAccel() > 5) {
                    InTransitActivity.setPlace(100);
                }
            }
        });
    }
}
