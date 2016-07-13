package com.openxc.openxcstarter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.openxcplatform.openxcstarter.R;

import com.openxc.VehicleManager;

import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.VehicleMessage;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.EngineSpeed;

public class StarterActivity extends Activity {
    private static final String TAG = "StarterActivity";

    private VehicleManager mVehicleManager;
    private TextView mEngineSpeedView;
    private TextView mVehicleSpeedView;
    private TextView mSteeringAngleView;
    private TextView mFuelConsumedView;
    private TextView mTestDiag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        // grab a reference to the engine speed text object in the UI, so we can
        // manipulate its value later from Java code
        mEngineSpeedView = (TextView) findViewById(R. id.engine_speed);
        mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
        mSteeringAngleView = (TextView) findViewById(R.id.steering_angle);
        mFuelConsumedView = (TextView) findViewById(R.id.fuel_consumption);
        mTestDiag = (TextView) findViewById(R.id.diag_command_test);
        Button diagButton = (Button)findViewById(R.id.but_diag);

        diagButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        TextView myTextView = mTestDiag;
                        VehicleMessage response = mVehicleManager.request(new DiagnosticRequest(1, 2, 3));
                        if(response != null) {
                            DiagnosticResponse diagnosticResponse = response.asDiagnosticResponse();
                            myTextView.setText(diagnosticResponse.toString());
                        } else {
                            myTextView.setText("No diagnostic response or no vehicle located.");
                        }

                    }
                }
        );

    }


    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.
            mVehicleManager.removeListener(EngineSpeed.class,
                    mEngineSpeedListener);
            mVehicleManager.removeListener(VehicleSpeed.class,
                    mVSpeedListener);
            mVehicleManager.removeListener(SteeringWheelAngle.class,
                    mWheelAngleListener);
            mVehicleManager.removeListener(FuelConsumed.class,
                    mFuelListener);

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
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mEngineSpeedView.setText("Engine speed (RPM): "
                            + speed.getValue().doubleValue());
                }
            });
        }
    };

    VehicleSpeed.Listener mVSpeedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVehicleSpeedView.setText("Vehicle speed (KM/H): "
                            + speed.getValue().doubleValue());
                }
            });
        }
    };

    SteeringWheelAngle.Listener mWheelAngleListener = new SteeringWheelAngle.Listener() {
        public void receive(Measurement measurement) {
            final SteeringWheelAngle angle = (SteeringWheelAngle) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSteeringAngleView.setText("Steering Wheel Angle: "
                            + angle.getValue().doubleValue());
                }
            });
        }
    };

    FuelConsumed.Listener mFuelListener = new FuelConsumed.Listener() {
        public void receive(Measurement measurement) {
            final FuelConsumed fuel = (FuelConsumed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFuelConsumedView.setText("Fuel Consumed: "
                            + fuel.getValue().doubleValue());
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
            mVehicleManager.addListener(FuelConsumed.class, mFuelListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }
}
