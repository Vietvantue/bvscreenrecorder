package com.example.bvlab.screenrecord.animation;

/**
 * Created by VietMac on 2017-02-08.
 */

import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

/**
 * Defines a count down com.example.bvlab.screenrecord.animation to be shown on a {@link TextView }.
 */
public class CountDownAnimation {

    private TextView mTextView;
    private Animation mAnimation;
    private int mStartCount;
    private int mCurrentCount;
    private CountDownListener mListener;

    private Handler mHandler = new Handler();

    private final Runnable mCountDown = new Runnable() {
        public void run() {
            if (mCurrentCount > 0) {
                mTextView.setText(mCurrentCount + "");
                mTextView.startAnimation(mAnimation);
                mCurrentCount--;
            } else {
                mTextView.setVisibility(View.GONE);
                if (mListener != null)
                    mListener.onCountDownEnd(CountDownAnimation.this);
            }
        }
    };

    /**
     * <p>
     * Creates a count down com.example.bvlab.screenrecord.animation in the <var>textView</var>, starting from
     * <var>startCount</var>.
     * </p>
     * <p>
     * By default, the class defines a fade out com.example.bvlab.screenrecord.animation, which uses
     * {@link AlphaAnimation } from 1 to 0.
     * </p>
     *
     * @param textView   The view where the count down will be shown
     * @param startCount The starting count number
     */
    public CountDownAnimation(TextView textView, int startCount) {
        this.mTextView = textView;
        this.mStartCount = startCount;

        mAnimation = new AlphaAnimation(1.0f, 0.0f);
        mAnimation.setDuration(800);
    }

    /**
     * Starts the count down com.example.bvlab.screenrecord.animation.
     */
    public void start() {
        mHandler.removeCallbacks(mCountDown);

        mTextView.setText(mStartCount + "");
        mTextView.setVisibility(View.VISIBLE);

        mCurrentCount = mStartCount;

        mHandler.post(mCountDown);
        for (int i = 1; i <= mStartCount; i++) {
            mHandler.postDelayed(mCountDown, i * 800);
        }
    }

    /**
     * Cancels the count down com.example.bvlab.screenrecord.animation.
     */
    public void cancel() {
        mHandler.removeCallbacks(mCountDown);

        mTextView.setText("");
        mTextView.setVisibility(View.GONE);
    }

    /**
     * Sets the com.example.bvlab.screenrecord.animation used during the count down. If the duration of the
     * com.example.bvlab.screenrecord.animation for each number is not set, one second will be defined.
     */
    public void setAnimation(Animation animation) {
        this.mAnimation = animation;
        if (mAnimation.getDuration() == 0)
            mAnimation.setDuration(800);
    }

    /**
     * Returns the com.example.bvlab.screenrecord.animation used during the count down.
     */
    public Animation getAnimation() {
        return mAnimation;
    }

    /**
     * Sets a new starting count number for the count down com.example.bvlab.screenrecord.animation.
     *
     * @param startCount The starting count number
     */
    public void setStartCount(int startCount) {
        this.mStartCount = startCount;
    }

    /**
     * Returns the starting count number for the count down com.example.bvlab.screenrecord.animation.
     */
    public int getStartCount() {
        return mStartCount;
    }

    /**
     * Binds a listener to this count down com.example.bvlab.screenrecord.animation. The count down listener is
     * notified of events such as the end of the com.example.bvlab.screenrecord.animation.
     *
     * @param listener The count down listener to be notified
     */
    public void setCountDownListener(CountDownListener listener) {
        mListener = listener;
    }

    /**
     * A count down listener receives notifications from a count down com.example.bvlab.screenrecord.animation.
     * Notifications indicate count down com.example.bvlab.screenrecord.animation related events, such as the
     * end of the com.example.bvlab.screenrecord.animation.
     */
    public interface CountDownListener {
        /**
         * Notifies the end of the count down com.example.bvlab.screenrecord.animation.
         *
         * @param animation The count down com.example.bvlab.screenrecord.animation which reached its end.
         */
        void onCountDownEnd(CountDownAnimation animation);
    }
}
