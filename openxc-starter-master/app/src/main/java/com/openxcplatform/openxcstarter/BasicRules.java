package com.openxcplatform.openxcstarter;

import android.app.Activity;

import java.util.ArrayDeque;

/**
 * Contains the methods that assert our rule-set.
 * Currently, safe driving is evaluated on the basis of:
 * - staying under a certain engine speed (rpm)
 * - staying under a certain acceleration intensity (percent that pedal is pressed)
 * - staying under a certain vehicle speed (km/hr)
 * - not steering too quickly (change in steering wheel angle per second)
 * - staying under a certain steering wheel angle magnitude when driving at a significant vehicle
 * speed
 */
public class BasicRules extends Activity {

    /**
     * An ArrayDeque that stores up to 10 steering wheel angle values (double). These values are
     * used to calculate the greatest range in the steering wheel angle during any 1-second
     * interval.
     */
    private ArrayDeque<Double> steeringQ;

    /**
     * The minimum angle during any 1-second period experienced by the steering wheel.
     */
    private double minAngle;

    /**
     * The maximum angle during any 1-second period experienced by the steering wheel.
     */
    private double maxAngle;


    /**
     * Creates a new BasicRules object. The only task that this constructor has is to instantiate
     * a queue to store the 10 (or less if it hasn't polled 10 times yet) most recent steering
     * angle values
     */
    public BasicRules() {
        steeringQ = new ArrayDeque<>();
    }

    /**
     * Enforces the maximum engine speed to be 5000 RPM.
     *
     * @param newSpd is the latest (double) engine speed value received from the OpenXC
     *               measurements.
     *
     * @return the engine speed value that broke the rule, or zero if the rule was not broken.
     */
    public double ruleMaxEngSpd(double newSpd) {
        /*
        if rpm is over 5000, return 40 for place addition, otherwise return 0.
         */
        if (newSpd > 5000) {

            /*
            Updates the engine speed break time in InTransitActivity to match the current
            execution time. This will cause this rule to enter a resting period until a new
            execution time threshold is reached.
             */
            InTransitActivity.setEngBreakTime();


            //return the engine speed value that broke the rule
            return newSpd;
        }
        return 0;
    }

    /**
     * Limits the maximum acceleration pedal value to 65.
     *
     * @param newAccel latest acceleration value polled from the vehicle
     * @return the acceleration value if the rule is broken, or zero otherwise.
     */
    public double ruleMaxAccel(double newAccel) {
        if (newAccel > 65) {

           /*
            Updates the acceleration pedal break time in InTransitActivity to match the current
            execution time. This will cause this rule to enter a resting period until a new
            execution time threshold is reached.
             */
            InTransitActivity.setAccelBreakTime();

            //return the acceleration pedal value that broke the rule.
            return newAccel;
        }

        //otherwise, return zero
        return 0;
    }

    /**
     * Limits the maximum vehicle speed value to 120 km/hr.
     *
     * @param newSpeed the latest vehicle speed (km/hr) polled from the vehicle.
     * @return the vehicle speed value that broke the rule, or zero otherwise.
     */
    public double ruleMaxVehSpd(double newSpeed) {
        if (newSpeed > 120) {

            /*
            Updates the vehicle speed break time in InTransitActivity to match the current
            execution time. This will cause this rule to enter a resting period until a new
            execution time threshold is reached.
             */
            InTransitActivity.setSpeedBreakTime();
            System.out.println("Broke speed rule");
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
    public double ruleSteering(double newAngle, double newSpeed) {

        /*
        This exception is to provide clarity in the event that the queue exceeds 10 elements,
        which should never happen.
         */
        if (steeringQ.size() > 10) {
            throw new IndexOutOfBoundsException("steering queue has exceeded 10 elements.");
        }
        // if the queue already has 10 elements, remove the oldest value from the back
        else if (steeringQ.size() == 10) {
            steeringQ.removeLast();
        }

        //add the newest value (most recently polled) to front of queue
        steeringQ.addFirst(newAngle);
        //default value for min and max, this will be updated appropriately in the lines below
        minAngle = newAngle;
        maxAngle = newAngle;

        /*
        For each value in the queue, check if it is the minimum or the maximum, and update those
        fields accordingly.
         */
        for (double val : steeringQ) {
            if (val < minAngle) minAngle = val;
            if (val > maxAngle) maxAngle = val;
        }

        /*
        if angle difference is 90 or greater and speed is greater than 25, clear the queue,
        and return the that angle difference.
         */
        if ((maxAngle - minAngle) >= 90 && newSpeed > 25) {

            /* if this rule is broken, reset the steering queue (so the driver will not be
            penalized again for the same infraction).
             */
            steeringQ.clear();

            /*
            Updates the vehicle speed break time in InTransitActivity to match the current
            execution time. This will cause this rule to enter a resting period until a new
            execution time threshold is reached.
             */
            InTransitActivity.setAngleBreakTime();

            //return that angle difference that broke the rule.
            return (maxAngle - minAngle);
        }

        //otherwise, return zero if the rule is not broken
        return 0;

    }

    /**
     * Enforces this situation: if the engine speed is above 3000 RPM, and the accelerator pedal
     * is pressed in at least 30%, and the steering wheel is rotated at least 60 deg from center
     * in either direction, the driver has committed a violation.
     *
     * @return the engine speed (double) in rpm at which this infraction occurred.
     */

    public double ruleSpeedSteering(double engVal, double accelVal, double steerVal) {
        if (engVal > 3000
                && (steerVal > 60 || steerVal < -60)
                && accelVal > 30) {

           /*
            Updates the speed steering break time in InTransitActivity to match the current
            execution time. This will cause this rule to enter a resting period until a new
            execution time threshold is reached.
             */
            InTransitActivity.setSpeedSteeringBreakTime();

            //return the engine speed that broke this rule.
            return engVal;
        }

        //rule not broken, return zero.
        return 0;
    }
}
