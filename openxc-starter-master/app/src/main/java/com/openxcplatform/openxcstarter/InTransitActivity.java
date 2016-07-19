package com.openxcplatform.openxcstarter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class InTransitActivity extends Activity {

    private TextView mBackground;
    private int red = 255;
    private int green = 0;
    /*int images[] = {R.drawable.happy_driving, R.drawable.sad_driving,
            R.drawable.displeased_driving, R.drawable.ok_driving};
    private static int currentBackground = 0;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_in_transit);

        mBackground = (TextView)findViewById(R.id.fullscreen_content);

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                TimerMethod();
            }
        }, 0, 100);

        goToReview();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    // the timer!!
    public void TimerMethod()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (green < 250) {
                    mBackground.setBackgroundColor(Color.argb(255, red, green, 0));
                    red = red - 1;
                    green = green + 1;
                } else {
                    mBackground.setBackgroundResource(R.drawable.happy_driving);
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

}
