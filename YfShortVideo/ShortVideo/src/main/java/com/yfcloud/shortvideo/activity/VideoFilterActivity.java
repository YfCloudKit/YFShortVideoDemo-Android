package com.yfcloud.shortvideo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kuaipai.fangyan.core.shooting.jni.RecorderJni;
import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.bean.VideoEditInfo;
import com.yfcloud.shortvideo.http.OkHttpHelper;
import com.yfcloud.shortvideo.http.Server;
import com.yfcloud.shortvideo.utils.CacheActivity;
import com.yfcloud.shortvideo.utils.Configure;
import com.yfcloud.shortvideo.utils.Const;
import com.yfcloud.shortvideo.utils.ExtractFrameWorkThread;
import com.yfcloud.shortvideo.utils.ExtractVideoInfoUtil;
import com.yfcloud.shortvideo.utils.FilterFactory;
import com.yfcloud.shortvideo.utils.Util;
import com.yfcloud.shortvideo.widget.YfController;
import com.yfcloud.shortvideo.widget.YfPopupWindow;
import com.yunfan.encoder.filter.BaseFilter;
import com.yunfan.encoder.filter.YfBlackMagicFilter;
import com.yunfan.encoder.filter.YfGifFilter;
import com.yunfan.encoder.filter.YfMirrorFilter;
import com.yunfan.encoder.filter.YfMultiWindowFilter;
import com.yunfan.encoder.filter.YfShakeFilter;
import com.yunfan.encoder.filter.YfSoulDazzleFilter;
import com.yunfan.encoder.filter.YfTartanFilter;
import com.yunfan.encoder.filter.YfWaterMarkFilter;
import com.yunfan.encoder.filter.YfWaveFilter;
import com.yunfan.encoder.filter.entity.TimeSection;
import com.yunfan.encoder.style.YfAntiqueFilter;
import com.yunfan.encoder.style.YfWhiteCatFilter;
import com.yunfan.encoder.widget.YfGlSurfaceView;
import com.yunfan.encoder.widget.YfKitFactory;
import com.yunfan.encoder.widget.YfMediaEditor;
import com.yunfan.encoder.widget.YfMediaKit;
import com.yunfan.player.widget.YfCloudPlayer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 视频添加特效界面
 */
public class VideoFilterActivity extends AppCompatActivity implements YfController.YfControl, View.OnClickListener {
    private final String TAG = "Yf_VideoPlayActivity";
    private YfGlSurfaceView mYfGlSurfaceView;
    private YfCloudPlayer mYfCloudPlayer;
    private String mCurrentPath, mFlashbackPath, mNormalPath;
    public static final String BITRATE = "bitrate";
    private YfWaterMarkFilter mWaterMarkFilter;
    private YfGifFilter mGifFilter;


    private ExtractVideoInfoUtil mExtractVideoInfoUtil;
    private ExtractFrameWorkThread mExtractFrameWorkThread;
    private FrameLayout mSurfaceLayout;
    private Button mStartBtn;
    private YfController mController;
    private Button mFilterBtn;
    private Button mLogoBtn;
    private String[] /*mArItems, mArNames, */mFilterItems, mFilterNames;
    private YfPopupWindow/* mArPopupWindow, */mFilterPopupWindow;
    private TextView mUploadHint;
    private TextView mOutputBtn;
    private ImageView mCover;
    private YfWhiteCatFilter mYfWhiteCatFilter;
    private YfAntiqueFilter mYfAntiqueFilter;
    private YfTartanFilter mYfTartanFilter;
    private YfWaveFilter mYfWaveFilter;
    private YfSoulDazzleFilter mYfSoulDazzleFilter;
    private YfMultiWindowFilter mYfMultiWindowFilter;
    private long mDuration;
    private LinearLayout mLlBottom;
    private String mFilterVideoPath = Const.PATH_RECORD + "/testVideo-" + Util.getFormatTime() + ".mp4";
    private String mInputTrailerPath = Const.PATH_RECORD + "/inputTrailer.mp4";
    private int mBitrate;
    private YfPopupWindow mLogoPopupWindow;
    //    private PopupWindow mUploadPopupWindow;
//    private View popContentView;
    private ImageView mUploadAnim;
    private Animation anim;
    private String[] mLogoItems;
    private Button mGifBtn, mMenu;
    private String[] mGifItems;
    private YfPopupWindow mGifPopupWindow;
    private int mFrameRate;
    private String mPlayUrl;
    private YfMediaEditor mYfEditor;
    private boolean mHevcEncoder;
    private Button mBtnReverse;
    private String[] mTimeEffectItems;
    private YfPopupWindow mTimeEffectPopupwindow;
    private Button mBtnSave;
    private RelativeLayout mRlSurface;
    private int mGifIndex;
    private int mLogoIndex;
    private YfMirrorFilter mYfMirrorFilter;
    private YfBlackMagicFilter mYfEdgeFilter;
    private YfShakeFilter mYfShakeFilter;
    private int mVideoWidth;
    private int mVideoHeight;
    private RelativeLayout mRlUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_play);
        mSurfaceLayout = (FrameLayout) findViewById(R.id.preview_layout);
        mRlSurface = (RelativeLayout) findViewById(R.id.rl_surface);
        mYfGlSurfaceView = (YfGlSurfaceView) findViewById(R.id.yf_surface);

        mLlBottom = (LinearLayout) findViewById(R.id.recorder_bottom);
        mStartBtn = (Button) findViewById(R.id.start_pause);
        mOutputBtn = (TextView) findViewById(R.id.publish);
        mFilterBtn = (Button) findViewById(R.id.btn_filter);
        mLogoBtn = (Button) findViewById(R.id.btn_logo);
        mGifBtn = (Button) findViewById(R.id.btn_gif);
        mBtnReverse = (Button) findViewById(R.id.btn_reverse);
        mCover = (ImageView) findViewById(R.id.preview);
        mMenu = (Button) findViewById(R.id.menu);
        mBtnSave = (Button) findViewById(R.id.btn_save);

        mRlUpload = (RelativeLayout) findViewById(R.id.rl_upload);
//        mUploadPopupWindow = new PopupWindow(popContentView,
//                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);
        mUploadAnim = (ImageView) findViewById(R.id.save_run);
        mUploadHint = (TextView) findViewById(R.id.hint);

        mFilterBtn.setOnClickListener(this);
        mLogoBtn.setOnClickListener(this);
        mGifBtn.setOnClickListener(this);
        mOutputBtn.setOnClickListener(this);
        mMenu.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnReverse.setOnClickListener(this);

        mController = (YfController) findViewById(R.id.controller);
        mController.setPlayer(this);
        mController.show(0);


        mLandscape = false;
        mYfGlSurfaceView.init();
        mYfGlSurfaceView.setYfRenderCallback(mRenderCallback);
        mYfGlSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startPause(mStartBtn);
                }
                return false;
            }
        });
        RecorderJni.getInstance();
        mBitrate = Configure.BITRATE;
        mFrameRate = Configure.FRAME_RATE;
        mHevcEncoder = Configure.HEVC_ENCODER;

        mNormalPath = mCurrentPath = getIntent().getStringExtra(Const.KEY_PATH_MUX_VIDEO);
        if (mNormalPath == null)
            mNormalPath = mCurrentPath = "/sdcard/daytime.mp4";
        mFlashbackPath = mCurrentPath.replace(".mp4", "_reverse.mp4");
        showFrameThumbnails(mCurrentPath);
        try {
            mYfEditor = new YfKitFactory.Factory(this).buildYfMediaEditor();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "IllegalAccessException: " + e.getMessage());
        }
        Log.d(TAG, "mYfEditor: " + mYfEditor);

        try {
            mFactory = new FilterFactory(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        initMediaKit();
        if (!new File(mFlashbackPath).exists())
            reverseMedia();
        if (!new File(mInputTrailerPath).exists())//预先复制片尾到SDcard
            Util.CopyAssets(VideoFilterActivity.this, "368-640.mp4", mInputTrailerPath);
    }


    private void initMediaKit() {
        if (mMediaKit == null) mMediaKit = new YfMediaKit(mMediaKitCallback);
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

    private final MainHandler mUIHandler = new MainHandler(this);
    private int thumbnailsCount, picCount;
    public static final int SHOW_IMG_SUCCESS = 0;
    public static final int UPLOAD_PROGRESS = 1;

    @Override
    public int getDuration() {
        return mYfCloudPlayer == null ? 0 : (int) mYfCloudPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mYfCloudPlayer == null ? 0 : (int) mYfCloudPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        if (mYfCloudPlayer != null) mYfCloudPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mYfCloudPlayer != null && mYfCloudPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_filter:
                showFilterPopupWindow();
                break;
            case R.id.btn_logo:
                showLogoPopupWindow();
                break;
            case R.id.btn_gif:
                showGifPopupWindow();
                break;
            case R.id.btn_reverse:
                showTimeEffectPopupWindow();
                break;
            case R.id.publish:
                if (mFlashback && mReversing) {
                    Toast.makeText(VideoFilterActivity.this, "时光倒流中，请稍后重试...", Toast.LENGTH_SHORT).show();
                    return;
                }
                mYfCloudPlayer.pause();
                hideControlUI(true);
                showUploadPopupWindow();

                List<BaseFilter> configFilterList = new ArrayList<>();
                for (BaseFilter filter : mPreviewFilters) {
                    BaseFilter configFilter = getConfigFilter(filter.getIndex());
                    if (configFilter != null) {
                        configFilter.setRenderSections(filter.getRenderSections());
                        configFilterList.add(configFilter);
                    }

                }
                if (mAddGif && mGifIndex != -1) {
                    configFilterList.add(mFactory.createGifFilter(this, GIF_INDEX, mYfCloudPlayer.getVideoWidth(), gifFileNames[mGifIndex]));
                }
                if (mAddLogo && mLogoIndex != -1) {
                    configFilterList.add(mFactory.createWaterMarkFilter(this, LOGO_INDEX, mYfCloudPlayer.getVideoWidth(), logos[mLogoIndex]));
                }
                mYfEditor.addFilters(configFilterList);
                mYfEditor.enableHighQuality(Configure.HIGH_QUALITY);
                mYfEditor.setSize(mYfCloudPlayer.getVideoWidth(), mYfCloudPlayer.getVideoHeight());
                mYfEditor.setBitrate(mBitrate);
                mYfEditor.encodeHEVC(mHevcEncoder);//输出H265视频
                mYfEditor.setSaveProgressListener(mDuration, mProgressListener);
                try {
                    mYfEditor.startEdit(this, mCurrentPath, mFilterVideoPath);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                break;
            case R.id.btn_save:
            case R.id.menu:
//                mSurfaceLayout.startAnimation(animation);
//                mYfGlSurfaceView.startAnimation(animation);
                toggleEditContent(mLlBottom.getVisibility() == View.GONE);
                break;
            default:
                break;
        }
    }


    private Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            toggleEditContent(mLlBottom.getVisibility() == View.GONE);

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private YfMediaEditor.SaveProgressListener mProgressListener
            = new YfMediaEditor.SaveProgressListener() {
        @Override
        public void onSaveProgress(int progress) {
            mUploadHint.setText("视频处理中" + progress / 2 + "%...");
            if (progress == 100) {
                Toast.makeText(VideoFilterActivity.this, "文件保存在" + mFilterVideoPath, Toast.LENGTH_LONG).show();
                insertToAlbum();
                Server.POST_UPLOAD_FILE(new File(mFilterVideoPath),
                        mFilterVideoPath.substring(mFilterVideoPath.lastIndexOf("/") + 1),
                        mUploadCallback, mUploadListener);
            }
        }
    };

    private void insertToAlbum() {
        boolean insert = Util.insertVideoToMediaStore(VideoFilterActivity.this, mFilterVideoPath,
                System.currentTimeMillis(), mVideoWidth, mVideoHeight, mDuration);
        if (insert) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(mFilterVideoPath));
            intent.setData(uri);
            sendBroadcast(intent);
        }
    }


    private void showUploadPopupWindow() {
//        popContentView = LayoutInflater.from(this).inflate(
//                R.layout.upload_window, null);
        mRlUpload.setVisibility(View.VISIBLE);
//        mUploadPopupWindow = new PopupWindow(popContentView,
//                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);
        anim = new RotateAnimation(360, 0,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(1000);
        anim.setInterpolator(new LinearInterpolator());

        mUploadAnim.startAnimation(anim);

//        mUploadPopupWindow.setFocusable(true);
//        mUploadPopupWindow.showAtLocation(mFilterBtn, Gravity.CENTER, 0, 0);
    }

    private void toggleEditContent(boolean show) {
        if (show) {
            mBtnSave.setVisibility(View.VISIBLE);
            mMenu.setVisibility(View.GONE);
            mController.showThumbnails();
            mLlBottom.setVisibility(View.VISIBLE);
            mOutputBtn.setVisibility(View.GONE);
        } else {
            mMenu.setVisibility(View.VISIBLE);
            mBtnSave.setVisibility(View.GONE);
            mController.hideThumbnails();
            mLlBottom.setVisibility(View.GONE);
            mOutputBtn.setVisibility(View.VISIBLE);
        }
    }

    private Callback mUploadCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "onFailure:" + call.toString() + ",io exception:" + e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoFilterActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                    onUploadFinish();
                }
            });
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Log.d(TAG, "onResponse:" + response.toString() + ",response.body.toString:" + response.body().string() + ",response.message:" + response.message());
            mUIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onUploadFinish();
                }
            }, 1000);
        }
    };
    private OkHttpHelper.ProgressListener mUploadListener = new OkHttpHelper.ProgressListener() {
        @Override
        public void onProgress(long totalBytes, long remainingBytes, boolean done) {
            int percent = (int) ((totalBytes - remainingBytes) * 100 / totalBytes);
            if (percent % 5 == 0) {
                Message msg = mUIHandler.obtainMessage(UPLOAD_PROGRESS);
                msg.arg1 = percent;
                mUIHandler.sendMessage(msg);
            }
        }
    };

    private boolean preLoading;

    private void onUploadFinish() {
        hideControlUI(false);
        mPreviewFilters.clear();
        if (mAddGif) {
            mYfGlSurfaceView.removeFilter(GIF_INDEX);
            mAddGif = false;
        }
        if (mAddLogo) {
            mYfGlSurfaceView.removeFilter(LOGO_INDEX);
            mAddLogo = false;
        }
        preLoading = true;
        mPlayUrl = Const.PATH_PLAY + mFilterVideoPath.substring(mFilterVideoPath.lastIndexOf("/") + 1);
        Log.d(TAG, "mPlayUrl: " + mPlayUrl);
        openVideo(mPlayUrl);
    }

    private void onDone() {
        //// FIXME: 2017/7/27 0027 在这里打开新界面 开始播放视频
        mOutputBtn.setText("上传完成..." + 100 + "%");

        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(Const.KEY_VIDEO_PLAY_URL, mPlayUrl);
        startActivity(intent);
        CacheActivity.finishActivity();
        finish();
    }

    private final int TRAILER_INDEX = 888;

    private YfMediaKit.MediaKitCallback mMediaKitCallback = new YfMediaKit.MediaKitCallback() {
        @Override
        public void onMediaHandledFinish(int id, int result, final String path) {
            switch (id) {
                case TRAILER_INDEX:


                    break;
                case REVERSE_INDEX:
                    mReversing = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mFlashback) {
                                isPlayingFlashBack = true;
                                openVideo(path);
                                mController.reverseThumbnails();
                                Toast.makeText(VideoFilterActivity.this, "时光倒流成功，开始播放", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    break;
            }
        }
    };

    private void hideControlUI(boolean hide) {
        if (hide) {
            mOutputBtn.setText("准备中...");
            mOutputBtn.setEnabled(false);
        } else {
            mOutputBtn.setVisibility(View.GONE);
            mOutputBtn.setText("保存");
            mController.clear();
            mPreviewFilters.clear();
        }
        mLlBottom.setVisibility(View.GONE);
    }

    AlertDialog.Builder mMoreMenu;
    private boolean mAddLogo, /*mAddTrailer,*/
            mFlashback, mSlowVideo, mRepeatVideo, mAddGif;
    private boolean isPlayingFlashBack, isAddedLogo, isAddedGif;

    private void showMoreMenu() {
        if (mMoreMenu == null) {
            mMoreMenu = new AlertDialog.Builder(this)
                    .setTitle("更多编辑")
                    .setMultiChoiceItems(new String[]{"水印", /*"添加片尾",*/ "时光倒流", "添加动图"},
                            new boolean[]{mAddLogo, /*mAddTrailer, */mFlashback, mAddGif},
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    switch (which) {
                                        case 0:
                                            mAddLogo = isChecked;
                                            break;
                                        case 1:
                                            mFlashback = isChecked;
                                            break;
                                        case 2:
                                            mAddGif = isChecked;
                                            break;

                                    }
                                }
                            })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isAddedLogo && mAddLogo) {//添加水印
                                addLogo(R.mipmap.logo);
                                isAddedLogo = true;
                            } else if (isAddedLogo && !mAddLogo) {
                                mYfGlSurfaceView.removeFilter(LOGO_INDEX);
                                isAddedLogo = false;
                            }

                            if (!isAddedGif && mAddGif) {//添加gif
                                addGif(gifFileNames[0]);
                                isAddedGif = true;
                            } else if (isAddedGif && !mAddGif) {
                                mYfGlSurfaceView.removeFilter(GIF_INDEX);
                                isAddedGif = false;
                            }

                            if (mFlashback && !isPlayingFlashBack) {//时光倒流
                                if (new File(mFlashbackPath).exists()) {
                                    isPlayingFlashBack = true;
                                    mController.reverseThumbnails();
                                    openVideo(mFlashbackPath);
                                } else {
                                    reverseMedia();
                                    Toast.makeText(VideoFilterActivity.this, "开始时光倒流，倒流成功会立即播放，请稍候", Toast.LENGTH_SHORT).show();
                                }
                            } else if (!mFlashback && isPlayingFlashBack) {
                                isPlayingFlashBack = false;
                                mController.reverseThumbnails();
                                openVideo(mNormalPath);
                            }
                        }
                    });

        }
        mMoreMenu.show();

    }

    YfMediaKit mMediaKit;
    private static final int REVERSE_INDEX = 101;
    private boolean mReversing;

    private void reverseMedia() {
        if (!mReversing) {
            mReversing = true;
            mMediaKit.reverseMedia(mNormalPath, mFlashbackPath, REVERSE_INDEX);
        }
    }

    private void showFilterPopupWindow() {
        mFilterItems = getResources().getStringArray(R.array.filter_name);
        if (mFilterPopupWindow == null) {
            mFilterPopupWindow = new YfPopupWindow(this, mFilterItems, YfPopupWindow.TYPE_FILTER, true);
            mFilterPopupWindow.setOnPressedListener(mOnFilterChangeListener);
        }
        mFilterPopupWindow.showAtLocation(mFilterBtn, Gravity.BOTTOM, 0, 0);
    }

    private int[] logos = new int[]{R.mipmap.logo, R.drawable.watermark_1,
            R.drawable.watermark_2, R.drawable.watermark_6, R.drawable.watermark_7};

    private void showLogoPopupWindow() {
        mLogoItems = getResources().getStringArray(R.array.logo_name);
        if (mLogoPopupWindow == null) {
            mLogoPopupWindow = new YfPopupWindow(this, mLogoItems, YfPopupWindow.TYPE_LOGO, true);
            mLogoPopupWindow.setOnSelectedListener(new YfPopupWindow.OnSelectedListener() {
                @Override
                public void onSelected(int position) {
                    Log.d(TAG, "onSelected: " + position);
                    mLogoIndex = position - 1;
                    mAddLogo = position != 0;
                    if (position == 0) {
                        removeFilter(LOGO_INDEX);
                        mWaterMarkFilter = null;
                    } else {
                        removeFilter(LOGO_INDEX);
                        mWaterMarkFilter = null;
                        addLogo(logos[position - 1]);
                        startPlayWhenAddEffect();
                    }
                }
            });
        }
        mLogoPopupWindow.showAtLocation(mFilterBtn, Gravity.BOTTOM, 0, 0);
    }

    private String[] gifFileNames = {"0001.gif", "0002.gif", "0003.gif", "0004.gif", "0005.gif"};

    private void showGifPopupWindow() {
        mGifItems = getResources().getStringArray(R.array.gif_name);
        if (mGifPopupWindow == null) {
            mGifPopupWindow = new YfPopupWindow(this, mGifItems, YfPopupWindow.TYPE_GIF, true);
            mGifPopupWindow.setOnSelectedListener(new YfPopupWindow.OnSelectedListener() {
                @Override
                public void onSelected(int position) {
                    Log.d(TAG, "onSelected: " + position);
                    mGifIndex = position - 1;
                    mAddGif = position != 0;
                    if (position == 0) {
                        removeFilter(GIF_INDEX);
                        mGifFilter = null;
                    } else {
                        removeFilter(GIF_INDEX);
                        mGifFilter = null;
                        addGif(gifFileNames[position - 1]);
                        startPlayWhenAddEffect();
                    }
                }
            });
        }
        mGifPopupWindow.showAtLocation(mFilterBtn, Gravity.BOTTOM, 0, 0);
    }

    private void showTimeEffectPopupWindow() {
        mTimeEffectItems = getResources().getStringArray(R.array.time_effect_name);
        if (mTimeEffectPopupwindow == null) {
            mTimeEffectPopupwindow = new YfPopupWindow(this, mTimeEffectItems, YfPopupWindow.TYPE_TIME, true);
            mTimeEffectPopupwindow.setOnSelectedListener(new YfPopupWindow.OnSelectedListener() {
                @Override
                public void onSelected(int position) {
                    Log.d(TAG, "onSelected: " + position);
                    if (position == 0) {
                        mFlashback = mSlowVideo = mRepeatVideo = false;
                    }
                    mYfEditor.reset();
                    mFlashback = position == 1;
                    if (mFlashback && !isPlayingFlashBack) {//时光倒流
                        if (new File(mFlashbackPath).exists()) {
                            isPlayingFlashBack = true;
                            mController.reverseThumbnails();
                            openVideo(mFlashbackPath);
                        } else {
                            reverseMedia();
                            Toast.makeText(VideoFilterActivity.this, "开始时光倒流，倒流成功会立即播放，请稍候", Toast.LENGTH_SHORT).show();
                        }
                    } else if (!mFlashback && isPlayingFlashBack) {
                        isPlayingFlashBack = false;
                        mController.reverseThumbnails();
                        openVideo(mNormalPath);
                    }

                    Log.d(TAG, "startPosition: " + mYfCloudPlayer.getCurrentPosition());
                    mSlowVideo = position == 2;
                    if (mSlowVideo) {
                        if (mYfCloudPlayer != null) {
                            long startPosition = mYfCloudPlayer.getCurrentPosition();
                            mYfEditor.changeVideoSpeed(startPosition, 2000, 0.5);
                            Log.d(TAG, "slow video speed:" + startPosition);
                            mYfCloudPlayer.setVideoSpeed(startPosition, 2000, 0.5);
                        }
                    } else {
                        if (mYfCloudPlayer != null)
                            mYfCloudPlayer.setVideoSpeed(Double.NaN, Double.NaN, Double.NaN);
                    }

                    mRepeatVideo = position == 3;
                    if (mRepeatVideo) {
                        if (mYfCloudPlayer != null) {
                            long startPosition = mYfCloudPlayer.getCurrentPosition();
                            mYfEditor.repeatVideo(startPosition, 2000, 6);
                            Log.d(TAG, "repeatVideo:" + startPosition);
                            mYfCloudPlayer.setVideoRepeat(startPosition, 2000, 6);
                        }
                    } else {
                        if (mYfCloudPlayer != null)
                            mYfCloudPlayer.setVideoRepeat(Double.NaN, Double.NaN, 0);
                    }


                }
            });
        }
        mTimeEffectPopupwindow.showAtLocation(mFilterBtn, Gravity.BOTTOM, 0, 0);
    }

    private int[] colors = new int[]{R.color.effectBlue, R.color.effectYellow, R.color.effectGreen,
            R.color.red, R.color.effectWhite, R.color.effectBgGray, R.color.effectBlack,
            R.color.effectOrange, R.color.effectSkyBlue};
    private YfPopupWindow.OnPressedListener mOnFilterChangeListener = new YfPopupWindow.OnPressedListener() {
        @Override
        public void onPressed(int position, boolean pressed) {
            //这里使各滤镜的index刚好等于对应的position
            Log.d(TAG, "onFilterChanged: " + position + "," + pressed);
            BaseFilter filter = getPressedFilter(position);
            if (filter == null) return;
            if (pressed) {
                addFilter(filter, position);
                mController.startDrawSectionBackground(position, colors[position]);
                startPlayWhenAddEffect();
            } else {
                generateConfig(filter);
                removeFilter(position);
                mController.stopDrawSectionBackground(position);
            }
        }
    };

    private void startPlayWhenAddEffect() {
        if (mYfCloudPlayer != null && !mYfCloudPlayer.isPlaying()) {
            mCover.setVisibility(View.GONE);
            startPlayback();
            mController.show(0);
            mStartBtn.setVisibility(View.GONE);
        }
    }


    List<BaseFilter> mPreviewFilters = new ArrayList<>();

    private void generateConfig(BaseFilter filter) {
        int newFilterIndex = filter.getIndex();
        long newFilterStartTime = (long) filter.getTag();
        long newFilterEndTime = mYfCloudPlayer.getFrameTimestamp();
//
//        //===== 每次加特效都要判断是否需要删除原来的特效,以避免特效重叠 =================
//        //目前所加的所有特效时间区间，通过timeSection.getFilterIndex()知道当前是什么特效
//        ArrayList<TimeSection> timeSections = new ArrayList<>();
//        //放在同一个list中
//        for (BaseFilter previewFilter : mPreviewFilters) {
//            for (TimeSection timeSection : previewFilter.getRenderSections()) {
//                timeSections.add(timeSection);//这里的时间
//            }
//        }
//        //排序
//        Collections.sort(timeSections, new Comparator<TimeSection>() {
//            @Override
//            public int compare(TimeSection o1, TimeSection o2) {
//                return (int) (o1.getStart() - o2.getStart());
//            }
//        });
//
//
//        List<TimeSection> splitTimeSections = new ArrayList<>();
//        List<TimeSection> needRemoveSections = new ArrayList<>();
//        //遍历，判断几种情况
//        for (TimeSection timeSection : timeSections) {
//            //如果新加入的特效起始时间在已有某特效区间内
//            if (newFilterStartTime >= timeSection.getStart() && newFilterStartTime <= timeSection.getEnd()) {
//                if (newFilterEndTime <= timeSection.getEnd()) {
//                    //如果是被包含于某特效区间内
//                    if (newFilterIndex==timeSection.getFilterIndex()) {
//                        //do nothing
//                    }else {
//                        //拆分区间
//                        timeSection.setEnd(newFilterStartTime);//拆分原有特效区间
//                        splitTimeSections.add(new TimeSection(newFilterEndTime, timeSection.getEnd(),
//                                timeSection.getFilterIndex(), timeSection.getTimestamp()));
//                    }
//                    break;//
//                }else {//交叉关系
//                    //遍历下一个
//                    continue;
//                }
//
//            }else if (newFilterStartTime<=timeSection.getStart() && newFilterStartTime >= timeSection.getEnd()){
//
//            }
//
//
//
//        }
//
//        //=================判断完毕=====================

        //该方法已对所有区间做了排序和合并处理
        filter.addRenderSectionPts(newFilterStartTime, newFilterEndTime);
        if (!mPreviewFilters.contains(filter)) {
            mPreviewFilters.add(filter);
        }

        Log.d(TAG, "filters size:" + mPreviewFilters);
    }


    private boolean mLandscape;
    private int mCurrentFilterIndex = -1;
    private BaseFilter mCurrentFilter;
    private int sectionIndex;
    private final int BEAUTY_INDEX = 111, LOGO_INDEX = 222, FACE_INDEX = 333, GIF_INDEX = 444,
    /* WHITE_CAT_INDEX = 0, ANTIQUE_INDEX = 1,*/ WAVE_INDEX = 0, DAZZLE_INDEX = 1, NINE_WINDOW = 2,
            TARTAN_INDEX = 3, MIRROR_INDEX = 4, EDGE_INDEX = 5, SHAKE_INDEX = 6;
    private boolean setBeauty, setLogo, setAr, setCrayon, setSketch;

    private void addFilter(BaseFilter filter, int index) {
        filter.setIndex(index);
        filter.setTag(mYfCloudPlayer.getFrameTimestamp());//记录起始时间
        mYfGlSurfaceView.addFilter(filter);
        mCurrentFilterIndex = index;
        mCurrentFilter = filter;
    }

    private void addGif(String gifFileName) {
        Log.d(TAG, "addGif: " + gifFileName);
        if (mGifFilter == null) {
            mGifFilter = mFactory.createGifFilter(this, GIF_INDEX, mYfGlSurfaceView.getMeasuredWidth(), gifFileName);
        }
        mYfGlSurfaceView.addFilter(mGifFilter);
    }

    private void addLogo(int logo) {
        if (mWaterMarkFilter == null) {
            mWaterMarkFilter = mFactory.createWaterMarkFilter(this, LOGO_INDEX, mYfGlSurfaceView.getMeasuredWidth(), logo);
        }
        mYfGlSurfaceView.addFilter(mWaterMarkFilter);
    }

    FilterFactory mFactory;

    /**
     * 初始化预览界面用的、长按生效的滤镜
     *
     * @param filterIndex
     */
    private BaseFilter getPressedFilter(int filterIndex) {
        switch (filterIndex) {
//            case ANTIQUE_INDEX:
//                if (mYfAntiqueFilter == null)
//                    mYfAntiqueFilter = FilterFactory.createAntiqueFilter(filterIndex);
//                return mYfAntiqueFilter;
//            case WHITE_CAT_INDEX:
//                if (mYfWhiteCatFilter == null)
//                    mYfWhiteCatFilter = FilterFactory.createWhiteCatFilter(filterIndex);
//                return mYfWhiteCatFilter;
            case NINE_WINDOW:
                if (mYfMultiWindowFilter == null)
                    mYfMultiWindowFilter = mFactory.createMultiWindowFilter(9, filterIndex);
                return mYfMultiWindowFilter;
            case WAVE_INDEX:
                if (mYfWaveFilter == null)
                    mYfWaveFilter = mFactory.createWaveFilter(filterIndex);
                return mYfWaveFilter;
            case TARTAN_INDEX:
                if (mYfTartanFilter == null)
                    mYfTartanFilter = mFactory.createTartanFilter(VideoFilterActivity.this, filterIndex);
                return mYfTartanFilter;
            case DAZZLE_INDEX:
                if (mYfSoulDazzleFilter == null)
                    mYfSoulDazzleFilter = mFactory.createSoulDazzleFilter(filterIndex);
                return mYfSoulDazzleFilter;
            case MIRROR_INDEX:
                if (mYfMirrorFilter == null)
                    mYfMirrorFilter = mFactory.createMirrorFilter(filterIndex);
                return mYfMirrorFilter;
            case EDGE_INDEX:
                if (mYfEdgeFilter == null)
                    mYfEdgeFilter = mFactory.createEdgeFilter(filterIndex);
                return mYfEdgeFilter;
            case SHAKE_INDEX:
                if (mYfShakeFilter == null)
                    mYfShakeFilter = mFactory.createShakeFilter(filterIndex);
                return mYfShakeFilter;
        }

        return new BaseFilter();
    }

    /**
     * 初始化最后处理用的filter
     *
     * @param filterIndex
     * @return
     */
    private BaseFilter getConfigFilter(int filterIndex) {
        switch (filterIndex) {
            /*case ANTIQUE_INDEX:
                return FilterFactory.createAntiqueFilter(filterIndex);
            case WHITE_CAT_INDEX:
                return FilterFactory.createWhiteCatFilter(filterIndex);*/
            case NINE_WINDOW:
                return mFactory.createMultiWindowFilter(9, filterIndex);
            case WAVE_INDEX:
                return mFactory.createWaveFilter(filterIndex);
            case TARTAN_INDEX:
                return mFactory.createTartanFilter(VideoFilterActivity.this, filterIndex);
            case DAZZLE_INDEX:
                return mFactory.createSoulDazzleFilter(filterIndex);
            case EDGE_INDEX:
                return mFactory.createEdgeFilter(filterIndex);
            case MIRROR_INDEX:
                return mFactory.createMirrorFilter(filterIndex);
            case SHAKE_INDEX:
                return mFactory.createShakeFilter(filterIndex);
        }

        return new BaseFilter();
    }


    private void removeFilter(final int index) {
        mYfGlSurfaceView.removeFilter(index);
        mCurrentFilterIndex = -1;
        mCurrentFilter = null;
    }

   /* private void addAR(String itemName) {
        if (mFaceUnityFilter == null) {
            mFaceUnityFilter = new FaceUnityFilter(this);
            mFaceUnityFilter.setIndex(FACE_INDEX);
        }
        mFaceUnityFilter.setEffect(itemName, false);
        mYfGlSurfaceView.addFilter(mFaceUnityFilter);
        setAr = true;

    }*/

    private void openVideo(String path) {
        mCurrentPath = path;
        if (mYfCloudPlayer != null) {
//            mYfCloudPlayer.pause();
//            mYfCloudPlayer.stop();
            mYfCloudPlayer.release();
            mYfCloudPlayer = null;
        }

        mYfCloudPlayer = YfCloudPlayer.Factory.createPlayer(this, YfCloudPlayer.MODE_SOFT);
        mYfCloudPlayer.setOnPreparedListener(onPreparedListener);
        mYfCloudPlayer.setFrameCallback();
        mYfCloudPlayer.setAudioTrackStreamType(AudioManager.STREAM_MUSIC);
        mYfCloudPlayer.setOnNativeVideoDecodedListener(new YfCloudPlayer.OnNativeVideoDataDecoded() {
            @Override
            public void onVideoDataDecoded(YfCloudPlayer yfCloudPlayer, byte[] bytes, int i, int i1, long currentPts) {
//                Log.d(TAG,"onVideoDataDecoded:"+bytes.length+","+i+","+i1);

//                if (mFaceUnityFilter != null)
//                    mFaceUnityFilter.setCurrentFrameRGBA(bytes, i, i1);
                for (BaseFilter filter : mPreviewFilters) {
                    if (filter == mCurrentFilter || !filter.isRenderInSpecificPts())
                        return;
                    boolean inSection = false;
                    for (TimeSection timeSection : filter.getRenderSections()) {
                        if (currentPts >= timeSection.getStart() && currentPts <= timeSection.getEnd()) {
                            inSection = true;
                            break;
                        }
                    }
                    if (!filter.isAdded() && inSection) {
                        mYfGlSurfaceView.addFilter(filter);
                        Log.d(TAG, "in filter area:" + currentPts + "," + filter.getClass());
                        mCurrentFilterIndex = filter.getIndex();
                    } else if (filter.isAdded() && !inSection) {
                        Log.d(TAG, "not in filter area:" + currentPts + "," + filter.getClass());
                        mYfGlSurfaceView.removeFilter(filter.getIndex());
                    }
//
//                        if (!config.filter.isInitialized() && l >= config.startPts && l <= config.endPts) {
//                            Log.d(TAG, "in filter area:" + l + "," + config.startPts + "," + config.endPts);
//                            mYfGlSurfaceView.addFilter(config.filter);
//                            mCurrentFilterIndex = config.filter.getIndex();
//                        } else if (config.filter.isInitialized() && (l < config.startPts || l > config.endPts)) {
//                            Log.d(TAG, "not in filter area:" + l + "," + config.startPts + "," + config.endPts);
//                            mYfGlSurfaceView.removeFilter(config.filter.getIndex());
//                        }
                }

            }
        });
        mYfCloudPlayer.setOnCompletionListener(new YfCloudPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(YfCloudPlayer yfCloudPlayer) {
                mStartBtn.setVisibility(View.VISIBLE);
            }
        });
        mYfCloudPlayer.setOnErrorListener(new YfCloudPlayer.OnErrorListener() {
            @Override
            public boolean onError(YfCloudPlayer yfCloudPlayer, int i, int i1) {
//                mUIHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (preLoading) openVideo(mCurrentPath);
//                    }
//                }, 500);

                return false;
            }
        });
        mYfCloudPlayer.setSurface(mYfGlSurfaceView.getSurface());

        try {
            mYfCloudPlayer.setDataSource(path);
            mYfCloudPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean prepared, surfaceCreated;
    private YfCloudPlayer.OnPreparedListener onPreparedListener = new YfCloudPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(YfCloudPlayer yfCloudPlayer) {
            if (preLoading) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onDone();
                    }
                });
                return;
            }
            mDuration = yfCloudPlayer.getDuration();
            mVideoWidth = yfCloudPlayer.getVideoWidth();
            mVideoHeight = yfCloudPlayer.getVideoHeight();
            prepared = true;
            if (startPlayback) {
                startPause(mStartBtn);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
        mStartBtn.setVisibility(View.VISIBLE);
    }

    private void startPlayback() {
        startPlayback = true;
        if (surfaceCreated && prepared) {
            if (mYfCloudPlayer != null)
                mYfCloudPlayer.start();
        }
    }

    private void pause() {
        startPlayback = false;
        mYfCloudPlayer.pause();
    }

    private boolean startPlayback;

    public void startPause(View v) {
        if (mYfCloudPlayer == null)
            return;
        mCover.clearAnimation();
        mCover.setVisibility(View.GONE);
        if (mYfCloudPlayer.isPlaying()) {
            pause();
            v.setVisibility(View.VISIBLE);
        } else {
            startPlayback();
            mController.show(0);
            v.clearAnimation();
            v.setVisibility(View.GONE);
        }
    }

    private YfGlSurfaceView.YfRenderCallback mRenderCallback = new YfGlSurfaceView.YfRenderCallback() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            surfaceCreated = true;
            Log.d(TAG, "onSurfaceCreated~" + mYfCloudPlayer);
            if (mYfCloudPlayer == null) {
                openVideo(mCurrentPath);
            } else {
                mYfCloudPlayer.setSurface(mYfGlSurfaceView.getSurface());//从后台或其他界面恢复，重新设置display
                mYfCloudPlayer.start();
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged~" + mYfCloudPlayer);

        }

        @Override
        public void onDrawFrame(GL10 gl) {

        }
    };

    @Override
    protected void onDestroy() {
        if (mYfCloudPlayer != null) {
            mYfCloudPlayer.stop();
            mYfCloudPlayer.release();
        }
        if (mYfEditor != null) {
            mYfEditor.release();
        }
        mController.release();
        mMediaKitCallback = null;
        Util.deleteFile(mFlashbackPath);
        mMediaKitCallback = null;
        mMediaKit = null;
        dismissPopupWindow();
        super.onDestroy();
    }

    private void dismissPopupWindow() {
        if (mFilterPopupWindow != null) mFilterPopupWindow.dismiss();
        if (mLogoPopupWindow != null) mLogoPopupWindow.dismiss();
        if (mGifPopupWindow != null) mGifPopupWindow.dismiss();
//        if (mUploadPopupWindow != null) mUploadPopupWindow.dismiss();
        if (mTimeEffectPopupwindow != null) mTimeEffectPopupwindow.dismiss();
    }

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        private final WeakReference<VideoFilterActivity> mActivity;

        MainHandler(VideoFilterActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoFilterActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case SHOW_IMG_SUCCESS:
                        VideoEditInfo info = (VideoEditInfo) msg.obj;
                        if (activity.picCount == 0) {
                            Log.d(TAG, "load preview img：" + info.path);
                            Glide.with(VideoFilterActivity.this)
                                    .load("file://" + info.path).centerCrop()
                                    .into(mCover);
                        }
                        mController.addThumbnail(info);
                        activity.picCount++;
                        break;
                    case UPLOAD_PROGRESS:
                        int pro = Math.min(95, msg.arg1 / 2 + 50);
                        activity.mUploadHint.setText("视频上传中..." + pro + "%");
                        if (msg.arg1 == 100) {
//                            activity.onUploadFinish();
                            activity.mUploadHint.setText("视频上传中..." + 98 + "%");
                        }
                        break;
                }

            }
        }
    }


}
