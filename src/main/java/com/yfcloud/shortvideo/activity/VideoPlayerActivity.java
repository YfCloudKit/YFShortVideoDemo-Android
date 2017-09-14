package com.yfcloud.shortvideo.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.utils.Const;
import com.yfcloud.shortvideo.widget.YfController;
import com.yunfan.player.widget.YfCloudPlayer;
import com.yunfan.player.widget.YfPlayerKit;

/**
 * 视频点播界面
 */
public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "Yf_VideoPlayerActivity";
    private YfPlayerKit mYfPlayerKit;
    private Button mStartBtn;
    private String mCurrentPath;
    private String mUrl;
    private boolean startPlayback;
    private boolean surfaceCreated;
    private YfController mYfController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_filter);

        mUrl = getIntent().getStringExtra(Const.KEY_VIDEO_PLAY_URL);
        Log.d(TAG, "mUrl: " + mUrl);
//        mUrl = "http://yftest.mp4.yfcdn.net/android.mp4";
        initView();

    }

    public void copyUrl(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
// 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", mUrl);
// 将ClipData内容放到系统剪贴板里。
        cm.setText(mUrl);
        cm.setPrimaryClip(mClipData);

        Toast.makeText(this, "播放地址[" + mUrl + "]已复制至剪贴板", Toast.LENGTH_SHORT).show();

    }

    private void initView() {
        mYfPlayerKit = (YfPlayerKit) findViewById(R.id.surface_view);
        mYfController = (YfController) findViewById(R.id.controller);
        mYfController.setPlayer(mController);
        mYfController.show(0);
        mStartBtn = (Button) findViewById(R.id.start_pause);
        mYfPlayerKit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startPause(mStartBtn);
                }
                return false;
            }
        });
        mYfPlayerKit.setVideoLayout(YfPlayerKit.VIDEO_LAYOUT_FILL_PARENT);
        mYfPlayerKit.setAudioTrackStreamType(AudioManager.STREAM_MUSIC);
        mYfPlayerKit.setBufferSize(3 * 1024 * 1024);
        mYfPlayerKit.setVideoPath(mUrl);
        mYfPlayerKit.setHardwareDecoder(false);
        mYfPlayerKit.setOnPreparedListener(new YfCloudPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(YfCloudPlayer yfCloudPlayer) {
                Log.d(TAG, "onPrepared: ");
                startPause(mStartBtn);
            }
        });
        mYfPlayerKit.setOnErrorListener(new YfCloudPlayer.OnErrorListener() {
            @Override
            public boolean onError(YfCloudPlayer yfCloudPlayer, int i, int i1) {

                return false;
            }
        });
    }

    private YfController.YfControl mController = new YfController.YfControl() {
        @Override
        public int getDuration() {
            return mYfPlayerKit == null ? 0 : mYfPlayerKit.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return mYfPlayerKit == null ? 0 : mYfPlayerKit.getCurrentPosition();
        }

        @Override
        public void seekTo(int pos) {
            if (mYfPlayerKit != null) mYfPlayerKit.seekTo(pos);
        }

        @Override
        public boolean isPlaying() {
            return mYfPlayerKit != null && mYfPlayerKit.isPlaying();
        }

        @Override
        public int getBufferPercentage() {
            return 0;
        }
    };

    public void startPause(View v) {
        Log.d(TAG, "startPause: ");
        if (mYfPlayerKit == null)
            return;
        if (mYfPlayerKit.isPlaying()) {
            pause();
            v.setVisibility(View.VISIBLE);
        } else {
            startPlayback();
            mYfController.show(0);
            v.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mYfPlayerKit.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mYfPlayerKit.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closePlayer();
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
    }

}
