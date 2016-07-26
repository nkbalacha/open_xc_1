package com.openxcplatform.openxcstarter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.openxc.VehicleManager;
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
    private static double lat;
    private static double lng;

    // map coordinates
    private ArrayList<Double> totalLat = new ArrayList<>();
    private ArrayList<Double> totalLong = new ArrayList<>();
    private ArrayList<Double> ruleLat = new ArrayList<>();
    private ArrayList<Double> ruleLong = new ArrayList<>();

    // rules
    private BasicRules standardRules = new BasicRules();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_in_transit);

        // This is no longer necessary
         mBackground = (TextView)findViewById(R.id.fullscreen_content);

        // constantly changing from red to green
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                redToGreen();
            }
        }, 0, 500);


        // buttons
        goToReview();
        testRule();
        //    getLocation();
        /*if (engSpeed != null) {
            BasicRules.ruleOne();
        }*/
    }


    @Override
    public void onPause() {
        super.onPause();
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            mVehicleManager.removeListener(EngineSpeed.class,
                    mEngineSpeedListener);
            mVehicleManager.removeListener(VehicleSpeed.class,
                    mVSpeedListener);
            mVehicleManager.removeListener(SteeringWheelAngle.class,
                    mWheelAngleListener);
            mVehicleManager.removeListener(Latitude.class, mLatListener);
            mVehicleManager.removeListener(Longitude.class, mLongListener);

            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    EngineSpeed.Listener mEngineSpeedListener = new EngineSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final EngineSpeed speed = (EngineSpeed) measurement;
            InTransitActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    engSpeed = speed;
                }
            });
        }
    };

    VehicleSpeed.Listener mVSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            InTransitActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vehSpeed = speed;
                }
            });
        }
    };

    SteeringWheelAngle.Listener mWheelAngleListener = new SteeringWheelAngle.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final SteeringWheelAngle angle = (SteeringWheelAngle) measurement;
            InTransitActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swAngle = angle;
                }
            });
        }
    };

    Latitude.Listener mLatListener = new Latitude.Listener(){
        @Override
        public void receive(Measurement measurement) {
            final Latitude lati = (Latitude) measurement;
            InTransitActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lat = lati.getValue().doubleValue();
                    totalLat.add(lat);
                }
            });
        }
    };

    Longitude.Listener mLongListener = new Longitude.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Longitude lg = (Longitude) measurement;
            InTransitActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lng = lg.getValue().doubleValue();
                    totalLong.add(lng);
                }
            });
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
            mVehicleManager.addListener(Latitude.class, mLatListener);
            mVehicleManager.addListener(Longitude.class, mLongListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
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
                if (place == 0) {
                    mBackground.setBackgroundResource(R.drawable.happy_driving);
                } else {
                    if (place > 0 && place < 256) {
                        place--;
                    }
                    if (place > 255) {
                        place = 255;
                    }
                    mBackground.setBackgroundColor(Color.argb(255, red + place, green - place, 0));
                    System.out.println(place);
                }
            }
        });
    }


    // moved to BasicRules class
    /*public void rule()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (engSpeed.getValue().doubleValue() > 1000) {
                    place = place + 60;
                }
            }
        });
    }*/

    public Button MapReviewButton;
    public void goToReview() {
        MapReviewButton = (Button)findViewById(R.id.stop_button);

        MapReviewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent transferMapData = new Intent(InTransitActivity.this, MapReviewActivity.class);

                transferMapData.putExtra("latitude", totalLat);
                transferMapData.putExtra("longitude", totalLong);
                transferMapData.putExtra("ruleLatitude", ruleLat);
                transferMapData.putExtra("ruleLongitude", ruleLong);

                startActivity(transferMapData);

            }
        });
    }

    public Button TestButton;
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

    /*public void getLocation() {
        totalLat.add(lat);
        totalLong.add(lng);
    }*/


    // getters and setters
    public static double getEng () {
        return engSpeed.getValue().doubleValue();
    }

    public static double getVeh () { return vehSpeed.getValue().doubleValue();}

    public static double getSWAngle () { return swAngle.getValue().doubleValue();}

    public static void setPlace(int newPlace) {
        place = place + newPlace;
    }

}
