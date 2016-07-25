package com.openxcplatform.openxcstarter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.icu.util.Measure;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
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

/**
 * Defines the actions that occur as an Activity while the user is driving,
 * and OpenXC is reading data.
 */
public class InTransitActivity extends Activity {
    private static final String TAG = "InTransitActivity";

    // so background starts at all green
    private TextView mBackground;
    private int red = 0;
    private int green = 255;
    /* red = place, green = 255 - place */
    private int place = 0;

    // OpenXC data
    private VehicleManager mVehicleManager;
    private EngineSpeed engSpeed;
    private VehicleSpeed vehSpeed;
    private SteeringWheelAngle swAngle;
    private double lat;
    private double lng;

    // map coordinates
    private ArrayList<Double> totalLat = new ArrayList<>();
    private ArrayList<Double> totalLong = new ArrayList<>();
    private ArrayList<Double> ruleLat = new ArrayList<>();
    private ArrayList<Double> ruleLong = new ArrayList<>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_in_transit);

        // This is no longer necessary
        mBackground = (TextView) findViewById(R.id.fullscreen_content);

        // constantly changing from red to green
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                redToGreen();
            }
        }, 0, 500);

        // buttons
        goToReview();
        testRule();
        getLocation();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVehicleManager != null) {
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
        if (mVehicleManager == null) {
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
                    rule();
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

    Latitude.Listener mLatListener = new Latitude.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Latitude lati = (Latitude) measurement;
            InTransitActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lat = lati.getValue().doubleValue();
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
    public void redToGreen() {
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

    public void rule() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (engSpeed.getValue().doubleValue() > 693) {
                    place = place + 60;
                }
            }
        });
    }

    public Button MapReviewButton;

    public void goToReview() {
        MapReviewButton = (Button) findViewById(R.id.stop_button);

        MapReviewButton.setOnClickListener(new View.OnClickListener() {
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
        TestButton = (Button) findViewById(R.id.test_Button);

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

    public void getLocation() {
        totalLat.add(lat);
        totalLong.add(lng);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "InTransit Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.openxcplatform.openxcstarter/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "InTransit Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.openxcplatform.openxcstarter/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
