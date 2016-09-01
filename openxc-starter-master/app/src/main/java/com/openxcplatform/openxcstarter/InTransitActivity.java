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
    // TODO-CR
    private CustomRules newRules = new CustomRules();
    private boolean rulesChecked;

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

       //todo-CR
        // initial check for custom rules
        rulesChecked = RulesFragment.getRulesChecked();

        // script that changes the gradient from red to green
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                redToGreen();
            }
        }, 0, 1500);

        // button scripts
        goToReview();
        testRule();
    }

    /** When the app is paused, just call the default behavior of the superclass (Activity).
     */
    @Override
    public void onPause() {
        super.onPause();

    }

    // when the app is resumed, start everything
    @Override
    public void onResume() {
        super.onResume();
        if (mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /*
     listener for vehicle speed, includes a check for the mistake margin (you can only break this
      rule once every 30 seconds), then checks for a custom vs standard ruleset
      */
    VehicleSpeed.Listener mVSpeedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
            vehSpeed = (VehicleSpeed) measurement;
//            System.out.println(vehSpeed.getValue().toString());       // prints are for debugging
//            System.out.println("Time to next rule broken: " + (speedBreakTime + errorMargin -
// globalClock.elapsedRealtime()));
            if (SystemClock.elapsedRealtime() > speedBreakTime + errorMargin) {
                if (rulesChecked == true && RulesFragment.getvSMax() != 0) {
                    setPlace(MAX_VEH, newRules.customMaxVehSpd(getVeh(), RulesFragment.getvSMax()));
                } else {
                    setPlace(MAX_VEH, standardRules.ruleMaxVehSpd(getVeh()));
                }
            }
        }
    };

    // same as above
    EngineSpeed.Listener mEngineSpeedListener = new EngineSpeed.Listener() {
        public void receive(Measurement measurement) {
            engSpeed = (EngineSpeed) measurement;
//            System.out.println(engSpeed.getValue().toString());       // prints are for debugging
            if (SystemClock.elapsedRealtime() > engBreakTime + errorMargin) {
                if (rulesChecked && RulesFragment.getEngMax() != 0) {
                    System.out.println("engine speed ----- "+getEng());
                    setPlace(MAX_ENG, newRules.customMaxEngSpd(getEng(), RulesFragment.getEngMax()));
                } else {
                    setPlace(MAX_ENG, standardRules.ruleMaxEngSpd(getEng()));
                }
            }
        }
    };

    // same as above
    AcceleratorPedalPosition.Listener mAccelListener = new AcceleratorPedalPosition.Listener() {
        public void receive(Measurement measurement) {
            accelPosition = (AcceleratorPedalPosition) measurement;
//            System.out.println(accelPosition.getValue().toString());       // prints are for debugging
            if (SystemClock.elapsedRealtime() > accelBreakTime + errorMargin) {
                if (rulesChecked == true && RulesFragment.getAccelMax() != 0) {
                    setPlace(MAX_ACCEL, newRules.customMaxAccel(getAccel(), RulesFragment.getAccelMax()));
                } else {
                    setPlace(MAX_ACCEL, standardRules.ruleMaxAccel(getAccel()));
                }
            }
        }
    };

    // same as above
    SteeringWheelAngle.Listener mWheelAngleListener = new SteeringWheelAngle.Listener() {
        public void receive(Measurement measurement) {
            swAngle = (SteeringWheelAngle) measurement;
            if (SystemClock.elapsedRealtime() > angleBreakTime + errorMargin) {
                setPlace(STEER, standardRules.ruleSteering(getSWAngle(), getVeh()));
            }
            if (SystemClock.elapsedRealtime() > speedSteeringBreakTime + errorMargin) {
                setPlace(SPEED_STEER,
                        standardRules.ruleSpeedSteering(getEng(), getAccel(), getSWAngle()));
            }
        }
    };

    // listener for latitude, puts the received value into an arrayList of doubles and adds the
    // current color to another arrayList
    Latitude.Listener mLatListener = new Latitude.Listener() {
        public void receive(Measurement measurement) {
            final Latitude lati = (Latitude) measurement;
            lat = lati.getValue().doubleValue();
            totalLat.add(lat);
            errorColors.add(place);
//            System.out.println(lati);       // for testing
        }
    };

    // same as above
    Longitude.Listener mLongListener = new Longitude.Listener() {
        public void receive(Measurement measurement) {
            final Longitude lg = (Longitude) measurement;
            lng = lg.getValue().doubleValue();
            totalLong.add(lng);
        }
    };

    // OpenXC command to support and manage the listeners
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            mVehicleManager.addListener(EngineSpeed.class, mEngineSpeedListener);
            mVehicleManager.addListener(VehicleSpeed.class, mVSpeedListener);
            mVehicleManager.addListener(SteeringWheelAngle.class, mWheelAngleListener);
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAccelListener);
            mVehicleManager.addListener(Latitude.class, mLatListener);
            mVehicleManager.addListener(Longitude.class, mLongListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
            myTimer.cancel();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    // the timer!!
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
                System.out.println("place: " + place);  // for testing
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

    // test button
    public void testRule() {
        TestButton = (Button) findViewById(R.id.test_Button);

        TestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPlace(STEER, 420); //test values
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

    //TODO-spencer: if newPlace is 0, this method does nothing. Can this be simplified
    // outside of this method?

        if (errorValue > 0) {
            int newPlace;
            ruleLat.add(lat);
            ruleLong.add(lng);
            errorNames.add(ruleNum);
            errorValues.add(errorValue);

            /* This is a switch statement that assigns different place penalties for each rule
             */
            switch (ruleNum){
                case MAX_ENG:
                    newPlace = 40;
                    break;
                case MAX_ACCEL:
                    newPlace = 30;
                    break;
                case MAX_VEH:
                    newPlace = 80;
                    break;
                case STEER:
                    newPlace = 50;
                    break;
                case SPEED_STEER:
                    newPlace = 100;
                    break;

                //the case below is just to catch an unexpected ruleNum value passed in.
                default:
                    System.out.println("switch statement in setPlace failed!");
                    throw new IllegalArgumentException();
            }

            // guarantees that place does not exceed 255
            place = Math.min(place + newPlace, 255);
        }
    }

    // removes and unbinds OpenXC listeners
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

    // getters and setters
    public static double getVeh() {
        if (vehSpeed == null) {
            System.out.println("vehicle speed is null.");
            return 0.0;
        }
        return vehSpeed.getValue().doubleValue();
    }

    public static double getEng() {
        if (engSpeed == null){
            System.out.println("engine speed is null.");
            return 0.0;
        }
        return engSpeed.getValue().doubleValue();
    }

    public static double getSWAngle() {
        if (swAngle == null) {
            System.out.println("accelerator position is null.");
            return 0.0;
        }
        return swAngle.getValue().doubleValue();
    }

    public static double getAccel() {
        if (accelPosition == null){
            System.out.println("accelerator position is null.");
            return 0.0;
        }
        return accelPosition.getValue().doubleValue();
    }

    public static void setSpeedBreakTime() { speedBreakTime = SystemClock.elapsedRealtime();}

    public static void setEngBreakTime() { engBreakTime = SystemClock.elapsedRealtime();}

    public static void setAngleBreakTime() { angleBreakTime = SystemClock.elapsedRealtime();}

    public static void setAccelBreakTime() { accelBreakTime = SystemClock.elapsedRealtime();}

    public static void setSpeedSteeringBreakTime () { speedSteeringBreakTime = SystemClock.elapsedRealtime();}
}
