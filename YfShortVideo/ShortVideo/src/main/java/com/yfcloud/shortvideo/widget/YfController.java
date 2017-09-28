package com.yfcloud.shortvideo.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.adapter.VideoEditAdapter;
import com.yfcloud.shortvideo.bean.VideoEditInfo;
import com.yfcloud.shortvideo.utils.Util;


/**
 * Created by xjx on 2016/5/24.
 */
public class YfController extends RelativeLayout {
    private static final String TAG = "YfController";
    //    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    public TextView mCurrentTime, mEndTime, mPublisher_name;
    public YfSectionSeekBar mProgress;

    private boolean mShowing;
    private YfControl mPlayer;
    private int sDefaultTimeout = 3000;
    private boolean mDragging;
    private RecyclerView mRecyclerView;
    private long startTime;
    private VideoEditAdapter videoEditAdapter;

    public YfController(Context context) {
        super(context);
    }

    public YfController(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.widget_yfcontroller, this, true);
        mCurrentTime = (TextView) findViewById(R.id.timestamp_past);
        mRecyclerView = (RecyclerView) findViewById(R.id.id_rv);

        mEndTime = (TextView) findViewById(R.id.timestamp_duration);
        mProgress = (YfSectionSeekBar) findViewById(R.id.seekBar);
        mProgress.setOnSeekBarChangeListener(mSeekListener);
        mProgress.setMax(1000);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        int picWidth = (int) (Util.getDisplayWidth(context) / /*(MAX_RECORD_TIME / 1000 / 2)*/15);
        videoEditAdapter = new VideoEditAdapter(context, picWidth);
        mRecyclerView.setAdapter(videoEditAdapter);

    }

    public void showThumbnails() {
        mRecyclerView.setVisibility(VISIBLE);
        mProgress.enableShowColorBar(true);
    }

    public void hideThumbnails() {
        mRecyclerView.setVisibility(GONE);
        mProgress.enableShowColorBar(false);
    }

    public void reverseThumbnails() {
        videoEditAdapter.reverseItem();
    }

    public void release() {
        for (VideoEditInfo videoEditInfo : videoEditAdapter.getVideoEditInfoList()) {
             Util.deleteFile(videoEditInfo.path);
        }
    }

    public void addThumbnail(VideoEditInfo info) {
        videoEditAdapter.addItemVideoInfo(info);
    }

    public void startDrawSectionBackground(int sign, int color) {
        mProgress.startDrawBackground(sign, color);
    }

    public void stopDrawSectionBackground(int sign) {
        mProgress.stopDrawBackground(sign);
    }

    public void clear() {
        mProgress.clear();
    }

    public void setPlayer(YfControl player) {
        mPlayer = player;
    }


    public void setDefaultTimeOutMs(int timeOut) {
        sDefaultTimeout = timeOut;
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void show(int timeout) {
        if (!mShowing) {
            setProgress();
            setVisibility(VISIBLE);
            mShowing = true;
        }
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
//
//        if (timeout != 0) {
//            mHandler.removeMessages(FADE_OUT);
//            Message msg = mHandler.obtainMessage(FADE_OUT);
//            mHandler.sendMessageDelayed(msg, timeout);
//        }
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            mHandler.removeMessages(SHOW_PROGRESS);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            long newposition = 0;

            long duration = mPlayer.getDuration();
            newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(Util.stringForTime((int) newposition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            show(sDefaultTimeout);

            mHandler.sendEmptyMessage(SHOW_PROGRESS);

        }
    };

//
//    public void hide() {
//        if (mShowing) {
//            try {
//                mHandler.removeMessages(SHOW_PROGRESS);
//                setVisibility(GONE);
//            } catch (IllegalArgumentException ex) {
//                Log.w("MediaController", "already removed");
//            }
//            mShowing = false;
//        }
//    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
//                case FADE_OUT:
//                    hide();
//                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 100 - (pos % 100));
                    }
                    break;
            }
        }
    };

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = 0;
        int duration = 0;
        if (mProgress != null) {

            position = mPlayer.getCurrentPosition();
            duration = mPlayer.getDuration();
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(Util.stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(Util.stringForTime(position));

        return position;
    }


    public interface YfControl {
        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();
    }
}
