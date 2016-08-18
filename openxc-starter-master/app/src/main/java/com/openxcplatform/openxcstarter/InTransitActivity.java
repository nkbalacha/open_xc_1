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

public class InTransitActivity extends Activity {

    private static final String TAG = "InTransitActivity";

    // all the variables for the background gradient, starts at green
    private ImageView mBackground;
    private int red = 0;
    private int green = 255;
    /**
     * Corresponds to how poorly the user has been driving recently. A higher <code>place</code>
     * value signifies more violations.
     */
    private static int place = 0;

    // all the OpenXC data variables that we measure
    private VehicleManager mVehicleManager;
    private static EngineSpeed engSpeed;
    private static VehicleSpeed vehSpeed;
    private static SteeringWheelAngle swAngle;
    private static AcceleratorPedalPosition accelPosition;
    private static double lat;
    private static double lng;

    // map coordinates
    private ArrayList<Double> totalLat = new ArrayList<>();
    private ArrayList<Double> totalLong = new ArrayList<>();

    // values being sent to the Map Review page
    private static ArrayList<Double> ruleLat = new ArrayList<>();
    private static ArrayList<Double> ruleLong = new ArrayList<>();
    private static ArrayList<Integer> errorNames = new ArrayList<>();
    private static ArrayList<Double> errorValues = new ArrayList<>();
    private static ArrayList<Integer> errorColors = new ArrayList<>();

    // misc variables, remind me to sort later
    private BasicRules standardRules = new BasicRules();
    private CustomRules newRules = new CustomRules();
    Timer myTimer = new Timer();
    public Button TestButton;    //remove in final presentation
    public Button MapReviewButton;
    private boolean rulesChecked;
    private static long speedBreakTime = 0;
    private static long engBreakTime = 0;
    private static long angleBreakTime = 0;
    private static long accelBreakTime = 0;
    private static long speedSteeringBreakTime = 0;

    private int errorMargin = 30000;

    static final int MAX_ENG = 1;
    static final int MAX_ACCEL = 2;
    static final int MAX_VEH = 3;
    static final int STEER = 4;
    static final int SPEED_STEER = 5;

    // when this activity is created, we set the view to the initial green gradient
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_transit);

        // this is the emoji that gets applied
        mBackground = (ImageView) findViewById(R.id.overlay_layer);

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

    // when the app is paused, stop everything
    @Override
    public void onPause() {
        super.onPause();
    //    stopEverything(); 
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
                setPlace(STEER, standardRules.ruleSteering(getSWAngle()));
            }
            if (SystemClock.elapsedRealtime() > speedSteeringBreakTime + errorMargin) {
                setPlace(SPEED_STEER,
                        standardRules.ruleSpeedSteering(getEng(), getAccel(), getSWAngle()));
            }
        }
    };

    // listener for latitude, puts the received value into an arrayList of doubles and adds the
    // current
    // color to another arrayList
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

    // actually no idea what this is for
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

                // algorithm to get specific color gradient

                if (place > 0 && place < 256) {
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

                transferMapData.putExtra("latitude", totalLat);
                transferMapData.putExtra("longitude", totalLong);
                transferMapData.putExtra("ruleLatitude", ruleLat);
                transferMapData.putExtra("ruleLongitude", ruleLong);

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

    // getters and setters
    public static double getVeh() {
        return vehSpeed.getValue().doubleValue();
    }

    public static double getEng() {
        return engSpeed.getValue().doubleValue();
    }

    public static double getSWAngle() {
        return swAngle.getValue().doubleValue();
    }

    public static double getAccel() {
        return accelPosition.getValue().doubleValue();
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

    public static void setSpeedBreakTime() { speedBreakTime = SystemClock.elapsedRealtime();}

    public static void setEngBreakTime() { engBreakTime = SystemClock.elapsedRealtime();}

    public static void setAngleBreakTime() { angleBreakTime = SystemClock.elapsedRealtime();}

    public static void setAccelBreakTime() { accelBreakTime = SystemClock.elapsedRealtime();}

    public static void setSpeedSteeringBreakTime () { speedSteeringBreakTime = SystemClock.elapsedRealtime();}

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
}
