package com.yfcloud.shortvideo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.yfcloud.shortvideo.R;

public class RoundButton extends Button {
    private static final String TAG = RoundButton.class.getSimpleName();
    private static final int WIDTH = 400;
    //    private Rect rect = new Rect(0, 0, WIDTH, WIDTH);
    private Paint paint = new Paint();
    //    private Paint textPaint = new Paint();
    private int deltaX, deltaY;
    private boolean mIsLongTouch;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    public RoundButton(Context context) {
        this(context, null);
    }

    public RoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
//        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
//
//        textPaint.setColor(Color.WHITE);
//        paint.setAntiAlias(true);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_record_start);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        setBackground(getContext().getResources().getDrawable(R.drawable.ic_record));
//        canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "ACTION_DOWN: ");
//                if (!rect.contains(x, y)) {
//                    //没有在矩形上点击，不处理触摸消息
//                    Log.d(TAG, "!contains: ");
//                    return false;
//                }
//                deltaX = x - rect.left;
//                deltaY = y - rect.top;
                Log.d(TAG, "postDelayed: ");
                mHandler.postDelayed(mLongTouchRunnable, 200);
                setBackground(getContext().getResources().getDrawable(R.drawable.ic_publish_video_bg));
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "ACTION_MOVE: ");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP: ");
//                Rect old = new Rect(rect);
//                //更新矩形的位置
////                rect.left = x - deltaX;
////                rect.top = y - deltaY;
////                rect.right = rect.left + WIDTH;
////                rect.bottom = rect.top + WIDTH;
//
//                old.union(rect);//要刷新的区域，求新矩形区域与旧矩形区域的并集
//                invalidate(old);//出于效率考虑，设定脏区域，只进行局部刷新，不是刷新整个view
                mHandler.removeCallbacks(mLongTouchRunnable);
                if (mIsLongTouch && mOnCustomTouchListener != null) {
                    mOnCustomTouchListener.onLongTouchUp();
                    Log.d(TAG, "onLongTouchUp: ");
                }
                setBackground(getContext().getResources().getDrawable(R.drawable.ic_record));
                mIsLongTouch = false;
                break;
        }
        return true;//处理了触摸消息，消息不再传递
    }

    Runnable mLongTouchRunnable = new Runnable() {
        @Override
        public void run() {
            mIsLongTouch = true;
            if (mOnCustomTouchListener != null) {
                mOnCustomTouchListener.onLongTouch();
                Log.d(TAG, "onLongTouch: ");
            }
        }
    };

    Handler mHandler = new Handler();

    public void setOnCustomTouchListener(OnCustomTouchListener onCustomTouchListener) {
        mOnCustomTouchListener = onCustomTouchListener;
    }


    private OnCustomTouchListener mOnCustomTouchListener;

    public interface OnCustomTouchListener {
        void onLongTouchUp();

//        void onTouchDown(int x, int y);

        void onLongTouch();
    }


}