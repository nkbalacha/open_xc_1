package com.openxcplatform.openxcstarter;

import android.app.Activity;

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
                if (InTransitActivity.getEng() > 4000) {
                    InTransitActivity.setPlace(40);
                }
                System.out.println("running basic rules");
    }

    /**
     * Enforce the maximum vehicle speed as 90 km/hr.
     */
    public void ruleMaxVehSpd() {
                if (InTransitActivity.getVeh() > 90) {
                    InTransitActivity.setPlace(80);
                }
    }

    /**
     * Enforces that the driver should not rotate the steering wheel by 90 degrees or more during
     * any period of 0.5 seconds.
     *
     * This is a rudimentary algorithm. The rule is called slightly less than 10 times a second
     * (on average 108 millisec between calls), so we will keep a queue of 10 values.
     * After each call, the oldest value is discarded, and the newly measured steering angle
     * value is added to the queue.
     *
     * Then the maximum and minimum are calculated, and we check if there is a difference of 90
     * degrees or greater.
     */
    public void ruleSteering() {
        // the mod is used to just take the last 4 digits of the execution time, for convenience
//        System.out.println(System.currentTimeMillis() % 10000);


    }

    /**
     * Limits the maximum acceleration pedal value to 97.
     */
    public void ruleMaxAccel() {
                if (InTransitActivity.getAccel() > 97) {
                    InTransitActivity.setPlace(30);
                }
    }

    /**
     * Enforces this situation: if the engine speed is above 3000 RPM, and the accelerator pedal
     * is pressed in at least 5%, and the steering wheel is rotated at least 60 deg from center
     * in either direction, the driver has committed a violation.
     */
    public void ruleSpeedSteering() {
                if (InTransitActivity.getEng() > 3000
                        && (InTransitActivity.getSWAngle() > 60 || InTransitActivity
                        .getSWAngle() < -60)
                        && InTransitActivity.getAccel() > 5) {
                    InTransitActivity.setPlace(100);
                }
    }
}
