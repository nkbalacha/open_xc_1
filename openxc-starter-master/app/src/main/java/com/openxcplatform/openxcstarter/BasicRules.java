package com.openxcplatform.openxcstarter;

import android.app.Activity;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;

public class BasicRules extends Activity {

    /* declaring these variables here because I think it will save memory space to only have
    them declared once.
     */
    private ArrayDeque<Double> steeringQ;  //TODO-spencer: most efficient way to store this queue?
    private double minAngle;
    private double maxAngle;

    public BasicRules() {
        steeringQ = new ArrayDeque<>();
    }

    /**
     * Enforces the maximum engine speed as 4000 RPM.
     */
    public int ruleMaxEngSpd(double newSpd) {

        /*
        if rpm is over 4000, return 40 for place addition, otherwise return 0.
         */
        if (newSpd > 4000) return 40;
        return 0;
    }

    //TODO vehicle speed units are km/hr, right?

    /**
     * Enforce the maximum vehicle speed as 90 km/hr.
     */
//    public void ruleMaxVehSpd() {
//                if (InTransitActivity.getVeh() > 90) {
//                    InTransitActivity.setPlace(80);
//                }
//    }

    /**
     * Enforces that the driver should not rotate the steering wheel by 90 degrees or more during
     * any period of 0.5 seconds. Violation severity is 50 place units.
     *
     * This is a rudimentary algorithm. The rule is called slightly less than 10 times a second
     * (on average 108 millisec between calls), so we will keep a queue of 10 values.
     * After each call, the oldest value is discarded, and the newly measured steering angle
     * value is added to the queue.
     *
     * Then the maximum and minimum are calculated, and we check if there is a difference of 90
     * degrees or greater.
     */
    public int ruleSteering(double newAngle) {
        // the mod is used to just take the last 4 digits of the execution time, for convenience
//        System.out.println(System.currentTimeMillis() % 10000);

        /*
        Pseudo-code

        if size of queue > 10, return an error
        == 10, pop first out

        in all cases:
        add most recent value (parameter) to the queue
        calculate the max
        calculate the min
        calculate the difference (max- min)
        if > 90, then adjust 'place' accordingly.

         */

        if (steeringQ.size() > 10) {
            throw new IndexOutOfBoundsException("steering queue has exceeded 10 elements.");
        }
        else if (steeringQ.size() == 10){
            steeringQ.removeLast();
        }

        steeringQ.addFirst(newAngle);
        minAngle = newAngle;
        maxAngle = newAngle;

        for (double val : steeringQ){
            if (val < minAngle) minAngle = val;
            if (val > maxAngle) maxAngle = val;
        }

        /*
        if angle difference is 90 or greater, clear the queue, and return the severity value
         */
        if ((maxAngle - minAngle) >= 90){
            steeringQ.clear();
            return 50;
        }
        return 0;

    }

    /**
     * Limits the maximum acceleration pedal value to 97.
     */
//    public void ruleMaxAccel() {
//                if (InTransitActivity.getAccel() > 97) {
//                    InTransitActivity.setPlace(30);
//                }
//    }

    /**
     * Enforces this situation: if the engine speed is above 3000 RPM, and the accelerator pedal
     * is pressed in at least 5%, and the steering wheel is rotated at least 60 deg from center
     * in either direction, the driver has committed a violation.
     */
//    public void ruleSpeedSteering() {
//                if (InTransitActivity.getEng() > 3000
//                        && (InTransitActivity.getSWAngle() > 60 || InTransitActivity
//                        .getSWAngle() < -60)
//                        && InTransitActivity.getAccel() > 5) {
//                    InTransitActivity.setPlace(100);
//                }
//    }
}
