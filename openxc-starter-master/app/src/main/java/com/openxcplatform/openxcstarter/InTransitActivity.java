package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.icu.util.Measure;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    // so background starts at all green
    private TextView mBackground;
    private int red = 0;
    private int green = 255;
    private static int place = 0;

    // OpenXC data
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
    private static ArrayList<Double> ruleLat = new ArrayList<>();
    private static ArrayList<Double> ruleLong = new ArrayList<>();

    // misc
    private BasicRules standardRules = new BasicRules();
    private CustomRules newRules = new CustomRules();
    Timer myTimer = new Timer();
    public Button TestButton;    //remove in final presentation
    public Button MapReviewButton;
    private boolean rulesChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_in_transit);

        // TODO: This is no longer necessary
         mBackground = (TextView)findViewById(R.id.fullscreen_content); //we can remove this

        rulesChecked = RulesFragment.getRulesChecked(); //custom rules or no

        // test for switch in RulesFragment
        System.out.println(rulesChecked); //it works

        // constantly changing from red to green
        myTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                redToGreen();
            }
        }, 0, 500);


        // button scripts
        goToReview();
        testRule();
    //    getLocation();

        /*
        Ideally we'd have something here that initializes a ruleset and then runs it throughout the
        activity. Currently it fails because the listeners are called after the stuff here begins.
         */
        /*if (rulesChecked = true) {
            new ruleSet = new CustomRules();
        } else {
            new ruleSet = new BasicRules();
        }*/

        /*
        some more ideas: we could turn "rules" into an interface and have basic/custom rules extend
        that interface. That should let us use the same methods to call both by just initializing
        them as different objects.
         */

    }

    @Override
    public void onPause() {
        super.onPause();
        stopEverything();  //unbinds everything
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    VehicleSpeed.Listener mVSpeedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
                    vehSpeed = speed;
                    if (rulesChecked = true && RulesFragment.getvSMax() != 0) {
                        newRules.customMaxVehSpd(RulesFragment.getvSMax());
                    } else {
                        standardRules.ruleMaxVehSpd();
                    }
        }
    };

    EngineSpeed.Listener mEngineSpeedListener = new EngineSpeed.Listener() {
        public void receive(Measurement measurement) {
            final EngineSpeed speed = (EngineSpeed) measurement;
                    engSpeed = speed;
                    if (rulesChecked = true && RulesFragment.getEngMax() != 0) {
                        newRules.customMaxEngSpd(RulesFragment.getEngMax());
                    } else {
                        standardRules.ruleMaxEngSpd();
                        standardRules.ruleSpeedSteering();
                    }
        }
    };

    AcceleratorPedalPosition.Listener mAccelListener = new AcceleratorPedalPosition.Listener() {
        public void receive(Measurement measurement) {
            final AcceleratorPedalPosition position = (AcceleratorPedalPosition) measurement;
                    accelPosition = position;
                    if (rulesChecked = true && RulesFragment.getAccelMax() != 0) {
                        newRules.customMaxAccel(RulesFragment.getAccelMax());
                    } else {
                        standardRules.ruleMaxAccel();
                    }
        }
     };

    SteeringWheelAngle.Listener mWheelAngleListener = new SteeringWheelAngle.Listener() {
        public void receive(Measurement measurement) {
            final SteeringWheelAngle angle = (SteeringWheelAngle) measurement;
                    swAngle = angle;
                    standardRules.ruleSteering();
                }
    };

    Latitude.Listener mLatListener = new Latitude.Listener(){
        public void receive(Measurement measurement) {
            final Latitude lati = (Latitude) measurement;
                    lat = lati.getValue().doubleValue();
                    totalLat.add(lat);
                }
    };

    Longitude.Listener mLongListener = new Longitude.Listener() {
        public void receive(Measurement measurement) {
            final Longitude lg = (Longitude) measurement;
                    lng = lg.getValue().doubleValue();
                    totalLong.add(lng);
                }
    };


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
        //    System.out.println("after adding listeners");
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
    public void redToGreen()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //if (place == 0) {
                //    mBackground.setBackgroundResource(R.drawable.happy_driving);
                //} else {
                    if (place > 0 && place < 256) {
                        place--;
                    }
                    if (place > 255) {
                        place = 255;
                    }
                    mBackground.setBackgroundColor(Color.argb(255, red + place, green - place, 0));
                   // System.out.println(place);  test to check the color
    //            }
            }
        });
    }

    /** Activates a listener for when the "stop trip" button is pressed. At that point in time,
     * the listeners and scripts on this activity are stopped, and the app will proceed to the
     * map activity page.
     *
      */
    public void goToReview() {
        MapReviewButton = (Button)findViewById(R.id.stop_button);

        MapReviewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                // removes all the listeners, stops the scripts, etc
                stopEverything();

                // transfers the map data
                Intent transferMapData = new Intent(InTransitActivity.this, MapReviewActivity.class);

                transferMapData.putExtra("latitude", totalLat);
                transferMapData.putExtra("longitude", totalLong);
                transferMapData.putExtra("ruleLatitude", ruleLat);
                transferMapData.putExtra("ruleLongitude", ruleLong);
         //       System.out.println("TotalLat: " + totalLat.toString());
         //       System.out.println("TotalLong: " + totalLong.toString());
                startActivity(transferMapData);
            }
        });
    }

    public void testRule() {
        TestButton = (Button)findViewById(R.id.test_Button);

        TestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                place = place + 60;
                System.out.println(place);
                ruleLat.add(lat);
                ruleLong.add(lng);
            }
        });
    }

    //done inside the listeners now
    /*public void getLocation() {
        totalLat.add(lat);
        totalLong.add(lng);
    }*/


    // getters and setters
    public static double getEng () { return engSpeed.getValue().doubleValue();}

    public static double getVeh () { return vehSpeed.getValue().doubleValue();}

    public static double getSWAngle () { return swAngle.getValue().doubleValue();}

    public static double getAccel () { return accelPosition.getValue().doubleValue();}

    public static void setPlace(int newPlace) {
        place = place + newPlace;
        ruleLat.add(lat);
        ruleLong.add(lng);
    }

    private void stopEverything() {
        // stops VehicleManager Listeners
        if(mVehicleManager != null) {
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
