package com.yfcloud.shortvideo.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.yfcloud.shortvideo.R;

import java.util.Arrays;

public class FocusLayout extends FrameLayout {
    private static final String TAG = FocusLayout.class.getSimpleName();
    private FrameLayout mFocusMarkerContainer;
    private ImageView mFill;
    private SeekBar mSeekBar;
    private int[] mValues;

    public FocusLayout(@NonNull Context context) {
        this(context, null);
    }

    public FocusLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_focus_marker, this);

        mFocusMarkerContainer = (FrameLayout) findViewById(R.id.focusMarkerContainer);
        mFill = (ImageView) findViewById(R.id.fill);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(mListener);
        mFocusMarkerContainer.setAlpha(0);
    }

    private final SeekBar.OnSeekBarChangeListener mListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, "onProgressChanged: " + progress);
            restartAnimation();
            if (mSeekBarChangeListener != null)
                mSeekBarChangeListener.onSeekChanged(seekBar.getProgress() - Math.abs(mValues[0]));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            restartAnimation();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            restartAnimation();

        }

        private void restartAnimation() {
            mFocusMarkerContainer.animate().cancel();
            mFocusMarkerContainer.animate().alpha(0).setStartDelay(3000).setDuration(400).setListener(null).start();
        }
    };

    public void reset() {
        mFocusMarkerContainer.animate().cancel();
        mFocusMarkerContainer.animate().alpha(0).setStartDelay(1).setDuration(1).setListener(null).start();
    }

    public void focus(float mx, float my) {
        mSeekBar.setProgress(mSeekBar.getMax() / 2);
        int x = (int) (mx - mFocusMarkerContainer.getWidth() / 2);
        int y = (int) (my - mFocusMarkerContainer.getWidth() / 2);

        mFocusMarkerContainer.setTranslationX(x);
        mFocusMarkerContainer.setTranslationY(y);

        mFocusMarkerContainer.animate().setListener(null).cancel();
        mFill.animate().setListener(null).cancel();

        mFill.setScaleX(0);
        mFill.setScaleY(0);
        mFill.setAlpha(1f);

        mFocusMarkerContainer.setScaleX(1.36f);
        mFocusMarkerContainer.setScaleY(1.36f);
        mFocusMarkerContainer.setAlpha(1f);

        mFocusMarkerContainer.animate().scaleX(1).scaleY(1).setStartDelay(0).setDuration(330)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFocusMarkerContainer.animate().alpha(0).setStartDelay(3000).setDuration(400).setListener(null).start();
                    }
                }).start();

        mFill.animate().scaleX(1).scaleY(1).setDuration(330)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFill.animate().alpha(0).setDuration(800).setListener(null).start();
                    }
                }).start();

    }

    public void setOnSeekBarChangeListener(SeekBarChangeListener onSeekBarChangeListener) {
        mSeekBarChangeListener = onSeekBarChangeListener;
    }

    SeekBarChangeListener mSeekBarChangeListener;

    public void setExposureCompensationValue(int[] values) {
        mValues = values;
        Log.d(TAG, "setExposureCompensationValue: " + Arrays.toString(values));
        int value = values[1] - values[0];
        mSeekBar.setMax(value);
        mSeekBar.setProgress(value / 2);//居中
    }

    public interface SeekBarChangeListener {
        void onSeekChanged(int value);
    }


}