package com.openxcplatform.openxcstarter;

import java.util.ArrayList;

/*
helper class for MapReviewActivity, used to store data
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
