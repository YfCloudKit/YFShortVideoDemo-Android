package com.yfcloud.shortvideo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kuaipai.fangyan.core.shooting.jni.RecorderJni;
import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.adapter.VideoEditAdapter;
import com.yfcloud.shortvideo.bean.VideoEditInfo;
import com.yfcloud.shortvideo.utils.CacheActivity;
import com.yfcloud.shortvideo.utils.Const;
import com.yfcloud.shortvideo.utils.ExtractFrameWorkThread;
import com.yfcloud.shortvideo.utils.ExtractVideoInfoUtil;
import com.yfcloud.shortvideo.utils.Util;
import com.yfcloud.shortvideo.widget.VideoCropBar;
import com.yunfan.encoder.widget.YfMediaKit;
import com.yunfan.player.widget.YfCloudPlayer;
import com.yunfan.player.widget.YfPlayerKit;

import java.io.File;
import java.lang.ref.WeakReference;

import static android.R.attr.duration;
import static android.R.attr.y;
import static android.view.View.Y;
import static com.yfcloud.shortvideo.R.id.seekBar;
import static com.yfcloud.shortvideo.activity.VideoFilterActivity.SHOW_IMG_SUCCESS;
import static com.yfcloud.shortvideo.activity.VideoFilterActivity.UPLOAD_PROGRESS;

/**
 * 视频裁剪界面
 */
public class VideoCutActivity extends AppCompatActivity {
    private static final String TAG = "Yf_VideoCutActivity";
    private static final int SPLIT_INDEX = 0x1001;
    private String mMuxVideoPath;
    private YfPlayerKit mYfPlayerKit;
    private boolean startPlayback;
    private TextView mTvStartTime;
    private TextView mTvEndTime;
    private VideoCropBar mVideoCropBar;
    private ExtractVideoInfoUtil mExtractVideoInfoUtil;
    private int thumbnailsCount;
    private ExtractFrameWorkThread mExtractFrameWorkThread;
    private VideoEditAdapter mVideoEditAdapter;
    private int picCount;
    private YfMediaKit mMediaKit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CacheActivity.addActivity(this);
        YfPlayerKit.enableRotation(true);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_cut);
        getIntentData();
        initView();
        initPlayer();
    }

    private void initMediaKit() {
        mMediaKitCallback = new YfMediaKit.MediaKitCallback() {
            @Override
            public void onMediaHandledFinish(int id, int result, final String path) {
                switch (id) {
                    case SPLIT_INDEX:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VideoCutActivity.this, "裁剪成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                        gotoVideoFilterActivity(path);
                        break;
                }
            }
        };
        mMediaKit = new YfMediaKit(mMediaKitCallback);
    }

    private YfMediaKit.MediaKitCallback mMediaKitCallback;

    private void gotoVideoFilterActivity(String path) {
        Log.d(TAG, "gotoVideoFilterActivity: " + path);
        Intent intent = new Intent(VideoCutActivity.this, VideoFilterActivity.class);
        intent.putExtra(Const.KEY_PATH_MUX_VIDEO, path);
        startActivity(intent);
        finish();
    }


    private void initView() {
        mYfPlayerKit = (YfPlayerKit) findViewById(R.id.yf_player_kit);
        mYfPlayerKit = (YfPlayerKit) findViewById(R.id.yf_player_kit);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.id_rv);
        mTvStartTime = (TextView) findViewById(R.id.tv_start_time);
        mTvEndTime = (TextView) findViewById(R.id.tv_end_time);
        Button btnPass = (Button) findViewById(R.id.btn_pass);
        btnPass.setOnClickListener(mOnClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerView.setLayoutManager(linearLayoutManager);
        int picWidth = Util.getDisplayWidth(this) / /*(MAX_RECORD_TIME / 1000 / 2)*/15;
        mVideoEditAdapter = new VideoEditAdapter(this, picWidth);
        recyclerView.setAdapter(mVideoEditAdapter);

        mVideoCropBar = (VideoCropBar) findViewById(R.id.video_crop_bar);
        mVideoCropBar.setSeekBarChangeListener(mSeekBarChangeListener);
//        int minDiff = (int) (cropDuration / (float) mDuration * 100) + 1;
        mVideoCropBar.setProgressMinDiff(mDuration);

        showFrameThumbnails(mMuxVideoPath);
    }

    private void showFrameThumbnails(String path) {
        if (!new File(path).exists()) {
            Log.d(TAG, "视频文件不存在:" + path);
            return;
        }
        if (mExtractVideoInfoUtil == null)
            mExtractVideoInfoUtil = new ExtractVideoInfoUtil(path);
        long endPosition = Long.valueOf(mExtractVideoInfoUtil.getVideoLength());
        long startPosition = 0;
        thumbnailsCount =/*(int) (MAX_RECORD_TIME / 1000 / 2)*/15;//2秒截取一帧
        int extractW = (Util.getDisplayWidth(this)) / 4;
        int extractH = Util.dip2px(this, 55);
        mExtractFrameWorkThread = new ExtractFrameWorkThread(
                extractW, extractH, mUIHandler, path,
                Const.PATH + "/Thumbnails", startPosition, endPosition, thumbnailsCount);
        mExtractFrameWorkThread.start();
    }

    MainHandler mUIHandler = new MainHandler(this);

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        private final WeakReference<VideoCutActivity> mActivity;

        MainHandler(VideoCutActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoCutActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case SHOW_IMG_SUCCESS:
                        if (activity.mVideoEditAdapter != null) {
                            VideoEditInfo info = (VideoEditInfo) msg.obj;
                            activity.mVideoEditAdapter.addItemVideoInfo(info);
                            if (activity.picCount == 0) {
                                Log.d(TAG, "load preview img：" + info.path);
//                                Glide.with(VideoCutActivity.this)
//                                        .load("file://" + info.path)
//                                        .into(mPreview);

                            }
                            activity.picCount++;
                        }
                        break;
                }

            }
        }
    }

    private void initPlayer() {
        mYfPlayerKit.setVideoLayout(YfPlayerKit.VIDEO_LAYOUT_FILL_PARENT);
        mYfPlayerKit.setVideoPath(mMuxVideoPath);
        mYfPlayerKit.setAudioTrackStreamType(AudioManager.STREAM_MUSIC);
        mYfPlayerKit.setOnPreparedListener(new YfCloudPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(YfCloudPlayer yfCloudPlayer) {
                mDuration = mYfPlayerKit.getDuration();
                mTvEndTime.setText((float) mDuration / 1000 + "");
                endTime = mDuration;
                Log.d(TAG, "onPrepared: " + mDuration);
                mYfPlayerKit.start();
            }
        });
        mYfPlayerKit.setOnErrorListener(new YfCloudPlayer.OnErrorListener() {
            @Override
            public boolean onError(YfCloudPlayer yfCloudPlayer, int i, int i1) {
                mVideoCropBar.setSliceBlocked(false);
                return false;
            }
        });
        mYfPlayerKit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startPause();
                }
                return false;
            }
        });
    }

    public void startPause() {
        Log.d(TAG, "startPause: ");
        if (mYfPlayerKit == null)
            return;
        if (mYfPlayerKit.isPlaying()) {
            pause();
        } else {
            startPlayback();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mYfPlayerKit.resume();
        mUIHandler.post(updateProgressRunnable);
        initMediaKit();
    }

    private Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mYfPlayerKit != null) {
                mVideoCropBar.showFrameProgress(true);
                mVideoCropBar.setFrameProgress((float) mYfPlayerKit.getCurrentPosition()
                        / (float) mDuration);
                mUIHandler.postDelayed(this, 40);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mUIHandler.removeCallbacks(updateProgressRunnable);
        mYfPlayerKit.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closePlayer();
        mMediaKitCallback = null;
        mMediaKit = null;
        Util.clearCacheFiles(true);
    }

    private void closePlayer() {
        if (mYfPlayerKit != null) {
            if (mYfPlayerKit.isPlaying())
                mYfPlayerKit.release(true);
        }
    }

    private void startPlayback() {
        startPlayback = true;
//        if (surfaceCreated && prepared) {
        if (mYfPlayerKit != null)
            mYfPlayerKit.start();
//        }
    }

    private void pause() {
        startPlayback = false;
        mYfPlayerKit.pause();
        mVideoCropBar.showFrameProgress(false);
        mVideoCropBar.invalidate();
    }

    private void getIntentData() {
        mMuxVideoPath = getIntent().getStringExtra(Const.KEY_PATH_MUX_VIDEO);
//        mMuxVideoPath = "/sdcard/daytime.mp4";
        Log.d(TAG, "muxVideoPath: " + mMuxVideoPath);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (needCutVideo) {
                int start = (int) startTime / 1000;
                int end = (int) endTime / 1000;
                Log.d(TAG, "onClick: " + start + " " + end);
                mMediaKit.splitMedia(mMuxVideoPath, start, end, mMuxVideoPath.replace(".mp4", "_split.mp4"), SPLIT_INDEX);
            } else {
                gotoVideoFilterActivity(mMuxVideoPath);
            }
        }
    };


    private long startTime;
    private long endTime;
    private int mDuration;
    boolean needCutVideo;
    long seekPos = 0;
    private VideoCropBar.SeekBarChangeListener mSeekBarChangeListener
            = new VideoCropBar.SeekBarChangeListener() {
        @Override
        public void SeekBarValueChanged(float leftThumb, float rightThumb, int whitchSide) {
            Log.d(TAG, "SeekBarValueChanged: " + leftThumb + " ---" + rightThumb + "---" + whitchSide);
            needCutVideo = true;
            if (whitchSide == 0) {
                seekPos = (long) (mDuration * leftThumb / 100);
                startTime = seekPos;
                mTvStartTime.setText((float) startTime / 1000 + "");
            } else if (whitchSide == 1) {
                long seekPos2 = (long) (mDuration * rightThumb / 100);
                endTime = seekPos2;
                mTvEndTime.setText((float) endTime / 1000 + "");
            }
            Log.d(TAG, "croptime: " + startTime + "---" + endTime);
//            dirationTxt.setText((float) (endTime - startTime) / 1000 / 1000 + "");
            mYfPlayerKit.seekTo((int) seekPos);
        }

        @Override
        public void onSeekStart() {
            Log.d(TAG, "onSeekStart: ");
            pause();
        }

        @Override
        public void onSeekEnd() {
            Log.d(TAG, "onSeekEnd: ");
            startPlayback();
        }
    };

}
