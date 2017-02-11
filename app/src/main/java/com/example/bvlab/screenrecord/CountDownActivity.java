package com.example.bvlab.screenrecord;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import animation.CountDownAnimation;

public class CountDownActivity extends AppCompatActivity implements CountDownAnimation.CountDownListener {
    CountDownAnimation countDownAnimation;
    TextView mCountDownText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);
        mCountDownText = (TextView) findViewById(R.id.textView);

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        try {
            FloatingView.windowManager.removeView(FloatingView.view);
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e("error", e.toString());
        }

        initCountDownAnimation();

        startCountDownAnimation();

    }

    private void initCountDownAnimation() {
        countDownAnimation = new CountDownAnimation(mCountDownText, getStartCount());
        countDownAnimation.setCountDownListener(this);
    }

    private void startCountDownAnimation() {

        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        countDownAnimation.setAnimation(animationSet);
        // Customizable start count
        countDownAnimation.setStartCount(getStartCount());

        countDownAnimation.start();
    }

    private void cancelCountDownAnimation() {
        countDownAnimation.cancel();
    }

    private int getStartCount() {
        return 5;
    }

    @Override
    public void onCountDownEnd(CountDownAnimation animation) {

        finish();

        Intent recordIntent = new Intent(getBaseContext(), RecordScreenActivity.class);
        recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(recordIntent);
    }
}
