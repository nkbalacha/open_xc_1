package com.openxcplatform.openxcstarter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleSpeed;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class InTransitActivity extends Activity {
    private static final String TAG = "InTransitActivity";

    // so background starts at all green
    private TextView mBackground;
    private int red = 0;
    private int green = 255;

    private VehicleManager mVehicleManager;
    private EngineSpeed engSpeed;
    private VehicleSpeed vehSpeed;
    private SteeringWheelAngle swAngle;

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
        }, 0, 100);

        goToReview();
        testRule();
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

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            mVehicleManager.addListener(EngineSpeed.class, mEngineSpeedListener);
            mVehicleManager.addListener(VehicleSpeed.class, mVSpeedListener);
            mVehicleManager.addListener(SteeringWheelAngle.class, mWheelAngleListener);
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
                if (green == 255 && red == 0) {
                //    mBackground.setBackgroundResource(R.drawable.happy_driving);
                } else {
                    if (red > 0) {
                        red--;
                    }
                    if (green < 255) {
                        green++;
                    }
                    mBackground.setBackgroundColor(Color.argb(255, red, green, 0));
                }
            }
        });
    }

    public void rule()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (engSpeed.getValue().doubleValue() > 685) {
                    red = red + 60;
                    green = green - 60;
                    /*try {
                        Thread.sleep(8000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }*/
                }
            }
        });
    }

    public Button MapReviewButton;
    public void goToReview() {
        MapReviewButton = (Button)findViewById(R.id.stop_button);

        MapReviewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent changePage = new Intent(InTransitActivity.this, MapReviewActivity.class);

                startActivity(changePage);
            }
        });
    }

    public Button TestButton;
    public void testRule() {
        TestButton = (Button)findViewById(R.id.test_Button);

        TestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                red = red + 60;
                green = green - 60;
            }
        });
    }
}
