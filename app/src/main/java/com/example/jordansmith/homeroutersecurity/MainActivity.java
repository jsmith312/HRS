package com.example.jordansmith.homeroutersecurity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.TextView;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

public class MainActivity extends Activity
        implements View.OnTouchListener, View.OnClickListener, SpringListener {
    private static double TENSION = 900;
    private static double DAMPER = 20; //friction
    private static TextView v;
    private ImageView mImageToAnimate;
    private ImageView mImageToAnimate2;
    private SpringSystem mSpringSystem;
    private Spring mSpring;
    private boolean mMovedUp = false;
    private float mOrigY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageToAnimate = (ImageView) findViewById(R.id.router);
        mImageToAnimate.setOnTouchListener(this);

        mSpringSystem = SpringSystem.create();

        mSpring = mSpringSystem.createSpring();
        mSpring.addListener(this);

        SpringConfig config = new SpringConfig(TENSION, DAMPER);
        mSpring.setSpringConfig(config);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mSpring.setEndValue(1f);
                return true;
            case MotionEvent.ACTION_UP:
                mSpring.setEndValue(0f);
                return true;
        }

        return false;
    }

    public void testRouterSecurity() {
        Intent mainIntent = new Intent(MainActivity.this,
                TestActivity.class);
        startActivity(mainIntent);
    }

    @Override
    public void onClick(View v) {
        if (mMovedUp) {
            mSpring.setEndValue(mOrigY);
        } else {
            mOrigY = mImageToAnimate.getY();

            mSpring.setEndValue(mOrigY - 300f);
        }

        mMovedUp = !mMovedUp;
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        float scale = 1f - (value * 0.5f);
        mImageToAnimate.setScaleX(scale);
        mImageToAnimate.setScaleY(scale);

        //mImageToAnimate.setY(value);
    }

    @Override
    public void onSpringAtRest(Spring spring) {
        testRouterSecurity();
    }

    @Override
    public void onSpringActivate(Spring spring) {
    }

    @Override
    public void onSpringEndStateChange(Spring spring) {
    }

}
