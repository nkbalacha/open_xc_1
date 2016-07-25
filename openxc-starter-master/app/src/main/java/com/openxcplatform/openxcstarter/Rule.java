package com.openxcplatform.openxcstarter;

/**
 * Created by Jeffrey on 7/18/2016.
 */
public class Rule {
    int time;
    Scenario start;
    Scenario end;

    // time may be a String during parse
    public Rule(int time, Scenario start, Scenario end) {
        this.time = time;
        this.start = start;
        this.end = end;
    }

}
