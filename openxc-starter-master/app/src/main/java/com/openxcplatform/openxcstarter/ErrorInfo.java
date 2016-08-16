package com.openxcplatform.openxcstarter;

import java.util.ArrayList;

/**
 * Created by Jeffrey on 8/16/2016.
 */
public class ErrorInfo {
    public ArrayList<Integer> errorNumber = new ArrayList<Integer>();
    public ArrayList<Double> errorValue= new ArrayList<Double>();

    public ErrorInfo(int errorNumber, Double errorValue) {
        this.errorNumber.add(errorNumber);
        this.errorValue.add(errorValue);
    }

    public void add(int errorNumber, Double errorValue) {
        this.errorNumber.add(errorNumber);
        this.errorValue.add(errorValue);
    }
}
