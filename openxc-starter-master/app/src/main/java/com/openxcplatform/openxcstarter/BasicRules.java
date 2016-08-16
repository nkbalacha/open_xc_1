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
     * Enforces the maximum engine speed to be 4000 RPM.
     */
    public double ruleMaxEngSpd(double newSpd) {

        /*
        if rpm is over 4000, return 40 for place addition, otherwise return 0.
         */
        if (newSpd > 2200) {
// TODO-spencer: this file doesn't need to make calls to setPlace() anymore
            InTransitActivity.setEngBreakTime();
            //TODO-spencer: make sure the above statement still works as needed
            return newSpd;
        }
        return 0;
    }

    /**
     * Limits the maximum acceleration pedal value to 97.
     *
     * @param newAccel latest acceleration value polled from the vehicle
     * @return the acceleration value if the rule is broken, or zero otherwise.
     */
    public double ruleMaxAccel(double newAccel) {

//        if (InTransitActivity.getAccel() > 97) {
//            InTransitActivity.setPlace(30, 3, InTransitActivity.getAccel());
//            InTransitActivity.setAccelBreakTime();
//        }

        if (newAccel > 55) {
            InTransitActivity.setAccelBreakTime();
            return newAccel;
        }
        return 0;
    }

    /**
     * Limits the maximum vehicle speed value to 90 km/hr.
     *
     * @param newSpeed the latest vehicle speed (km/hr) polled from the vehicle.
     * @return the vehicle speed value that broke the rule, or zero otherwise.
     */
    public double ruleMaxVehSpd(double newSpeed) {

//        if (InTransitActivity.getVeh() > 90) {
//            InTransitActivity.setPlace(80);
//            InTransitActivity.setPlace(80, 2, InTransitActivity.getVeh());
//            InTransitActivity.setSpeedBreakTime();
//        }

        if (newSpeed > 40) {
            InTransitActivity.setSpeedBreakTime();
            System.out.println("Broke rule 1");
            return newSpeed;
        }

        //rule not broke, return zero.
        return 0;
    }

    /**
     * Enforces that the driver should not rotate the steering wheel by 90 degrees or more during
     * any period of 0.5 seconds. Violation severity is 50 place units.
     * <p/>
     * This is a rudimentary algorithm. The rule is called slightly less than 10 times a second
     * (on average 108 millisec between calls), so we will keep a queue of 10 values.
     * After each call, the oldest value is discarded, and the newly measured steering angle
     * value is added to the queue.
     * <p/>
     * Then the maximum and minimum are calculated, and we check if there is a difference of 90
     * degrees or greater.
     */
    public double ruleSteering(double newAngle) {
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
        } else if (steeringQ.size() == 10) {
            steeringQ.removeLast();
        }

        steeringQ.addFirst(newAngle);
        minAngle = newAngle;
        maxAngle = newAngle;

        for (double val : steeringQ) {
            if (val < minAngle) minAngle = val;
            if (val > maxAngle) maxAngle = val;
        }

        /*
        if angle difference is 90 or greater, clear the queue, and return the that angle difference.
         */
        if ((maxAngle - minAngle) >= 90) {
            steeringQ.clear();
            InTransitActivity.setAngleBreakTime();
            return (maxAngle - minAngle);
        }
        return 0;

    }

    /**
     * Enforces this situation: if the engine speed is above 3000 RPM, and the accelerator pedal
     * is pressed in at least 5%, and the steering wheel is rotated at least 60 deg from center
     * in either direction, the driver has committed a violation.
     *
     * @return the engine speed (double) in rpm at which this infraction occurred.
     */

    public double ruleSpeedSteering(double engVal, double accelVal, double steerVal) {
        if (engVal > 3000
                && (steerVal > 60 || steerVal < -60)
                && accelVal > 5) {
            //InTransitActivity.setAngleBreakTime();

            return engVal;
        }

        //rule not broken, return zero.
        return 0;
    }
}
