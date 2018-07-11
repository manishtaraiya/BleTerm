package com.manishtaraiya.bleterm;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.manishtaraiya.bleterm.Scan.ScanActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        final TextView img_animation = (TextView) findViewById(R.id.textView);

       /*TranslateAnimation animation = new TranslateAnimation(500.0f, 0.0f,
                0.0f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)

        animation.setDuration(500);  // animation duration
        animation.setRepeatCount(0);  // animation repeat count
        animation.setRepeatMode(0);   // repeat animation (left to right, right to left )
//      animation.setFillAfter(true);

        img_animation.startAnimation(animation);*/

        AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(500);
        animation1.setStartOffset(100);
        animation1.setFillAfter(true);
        img_animation.startAnimation(animation1);


        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
        public void run() {
            // This method will be executed once the timer is over
            // Start your app main activity
            Intent i = new Intent(SplashScreen.this, ScanActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.down_from_top,R.anim.up_from_bottom);
            // close this activity
            finish();
        }
    }, 500);


    }

}
