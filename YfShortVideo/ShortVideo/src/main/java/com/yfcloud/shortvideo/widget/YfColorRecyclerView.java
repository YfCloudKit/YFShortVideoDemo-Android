package com.yfcloud.shortvideo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.LinkedList;

/**
 * Created by 37917 on 2017/7/29 0029.
 */

public class YfColorRecyclerView extends RecyclerView{
    private static final String TAG = "YfSectionSeekBar";

    private static final int COLOR = 100;
    private static final int START_POSITION = 101;
    private static final int END_POSITION = 102;
    private static final int NOT_END = -10086;
    private LinkedList<SparseIntArray> mSectionBackground = new LinkedList<>();
    public YfColorRecyclerView(Context context) {
        super(context);
    }

    public YfColorRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public YfColorRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface ProgressCallback{
        int getProgress();

        int getMax();
    }
    public ProgressCallback mProgressCallback;
    public void setProgressCallback(ProgressCallback callback){
        this.mProgressCallback=callback;
    }
    public void startDrawBackground(int sign, int color) {
        SparseIntArray array = new SparseIntArray();
//        colorIndex++;
//        if (colorIndex == colors.length) {
//            colorIndex = 0;
//        }
        array.put(COLOR, color);
        array.put(START_POSITION, mProgressCallback.getProgress());
        array.put(END_POSITION, NOT_END);
        mSectionBackground.add(array);
        Log.d(TAG, "startDrawSectionBackground:" + mProgressCallback.getProgress() + "," + mSectionBackground.size());
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }
    }

    public void stopDrawBackground(int sign) {
        mSectionBackground.getLast().put(END_POSITION, mProgressCallback.getProgress());
        Log.d(TAG, "stopDrawSectionBackground:" + mProgressCallback.getProgress() + "," + mSectionBackground.size());
    }

    private Paint mPaint;
    private int sectionEndProgress, sectionStartPosition, sectionEndPosition;

    @Override
    public synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (SparseIntArray array : mSectionBackground) {
            mPaint.setColor(getResources().getColor(array.get(COLOR)));
            sectionEndProgress = array.get(END_POSITION) == NOT_END ? mProgressCallback.getProgress() : array.get(END_POSITION);
            canvas.drawRect((getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / (float) mProgressCallback.getMax() * array.get(START_POSITION) + getPaddingRight(), 0, (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / (float) mProgressCallback.getMax() * sectionEndProgress + getPaddingRight(), getMeasuredHeight(), mPaint);
        }
    }

    public void clear() {
        mSectionBackground.clear();
    }
}
