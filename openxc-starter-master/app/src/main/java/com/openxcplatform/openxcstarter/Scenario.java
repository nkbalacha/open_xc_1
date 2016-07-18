package com.openxcplatform.openxcstarter;

/**
 * Created by Jeffrey on 7/18/2016.
 */
public class Scenario {
    private boolean accelReq = false;
    private boolean speedReq = false;
    private boolean wheelReq = false;
    private boolean brakesReq = false;

    private double accel;
    private double speed;
    private double wheel;
    private boolean brakes;

    public Scenario() {
    }

    public Scenario(String accel, String speed, String wheel, String brakes) {
        if(!accel.equals("")) {
            accelReq = true;
            this.accel = Double.parseDouble(accel);
        }
        if(!speed.equals("")) {
            speedReq = true;
            this.speed = Double.parseDouble(speed);
        }
        if(!wheel.equals("")) {
            wheelReq = true;
            this.wheel = Double.parseDouble(wheel);
        }
        if(!brakes.equals("")) {
            brakesReq = true;
            this.brakes = true;    //fix this later
        }
    }

}
