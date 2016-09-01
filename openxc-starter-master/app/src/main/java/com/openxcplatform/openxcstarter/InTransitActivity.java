package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Defines the actions that must happen after the user starts a "Trip", and before the user
 * decides to stop the "Trip". This activity listens for vehicle parameter measurements that are
 * received from the OpenXC Enabler, and calls the rules in BasicRules to evaluate the driver's
 * behavior.
 * When the "Stop Trip" button is pressed, the app will proceed to the "Trip Review" page.
 */
public class InTransitActivity extends Activity {

    //write a tag label for this activity for convenience
    private static final String TAG = "InTransitActivity";

    // all the variables for the background gradient, starts at green
    private ImageView mBackground;

   //TODO: remove red and green if they are not used
    private int red = 0;
    private int green = 255;

    /**
     * Corresponds to how poorly the user has been driving recently. A higher 'place'
     * value signifies more violations. A scale from 0 to 255 that also signifies your location on
     * the color gradient.
     */
    private static int place = 0;


    /** An instance of OpenXC's custom class VehicleManager, which does much of the interfacing
     * work for us. This instance needs to be set up before we can take measurements.
     */
    private VehicleManager mVehicleManager;

    /**
     * A static instance of OpenXC's custom class that stores and processes the engine speed (RPM).
     */
    private static EngineSpeed engSpeed;

    /**
     * A static instance of OpenXC's custom class that stores and processes the vehicle speed (km/hr).
     */
    private static VehicleSpeed vehSpeed;

    /**
     * A static instance of OpenXC's custom class that stores and processes the steering wheel angle (degrees).
     */
    private static SteeringWheelAngle swAngle;

    /**
     * A static instance of OpenXC's custom class that stores and processes the accelerator pedal
     * position (0% to 100%).
     */
    private static AcceleratorPedalPosition accelPosition;

    /**
     * A static field to store the most recent latitude value.
     */
    private static double lat;

    /**
     * A static field to store the most recent longitude value.
     */
    private static double lng;

    /** Chronologically stores all latitude values that are polled during the trip.*/
    private ArrayList<Double> totalLat = new ArrayList<>();
    /** Chronologically stores all longitude values that are polled during the trip.*/
    private ArrayList<Double> totalLong = new ArrayList<>();

    // values being sent to the Map Review page

    /** Chronologically stores all latitude values that correspond to broken rules*/
    private static ArrayList<Double> ruleLat = new ArrayList<>();
    /** Chronologically stores all longitude values that correspond to broken rules*/
    private static ArrayList<Double> ruleLong = new ArrayList<>();
    /** Chronologically stores the names of violations during the trip*/
    private static ArrayList<Integer> errorNames = new ArrayList<>();
    /** Chronologically stores the parameter that broke each rule during the trip*/
    private static ArrayList<Double> errorValues = new ArrayList<>();
    /** Chronologically stores the current screen color rule during the trip (which is used to
     * map the trip lines with the designated color)*/
    private static ArrayList<Integer> errorColors = new ArrayList<>();

    // TODO-jeffrey: remind me to sort later
    private BasicRules standardRules = new BasicRules();

    /**Timer object that allows the background gradient (corresponding to driving quality) to
     * gradually shift back to green.*/
    Timer myTimer = new Timer();

    // These are time variables that allow us to implement a resting period for each rule after
    // it is broken (don't want to penalize the driver multiple times for the same infraction).
    private static long speedBreakTime = 0;
    private static long engBreakTime = 0;
    private static long angleBreakTime = 0;
    private static long accelBreakTime = 0;
    private static long speedSteeringBreakTime = 0;

    // by default, the rest period will be 30,000 ms (30 seconds)
    private int errorMargin = 30000;

    // numbering the rules for more robust handling of cases later on
    static final int MAX_ENG = 1;
    static final int MAX_ACCEL = 2;
    static final int MAX_VEH = 3;
    static final int STEER = 4;
    static final int SPEED_STEER = 5;

    // Button that goes to next activity
    public Button MapReviewButton;

    // simulates a broken rule
    public Button TestButton;    //remove in final presentation


    /**
     When this activity is created, we set the view to the initial green gradient. Also starts
     the timer that constantly shifts the screen back to green.
      */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_transit);

        // this is the emoji that gets applied
        mBackground = (ImageView) findViewById(R.id.overlay_layer);

        // script that changes the gradient from red to green
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                redToGreen();
            }
        }, 0, 1500);

        // button scripts
        goToReview();

    }

    /** When the app is paused, just call the default behavior of the superclass (Activity).
     */
    @Override
    public void onPause() {
        super.onPause();

    }

    /** When app is resumed, start everything back up. */
    @Override
    public void onResume() {

        //perform Activity class's default actions for onResume()
        super.onResume();

        /*
        If the Vehicle Manager object is no longer defined, create one again
         */
        if (mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     listener for vehicle speed, includes a check for the mistake margin (you can only break this
      rule once every 30 seconds) */
    VehicleSpeed.Listener mVSpeedListener = new VehicleSpeed.Listener() {

       /**Actions to be performed when a new measurement is received from the Vehicle Manager. */
        public void receive(Measurement measurement) {

            //cast the measurement as VehicleSpeed object
            vehSpeed = (VehicleSpeed) measurement;

            /*
            The rule check will only happen if at least an 'errorMargin' amount of milliseconds
            have elapsed since the last rule break.
             */
            if (SystemClock.elapsedRealtime() > speedBreakTime + errorMargin) {
                setPlace(MAX_VEH, standardRules.ruleMaxVehSpd(getVeh()));
            }
        }
    };

    /**
     Listener for engine speed, includes a check for the mistake margin (you can only break this
     rule once every 30 seconds).
     */
    EngineSpeed.Listener mEngineSpeedListener = new EngineSpeed.Listener() {
        public void receive(Measurement measurement) {

            //cast the incoming measurement as EngineSpeed object.
            engSpeed = (EngineSpeed) measurement;

             /*
            The rule check will only happen if at least an 'errorMargin' amount of milliseconds
            have elapsed since the last rule break.
             */
            if (SystemClock.elapsedRealtime() > engBreakTime + errorMargin) {

                setPlace(MAX_ENG, standardRules.ruleMaxEngSpd(getEng()));
            }
        }
    };

    /**
     Listener for accelerator pedal position, includes a check for the mistake margin (you can only
     break this rule once every 30 seconds).
     */
    AcceleratorPedalPosition.Listener mAccelListener = new AcceleratorPedalPosition.Listener() {
        public void receive(Measurement measurement) {
            accelPosition = (AcceleratorPedalPosition) measurement;

             /*
            The rule check will only happen if at least an 'errorMargin' amount of milliseconds
            have elapsed since the last rule break.
             */
            if (SystemClock.elapsedRealtime() > accelBreakTime + errorMargin) {

                    setPlace(MAX_ACCEL, standardRules.ruleMaxAccel(getAccel()));
            }
        }
    };

    /**
     Listener for steering wheel angle, includes a check for the mistake margin (you can only
     break this rule once every 30 seconds).
     */
    SteeringWheelAngle.Listener mWheelAngleListener = new SteeringWheelAngle.Listener() {
        public void receive(Measurement measurement) {

           //cast the incoming measurement as a SteeringWheelAngle object.
            swAngle = (SteeringWheelAngle) measurement;

            /*
            The rule check will only happen if at least an 'errorMargin' amount of milliseconds
            have elapsed since the last rule break.
             */

            //check the basic steering rule
            if (SystemClock.elapsedRealtime() > angleBreakTime + errorMargin) {
                setPlace(STEER, standardRules.ruleSteering(getSWAngle(), getVeh()));
            }

            //check the speed steering rule
            if (SystemClock.elapsedRealtime() > speedSteeringBreakTime + errorMargin) {
                setPlace(SPEED_STEER,
                        standardRules.ruleSpeedSteering(getEng(), getAccel(), getSWAngle()));
            }
        }
    };

    /** Listener for latitude, puts the received value into an arrayList of doubles and adds the
    current color to another arrayList. */
    Latitude.Listener mLatListener = new Latitude.Listener() {
        public void receive(Measurement measurement) {
            final Latitude lati = (Latitude) measurement;

           //a Latitude object has methods to access the contained value, and use it as a double.
            lat = lati.getValue().doubleValue();

            //store this latitude value, and update the error color to match current 'place'
            totalLat.add(lat);
            errorColors.add(place);

        }
    };

    /** Listener for longitude, puts the received value into an arrayList of doubles and adds the
     current color to another arrayList. */
    Longitude.Listener mLongListener = new Longitude.Listener() {
        public void receive(Measurement measurement) {
            final Longitude lg = (Longitude) measurement;

            //a Longitude object has methods to access the contained value, and use it as a double.
            lng = lg.getValue().doubleValue();

            //store this longitude value
            totalLong.add(lng);
        }
    };

    /** OpenXC command to support and manage the listeners */
    private ServiceConnection mConnection = new ServiceConnection() {

        /** Actions performed when the connection is obtained. */
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            //add all of the listeners that are defined above in this class.
            mVehicleManager.addListener(EngineSpeed.class, mEngineSpeedListener);
            mVehicleManager.addListener(VehicleSpeed.class, mVSpeedListener);
            mVehicleManager.addListener(SteeringWheelAngle.class, mWheelAngleListener);
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAccelListener);
            mVehicleManager.addListener(Latitude.class, mLatListener);
            mVehicleManager.addListener(Longitude.class, mLongListener);
        }

        /** Actions performed when the connection is broken. */
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
            myTimer.cancel();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        //no additional functionality added here
        super.onPostCreate(savedInstanceState);
    }

    /**
     *Method that makes use of a Timer and slowly transitions the app background back to green
     * (if driving violations have caused it to become yellow/orange/red).
     */
    public void redToGreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // different emojis for different color backgrounds
                if (place > 204) {
                    mBackground.setImageResource(R.drawable.scared_face);
                }
                if (place > 153 && place < 204) {
                    mBackground.setImageResource(R.drawable.sad_face);
                }
                if (place > 102 && place < 153) {
                    mBackground.setImageResource(R.drawable.neutral_face);
                }
                if (place > 51 && place < 102) {
                    mBackground.setImageResource(R.drawable.smiling_face);
                }
                if (place < 51) {
                    mBackground.setImageResource(R.drawable.happy_face);
                }

                // cases to make the color gradient
                if (place > 0 && place < 256 && InTransitActivity.getVeh() > 5) {
                    place--;
                }
                if (place > 255) {
                    place = 255;
                }
                if (place < 128) {
                    mBackground.setBackgroundColor(Color.argb(255, place * 2, 255, 0));
                }
                if (place >= 128) {
                    mBackground.setBackgroundColor(Color.argb(255, 255, 256 - 2 * (place - 127),
                            0));
                }

            }
        });
    }

    /**
     * Activates a listener for when the "stop trip" button is pressed. At that point in time,
     * the listeners and scripts on this activity are stopped, and the app will proceed to the
     * map activity page.
     */
    public void goToReview() {
        MapReviewButton = (Button) findViewById(R.id.stop_button);

        MapReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // removes all the listeners, stops the scripts, etc
                stopEverything();

                // transfers the map data to MapReviewActivity
                Intent transferMapData = new Intent(InTransitActivity.this, MapReviewActivity
                        .class);
                // entire trip coordinates
                transferMapData.putExtra("latitude", totalLat);
                transferMapData.putExtra("longitude", totalLong);

                // coordinates of places where a rule is broken
                transferMapData.putExtra("ruleLatitude", ruleLat);
                transferMapData.putExtra("ruleLongitude", ruleLong);

                // data about the broken rule
                transferMapData.putExtra("errorNames", errorNames);
                transferMapData.putExtra("errorValues", errorValues);
                transferMapData.putExtra("errorColors", errorColors);

                startActivity(transferMapData);
            }
        });
    }

    /**
     * Adds <code>newPlace</code> to the <code>place</code> field, but keeps <code>place</code>
     * at an upper limit of 255. setPlace() is called when a violation occurs, so this method
     * also adds a latitude/longitude pair to the list of violation locations.
     *
     * @param ruleNum    is an integer corresponding to one of the rules.
     * @param errorValue is the (double) value that broke the rule, or ZERO if the rule was not
     *                   broken.
     */
    public void setPlace(int ruleNum, double errorValue) {
    //TODO: I think we should change this from public to private (and just call it within
    // this class)

        //if errorValue is 0 (rule was not broken), this method ends without doing anything
        if (errorValue > 0) {

            //different rule breaks will have a different severity of color-change toward red
            int penalty;

            //add the parameters for this rule break to the arraylists of all rule
            // breaks
            ruleLat.add(lat);
            ruleLong.add(lng);
            errorNames.add(ruleNum);
            errorValues.add(errorValue);

            /* This is a switch statement that assigns different place penalties for each rule
             */
            switch (ruleNum){
                case MAX_ENG:
                    penalty = 40;
                    break;
                case MAX_ACCEL:
                    penalty = 30;
                    break;
                case MAX_VEH:
                    penalty = 80;
                    break;
                case STEER:
                    penalty = 50;
                    break;
                case SPEED_STEER:
                    penalty = 100;
                    break;

                //the case below is just to catch an unexpected ruleNum value passed in.
                default:
                    System.out.println("switch statement in setPlace failed!");
                    throw new IllegalArgumentException();
            }

            // guarantees that place does not exceed 255
            place = Math.min(place + penalty, 255);
        }
    }

    /** removes and unbinds OpenXC listeners */
    private void stopEverything() {
        // stops VehicleManager Listeners
        if (mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            mVehicleManager.removeListener(EngineSpeed.class,
                    mEngineSpeedListener);
            mVehicleManager.removeListener(VehicleSpeed.class,
                    mVSpeedListener);
            mVehicleManager.removeListener(SteeringWheelAngle.class,
                    mWheelAngleListener);
            mVehicleManager.removeListener(AcceleratorPedalPosition.class,
                    mAccelListener);
            mVehicleManager.removeListener(Latitude.class, mLatListener);
            mVehicleManager.removeListener(Longitude.class, mLongListener);

            unbindService(mConnection);
            mVehicleManager = null;
        }
        // stops timer script
        myTimer.cancel();
        TestButton.setOnClickListener(null);
    }

    /**Get the vehicle speed (double) from the stored vehSpeed field. */
    public static double getVeh() {

        //TODO: additional check to let user know when vehSpeed is staying NULL for
        //extended period of time (which may or may not be a possible scenario)
        if (vehSpeed == null) {
            return 0.0;
        }

        //return the vehicle speed as a double
        return vehSpeed.getValue().doubleValue();
    }

    /**Get the engine speed (double) from the stored engSpeed field. */
    public static double getEng() {

        if (engSpeed == null){
            return 0.0;
        }


        return engSpeed.getValue().doubleValue();
    }

    /**Get the steering wheel angle (double) from the stored swAngle field. */
    public static double getSWAngle() {
        if (swAngle == null) {

            return 0.0;
        }
        return swAngle.getValue().doubleValue();
    }

    /**Get the accelerator pedal position (double) from the stored accelPosition field. */
    public static double getAccel() {
        if (accelPosition == null){

            return 0.0;
        }
        return accelPosition.getValue().doubleValue();
    }

    /** Update this rule's break time to the current execution time. */
    public static void setSpeedBreakTime() { speedBreakTime = SystemClock.elapsedRealtime();}
    /** Update this rule's break time to the current execution time. */
    public static void setEngBreakTime() { engBreakTime = SystemClock.elapsedRealtime();}
    /** Update this rule's break time to the current execution time. */
    public static void setAngleBreakTime() { angleBreakTime = SystemClock.elapsedRealtime();}
    /** Update this rule's break time to the current execution time. */
    public static void setAccelBreakTime() { accelBreakTime = SystemClock.elapsedRealtime();}
    /** Update this rule's break time to the current execution time. */
    public static void setSpeedSteeringBreakTime () { speedSteeringBreakTime = SystemClock.elapsedRealtime();}
}
