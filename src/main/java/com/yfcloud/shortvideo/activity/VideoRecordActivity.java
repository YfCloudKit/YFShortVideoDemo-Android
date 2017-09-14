package com.yfcloud.shortvideo.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.utils.CacheActivity;
import com.yfcloud.shortvideo.utils.Configure;
import com.yfcloud.shortvideo.utils.Const;
import com.yfcloud.shortvideo.utils.FilterFactory;
import com.yfcloud.shortvideo.utils.Util;
import com.yfcloud.shortvideo.widget.AudioCutView;
import com.yfcloud.shortvideo.widget.FaceuPopupWindow;
import com.yfcloud.shortvideo.widget.FocusLayout;
import com.yfcloud.shortvideo.widget.RoundButton;
import com.yfcloud.shortvideo.widget.ScaleGLSurfaceView;
import com.yfcloud.shortvideo.widget.VideoProgressView;
import com.yfcloud.shortvideo.widget.YfPopupWindow;
import com.yunfan.encoder.filter.AlphaBlendFilter;
import com.yunfan.encoder.filter.BaseFilter;
import com.yunfan.encoder.filter.FaceUnityFilter;
import com.yunfan.encoder.filter.YfBlurBeautyFilter;
import com.yunfan.encoder.widget.YfEncoderKit;
import com.yunfan.encoder.widget.YfVodKit;
import com.yunfan.player.widget.YfCloudPlayer;
import com.yunfan.player.widget.YfPlayerKit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.yfcloud.shortvideo.utils.Const.MAX_RECORD_TIME;
import static com.yfcloud.shortvideo.utils.Const.MIN_RECORD_TIME;

/**
 * 视频录制界面
 *
 * ---YfEncoderKit相关--
 * 切换前后置
 * 手动/自动对焦
 * 曝光补偿
 * 美颜
 * Faceu
 * 风格滤镜
 * 水印
 *
 * ---YfCloudPLayer相关---
 * 倍速播放
 *
 * ---YfVodKit相关---
 * 分段录制，回删
 *
 */
public class VideoRecordActivity extends AppCompatActivity {
    private static final String TAG = "Yf_VideoRecordActivity";
    private static final int BEAUTY_INDEX = 101;
    private static final int FACE_INDEX = 102;
    private static final int LOGO_INDEX = 103;
    private YfVodKit mYfVodKit;
    private YfEncoderKit mYfEncoderKit;
    private YfCloudPlayer mYfCloudPlayer;
    private YfBlurBeautyFilter mBeautyFilter;
    private List<View> mNeedHideViewList;

    private String mAudioPath;
    private LinearLayout mLlPreview;
    private RelativeLayout mRlCutMusic;
    private RelativeLayout mRlEffect;
    private RelativeLayout mRlRecordProgress;
    private RelativeLayout mRlRecordButton;
    private VideoProgressView mVideoProgressView;
    private Button mBtnAr;
    private FaceuPopupWindow mArPopupWindow;
    private FaceUnityFilter mFaceUnityFilter;
    private View mEffectBottom;
    private ImageButton mIbMusicSeek;
    private ScaleGLSurfaceView mGlSurfaceView;
    private FocusLayout mFocusLayout;
    private YfPopupWindow mStyleFilterPopupWindow;

    private boolean musicIsPaused;
    private boolean mSetFaceu;
    private boolean mSetBeauty = true;
    private boolean mHevcEncoder = false;
    private int mBitrate;
    private int mFramerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CacheActivity.addActivity(this);
        YfPlayerKit.enableRotation(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_record);
        getIntentData();
        initView();
    }

    private void getIntentData() {
        mAudioPath = getIntent().getStringExtra(Const.KEY_PATH_AUDIO);
        Log.d(TAG, "mAudioPath: " + mAudioPath);
        mBitrate = Configure.BITRATE;
        mFramerate = Configure.FRAME_RATE;
        int resolution = Configure.RESOLUTION;
        mHevcEncoder = Configure.HEVC_ENCODER;
        Log.d(TAG, "mBitrate: " + mBitrate);
        Log.d(TAG, "mFramerate: " + mFramerate);
        Log.d(TAG, "mResolution: " + resolution);
        Log.d(TAG, "mHevcEncoder: " + mHevcEncoder);

        if (resolution == Const.RESOLUTION_360) {
            VIDEO_WIDTH = 640;
            VIDEO_HEIGHT = 352;
        } else {
            VIDEO_WIDTH = 960;
            VIDEO_HEIGHT = 544;
        }
    }

    protected int VIDEO_WIDTH = 640;
    protected int VIDEO_HEIGHT = 368;

    protected int PREVIEW_WIDTH = 1280;
    protected int PREVIEW_HEIGHT = 720;
    private boolean mHardEncoder = false;

    private void initView() {
        mGlSurfaceView = (ScaleGLSurfaceView) findViewById(R.id.surface_view);
        mFocusLayout = (FocusLayout) findViewById(R.id.focus_layout);
        //预览界面ui
        ImageButton ibSwitch = (ImageButton) findViewById(R.id.ib_switch);
        ImageButton ibBack = (ImageButton) findViewById(R.id.ib_back);
        ImageButton ibEffect = (ImageButton) findViewById(R.id.ib_effects);
        ImageButton ibFilterStyle = (ImageButton) findViewById(R.id.ib_filter_style);
        mIbMusicSeek = (ImageButton) findViewById(R.id.ib_music_seek);
        RadioGroup rgSpeed = (RadioGroup) findViewById(R.id.rg_speed);

        //录制相关ui
        Button btnDelete = (Button) findViewById(R.id.btn_delete);
        Button btnNext = (Button) findViewById(R.id.btn_next);
        RoundButton ibRecord = (RoundButton) findViewById(R.id.ib_record);
        mVideoProgressView = (VideoProgressView) findViewById(R.id.progress_view);
        mVideoProgressView.setOnProgressListener(mOnProgressListener);
        mVideoProgressView.setMaxDuration(MAX_RECORD_TIME);
        //几层layout
        mLlPreview = (LinearLayout) findViewById(R.id.ll_preview_buttons);
        mRlRecordProgress = (RelativeLayout) findViewById(R.id.rl_record_progress);
        mRlRecordButton = (RelativeLayout) findViewById(R.id.rl_record_button);
        mRlCutMusic = (RelativeLayout) findViewById(R.id.rl_cut_music);
        mRlEffect = (RelativeLayout) findViewById(R.id.rl_effect);

        //选取音乐时间
        ImageButton ibSelectStartTimeOk = (ImageButton) findViewById(R.id.ib_select_start_time_ok);

        //特效
        mEffectBottom = findViewById(R.id.rl_effect_bottom);
        mBtnAr = (Button) findViewById(R.id.btn_ar);
        ToggleButton tbBeauty = (ToggleButton) findViewById(R.id.tb_beauty);

        rgSpeed.setOnCheckedChangeListener(mOnCheckedChangeListener);
        tbBeauty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getId()) {
                    case R.id.tb_beauty:
                        if (isChecked)
                            mYfEncoderKit.addFilter(mBeautyFilter);
                        else
                            mYfEncoderKit.removeFilter(BEAUTY_INDEX);
                        break;
                }
            }
        });


        ibSwitch.setOnClickListener(mOnClickListener);
        ibBack.setOnClickListener(mOnClickListener);
        ibEffect.setOnClickListener(mOnClickListener);
        ibFilterStyle.setOnClickListener(mOnClickListener);
        mIbMusicSeek.setOnClickListener(mOnClickListener);
        btnDelete.setOnClickListener(mOnClickListener);
        btnNext.setOnClickListener(mOnClickListener);
        ibSelectStartTimeOk.setOnClickListener(mOnClickListener);
        mBtnAr.setOnClickListener(mOnClickListener);

        ibRecord.setOnCustomTouchListener(mOnCustomTouchListener);
        setSurfaceSize(false, PREVIEW_WIDTH, PREVIEW_HEIGHT);
        initNeedHideViewList();
        initRecord(mGlSurfaceView);
        initDefaultSetting();
        initPlayer();
    }

    /**
     * @param landscape 是否为横屏模式，预览宽度，预览高度
     */
    private void setSurfaceSize(boolean landscape, int width, int height) {
        ViewGroup.LayoutParams lp = mGlSurfaceView.getLayoutParams();
        int realScreenWidth = Util.getScreenWidth(this);
        Log.d(TAG, "realScreenWidth:" + realScreenWidth + "," + landscape);
        int surfaceWidth;
        int surfaceHeight;
        if (landscape) {
            surfaceWidth = realScreenWidth * 16 / 9;
            surfaceHeight = surfaceWidth * height / width;
        } else {
            surfaceHeight = realScreenWidth * 16 / 9;
            //考虑到高度可能被内置虚拟按键占用，因此为了保证预览界面为16:9，不能直接获取高度。
            surfaceWidth = surfaceHeight * height / width;
        }
        lp.width = surfaceWidth;
        lp.height = surfaceHeight;
        Log.d(TAG, "计算出来的宽高:" + surfaceWidth + "___" + surfaceHeight);
        mGlSurfaceView.setLayoutParams(lp);
    }

    private void initNeedHideViewList() {
        mNeedHideViewList = new ArrayList<>();
        mNeedHideViewList.add(mLlPreview);
        mNeedHideViewList.add(mRlRecordButton);
        mNeedHideViewList.add(mRlCutMusic);
//        mNeedHideViewList.add(mRlEffect);
    }

    public void showView(View v) {
        for (View view : mNeedHideViewList) {
            view.setVisibility(view == v ? View.VISIBLE : View.GONE);
        }
        if (mLlPreview.getVisibility() == View.VISIBLE) {
            mRlRecordButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化默认设置，默认开启美颜，添加水印
     */
    protected void initDefaultSetting() {
        mBeautyFilter = new YfBlurBeautyFilter(this);
        mBeautyFilter.setIndex(BEAUTY_INDEX);
        mYfEncoderKit.addFilter(mBeautyFilter);//默认打开美颜

        AlphaBlendFilter mLogoFilter = new AlphaBlendFilter(1);
        mLogoFilter.setIndex(LOGO_INDEX);
        float landscapeMarginRight = 0.1f;//横屏模式下logo的marginright所占宽度的比例
        float portMarginRight = 0.05f;//竖屏模式下logo的marginright所占宽度的比例
        float landsapeMarginTop = 0.05f;//横屏模式下logo的marginTop所占宽度的比例
        float portMarginTop = 0.02f;//竖屏模式下logo的marginTop所占宽度的比例
        float landscapeLogoHeight = 0.2f;//横屏模式下logo的高度所占屏幕高度的比例
        float logoWidth = 454, logoHeight = 160;//计算logo的比例
        mLogoFilter.config(BitmapFactory.decodeResource(getResources(), R.mipmap.logo),
                landscapeLogoHeight * logoWidth / logoHeight,
                landscapeLogoHeight * 9 / 16,
                1 - landscapeLogoHeight * logoWidth / logoHeight,
                portMarginTop);
        mYfEncoderKit.addFilter(mLogoFilter);


        if (mFaceUnityFilter == null) {
            mFaceUnityFilter = new FaceUnityFilter(VideoRecordActivity.this);
            String[] faceuItems = getResources().getStringArray(R.array.ar);
            mFaceUnityFilter.setEffect(faceuItems[0], false);//使用assets里的文件只需要写文件名即可，第二个参数填false;
//              mFaceUnityFilter.setEffect(CACHE_DIRS+"/"+m_item_names[mCurrentEffectId],true);//使用完整路径的话，第二个参数填true
            mFaceUnityFilter.setGestureEffect("heart.mp3", false);//同上
            mFaceUnityFilter.enableBeautyEffect(true);//开启faceUnity自带的美颜
            mFaceUnityFilter.setBeautyType(FaceUnityFilter.BEAUTY_NATURE);//设置美颜类型
            mFaceUnityFilter.setBeautyBlurLevel(5);//设置磨皮等级，取值0~5
            mFaceUnityFilter.setBeautyCheekThinningLevel(0);//设置瘦脸等级，0为关闭，1为默认，大于1为继续增强效果
            mFaceUnityFilter.setBeautyColorLevel(0.6);//设置色彩效果等级，0为关闭，1为默认，大于1为继续增强效果；美颜类型为nature时代表美白等级
            mFaceUnityFilter.setBeautyEyeEnlargingLevel(0);//设置大眼效果等级，0为关闭，1为默认，大于1为继续增强效果
            mFaceUnityFilter.setIndex(FACE_INDEX);
        }
    }

    private VideoProgressView.RecordProgressListener mOnProgressListener
            = new VideoProgressView.RecordProgressListener() {
        @Override
        public void onProgress(int progress) {
            if (progress == 100) {//录制总时长已达到MAX_RECORD_TIME，强制停止
                showToast("录制已达最大时长");
                Log.d(TAG, "mTotalRecordTime: " + mYfVodKit.getCurrentRecordTimestamp());
                finishRecord();
            }
        }
    };
    private RoundButton.OnCustomTouchListener mOnCustomTouchListener
            = new RoundButton.OnCustomTouchListener() {
        @Override
        public void onLongTouchUp() {
            Log.d(TAG, "onTouchUp: ");
            mYfVodKit.pauseRecord();
            if (mYfVodKit.getVideoList().size() > 0) {
                mIbMusicSeek.setVisibility(View.GONE);
            }
            showView(mLlPreview);
        }

        @Override
        public void onLongTouch() {
            Log.d(TAG, "onLongTouch: ");
            if (!mYfVodKit.isRecording()) {
                if (mYfVodKit.getCurrentRecordTimestamp() >= MAX_RECORD_TIME) {
                    showToast("录制已达最大时长");
                    return;
                }
                if (!prepared) return;
                showView(mRlRecordButton);
                mYfVodKit.startRecord(1 / mSpeed);
                Log.d(TAG, "startRecord: ");
            }
        }
    };


    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private float mSpeed = 1.0f;
    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            switch (checkedId) {
                case R.id.rb_slower:
                    mSpeed = 3.0f;
                    break;
                case R.id.rb_slow:
                    mSpeed = 2.0f;
                    break;
                case R.id.rb_normal:
                    mSpeed = 1.0f;
                    break;
                case R.id.rb_fast:
                    mSpeed = 0.5f;
                    break;
                case R.id.rb_faster:
                    mSpeed = 0.33f;
                    break;
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ib_switch:
                    mFocusLayout.reset();
                    boolean b = mYfEncoderKit.switchCamera();
                    if (b && !mYfEncoderKit.isFrontCameraUsed()) {
                        mFocusLayout.setExposureCompensationValue(mYfEncoderKit.getExposureCompensationValue());
                        mFocusLayout.setOnSeekBarChangeListener(new FocusLayout.SeekBarChangeListener() {
                            @Override
                            public void onSeekChanged(int value) {
                                Log.d(TAG, "onSeekChanged: " + value);
                                mYfEncoderKit.manualExposureCompensation(value);
                            }
                        });
                    }
                    break;
                case R.id.ib_back:
                    onBackPressed();
                    break;
                case R.id.ib_effects:
                    showView(null);
                    showArPopupWindow();
                    break;
                case R.id.ib_filter_style:
                    showView(null);
                    showStyleFilterPopupWindow();
                    break;
                case R.id.ib_music_seek:
                    showView(mRlCutMusic);
                    break;
                case R.id.btn_delete:
                    deleteSegment();
                    break;
                case R.id.btn_next:
                    finishRecord();
                    break;
                case R.id.ib_select_start_time_ok:
                    showView(mLlPreview);
                    pauseMusic();
                    mYfCloudPlayer.seekTo(mAudioStartTime);
                    break;
                case R.id.btn_ar:
                    mEffectBottom.setVisibility(View.GONE);
                    showArPopupWindow();
                    break;
            }
        }
    };

    /**
     * 显示faceuPopupWindow
     */
    private void showArPopupWindow() {
        String[] arItems = getResources().getStringArray(R.array.ar);
        if (mArPopupWindow == null) {
            mArPopupWindow = new FaceuPopupWindow(this, arItems, false);
            mArPopupWindow.setOnFaceuChangeListener(mOnFaceuChangeListener);
            mArPopupWindow.setOnDismissListener(mOnDismissListener);
        }
        mArPopupWindow.showAtLocation(mBtnAr, Gravity.BOTTOM, 0, 0);
    }

    PopupWindow.OnDismissListener mOnDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            showView(mLlPreview);
        }
    };

    /**
     * 显示风格滤镜PopupWindow
     */
    private void showStyleFilterPopupWindow() {
        String[] styleFilterName = getResources().getStringArray(R.array.style_filter_name);
        if (mStyleFilterPopupWindow == null) {
            mStyleFilterPopupWindow = new YfPopupWindow(this, styleFilterName, YfPopupWindow.TYPE_FILTER_STYLE, true);
            mStyleFilterPopupWindow.setOnSelectedListener(mOnStyleFilterChangeListener);
            mStyleFilterPopupWindow.setOnDismissListener(mOnDismissListener);
        }
        mStyleFilterPopupWindow.showAtLocation(mBtnAr, Gravity.BOTTOM, 0, 0);
    }


    private YfPopupWindow.OnSelectedListener mOnStyleFilterChangeListener
            = new YfPopupWindow.OnSelectedListener() {

        private BaseFilter mStyleFilter;

        @Override
        public void onSelected(int position) {
            Log.d(TAG, "onSelected: " + position);
            if (mStyleFilter != null) mYfEncoderKit.removeFilter(mStyleFilter.getIndex());
            if (position != 0) {
                try {
                    mStyleFilter = createFilter(position);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                mYfEncoderKit.addFilter(mStyleFilter);
            }

        }
    };

    private final int INDEX_SKIN_WHITE = 801, INDEX_ROMANTIC = 802, INDEX_TENDER = 803,
            INDEX_FAIRY_TALE = 804, INDEX_ANTIQUE = 805, INDEX_WHITE_CAT = 806,
            INDEX_LATTE = 807, INDEX_BLACK = 808;
    FilterFactory mFactory;

    private BaseFilter createFilter(int index) throws IllegalAccessException {
        BaseFilter baseFilter = null;
        if (mFactory == null)
            mFactory = new FilterFactory(this);
        switch (index) {
            case 1:
                baseFilter = mFactory.createSkinWhiteFilter(INDEX_SKIN_WHITE);
                break;
            case 2:
                baseFilter = mFactory.createRomanticFilter(INDEX_ROMANTIC);
                break;
            case 3:
                baseFilter = mFactory.createTenderFilter(this, INDEX_TENDER);
                break;
            case 4:
                baseFilter = mFactory.createFairyTaleFilter(this, INDEX_FAIRY_TALE);
                break;
            case 5:
                baseFilter = mFactory.createAntiqueFilter(INDEX_ANTIQUE);
                break;
            case 6:
                baseFilter = mFactory.createWhiteCatFilter(INDEX_WHITE_CAT);
                break;
            case 7:
                baseFilter = mFactory.createLatteFilter(INDEX_LATTE);
                break;
            case 8:
                baseFilter = mFactory.createBlackWhiteFilter(INDEX_BLACK);
                break;
        }

        return baseFilter;
    }


    private FaceuPopupWindow.OnFaceuChangeListener mOnFaceuChangeListener
            = new FaceuPopupWindow.OnFaceuChangeListener() {
        @Override
        public void onFaceuChanged(String itemName) {
            Log.d(TAG, "onFaceuChanged: " + itemName);
            changeFaceu(itemName);
        }
    };

    /**
     * faceu特效文件的路径。当使用assets目录下的特效文件时，可以仅使用特效名；如果需要使用其他路径下的特效文件，需输入包含目录的完整特效路径
     * @param itemName
     */
    private void changeFaceu(String itemName) {
        if (mFaceUnityFilter != null) {
            if (TextUtils.isEmpty(itemName)) {
                //关闭faceu
                mYfEncoderKit.removeFilter(FACE_INDEX);
                mSetFaceu = false;
                mSetBeauty = true;
                mYfEncoderKit.addFilter(mBeautyFilter);//默认打开美颜
            } else {
                if (!mSetFaceu) {
                    if (mSetBeauty) {//开启face u 则默认关闭原本的美颜
                        mYfEncoderKit.removeFilter(BEAUTY_INDEX);
                        mSetBeauty = !mSetBeauty;
                    }
                    mYfEncoderKit.addFilter(mFaceUnityFilter);
                    mSetFaceu = !mSetFaceu;
                }
                mFaceUnityFilter.setEffect(itemName, false);
            }
        }
    }

    /**
     * 结束录制
     */
    private void finishRecord() {
        if (mYfVodKit.getVideoList().size() <= 0) {
            return;
        }
        if (mYfVodKit.isRecording()) {
            mYfVodKit.pauseRecord();
        }
        int totalRecordTime = mYfVodKit.getCurrentRecordTimestamp();
        Log.d(TAG, "totalRecordTime: " + totalRecordTime);
        if (totalRecordTime < MIN_RECORD_TIME) {
            showToast("录制时间过短");
            return;
        }
        showToast("视频合成中，请稍候...");
//        mYfVodKit.enableReverseVideo(mCbReverse.isChecked());
        mYfVodKit.finishRecord();
    }

    /**
     * 回删
     */
    private void deleteSegment() {
        if (mYfVodKit.isRecording()) return;
        Log.d(TAG, "mRecordSegments.size(): " + mYfVodKit.getVideoList().size());
        if (mYfVodKit.getVideoList().size() > 0) {
            mYfVodKit.deleteVideo(mYfVodKit.getVideoList().getLast().getId());
            mVideoProgressView.setCurrentState(VideoProgressView.State.DELETE);
//            if (mYfVodKit.getVideoList().isEmpty()) {
//                mIbMusicSeek.setVisibility(View.VISIBLE);
//            }
        } else {
            mVideoProgressView.clearTimeList();
        }
    }

    private void resetTag() {
        prepared = false;
        musicIsPaused = false;
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        resetTag();
        if (mYfCloudPlayer == null)
            mYfCloudPlayer = YfCloudPlayer.Factory.createPlayer(this, YfCloudPlayer.MODE_SOFT);
        mYfCloudPlayer.setSpeed(1.0f);
        mYfCloudPlayer.setOnPreparedListener(onPreparedListener);
        mYfCloudPlayer.setOnInfoListener(new YfCloudPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(YfCloudPlayer yfCloudPlayer, int i, int i1) {
                switch (i) {
                    case YfCloudPlayer.INFO_CODE_AUDIO_RENDERING_START:
                        //第一次录制开始计时
                        Log.d(TAG, "INFO_CODE_AUDIO_RENDERING_START");
                        mYfVodKit.onAudioRender();//为了音视频能同步，需通知编码器音频已经开始渲染，此时正式开始编码视频
                        break;
                }
                return false;
            }
        });
        mYfCloudPlayer.setHardwareDecoder(false);
        mYfCloudPlayer.setPCMCallback();
        mYfCloudPlayer.setOnNativeAudioDecodedListener(new YfCloudPlayer.OnNativeAudioDataDecoded() {
            @Override
            public void onAudioDataDecoded(YfCloudPlayer yfCloudPlayer, byte[] bytes, int i, long l) {
            }
        });
        mYfCloudPlayer.setDisplay(null);
        try {
            mYfCloudPlayer.setDataSource(mAudioPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mYfCloudPlayer.prepareAsync();
    }


    private long mAudioDuration;
    private boolean prepared;
    private YfCloudPlayer.OnPreparedListener onPreparedListener = new YfCloudPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(YfCloudPlayer yfCloudPlayer) {
            mAudioDuration = yfCloudPlayer.getDuration();
            Log.d(TAG, "onPrepared: " + mAudioDuration);
            mUIHandler.post(mAddAudioSelectViewRunnable);
            prepared = true;
        }
    };

    private AudioCutView mAudioCutView;
    private Runnable mAddAudioSelectViewRunnable = new Runnable() {
        @Override
        public void run() {
            mAudioCutView = new AudioCutView(VideoRecordActivity.this);
            mAudioCutView.setAudioDuration(mAudioDuration);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, 280);
//            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.addRule(RelativeLayout.ABOVE, R.id.music_cut_bottom);
            mAudioCutView.setLayoutParams(lp);
            mAudioCutView.setOnSelectStartTimeListener(mOnSelectStartTimeListener);
            mRlCutMusic.addView(mAudioCutView);
        }
    };

    @Override
    public void onBackPressed() {
        if (mYfEncoderKit.isRecording()) return;
        if (mRlCutMusic.getVisibility() == View.VISIBLE) {
            pauseMusic();
            mYfCloudPlayer.seekTo(mAudioStartTime);
        }

        if (mLlPreview.getVisibility() == View.GONE) {
            showView(mLlPreview);
        } else {
            super.onBackPressed();
        }
    }

    private long mAudioStartTime;
    private AudioCutView.SelectStartTimeListener mOnSelectStartTimeListener
            = new AudioCutView.SelectStartTimeListener() {
        @Override
        public void onSelectStartTime(long startTime) {
            Log.d(TAG, "onSelectStartTime: " + startTime);
            mAudioStartTime = startTime;
            mYfCloudPlayer.seekTo(startTime);
            mYfCloudPlayer.start();
        }
    };

    /**
     * 初始化YfVodKit、YfEncoderKit
     * @param glSurfaceView
     */
    private void initRecord(GLSurfaceView glSurfaceView) {
        Log.d(TAG, "RECORD_BITRATE: " + Configure.RECORD_BITRATE);
        mYfVodKit = new YfVodKit(this, Const.PATH, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                VIDEO_WIDTH, VIDEO_HEIGHT, mHardEncoder, mFramerate, Configure.RECORD_BITRATE, mMonitor);

        Log.d(TAG, "initRecord: " + mAudioPath);
        mYfVodKit.setAudioSource(mAudioPath);
        mYfVodKit.setInputPlayer(mPlayerInterface);
        mYfEncoderKit = mYfVodKit.getYfEncoderKit()//可以获取内部的YfEncoderKit以设置美颜等经典配置。
                .enableFlipFrontCamera(false)//设置前置摄像头是否镜像处理，默认为false
                .setContinuousFocus()//设置连续自动对焦
                .setDefaultCamera(true);//设置默认打开摄像头---true为前置，false为后置
        mYfEncoderKit.enableHEVCEncoder(mHevcEncoder);
        mYfVodKit.openCamera(glSurfaceView);

        mGlSurfaceView.initScaleGLSurfaceView(new ScaleGLSurfaceView.OnScaleCallback() {
            @Override
            public int getCurrentZoom() {
                return mYfEncoderKit.getCurrentZoom();
            }

            @Override
            public int getMaxZoom() {
                return mYfEncoderKit.getMaxZoom();
            }

            @Override
            public boolean onScale(int zoom) {
                return mYfEncoderKit.manualZoom(zoom);//手动缩放
            }

            @Override
            public boolean onFocus(Rect rect) {
                return mYfEncoderKit.manualFocus(rect);//手动对焦
            }
        });

        mGlSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && !mYfEncoderKit.isFrontCameraUsed()) {
                    mFocusLayout.focus(event.getX(), event.getY());//传入手动对焦的坐标
                }
                return false;
            }
        });
    }

    YfVodKit.InputPlayerInterface mPlayerInterface = new YfVodKit.InputPlayerInterface() {
        @Override
        public long getCurrentPosition() {
            return mYfCloudPlayer != null ? mYfCloudPlayer.getCurrentPosition() : 0;
        }

        @Override
        public void start() {
            playMusic();
        }

        @Override
        public void pause() {
            pauseMusic();
        }

        @Override
        public void seekTo(long l) {
            if (mYfCloudPlayer != null) {
                mYfCloudPlayer.seekTo(l);
            }
        }
    };

    private void playMusic() {
        if (prepared) {
            Log.d(TAG, "开始播放音乐~~~");
            //每次播放音乐时记录当前时间点
            mYfCloudPlayer.setSpeed(mSpeed);
            mYfCloudPlayer.start();
            musicIsPaused = false;
        }
    }

    Handler mUIHandler = new Handler();

    private void pauseMusic() {
        if (mYfCloudPlayer != null && mYfCloudPlayer.isPlaying()) {
            mYfCloudPlayer.pause();
            musicIsPaused = true;
        }
        mUIHandler.removeCallbacks(updateProgress);
    }


    /**
     * 根据录制时间戳绘制进度条
     */
    private final Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "getVideoPts:-------- " + mYfVodKit.getCurrentRecordTimestamp());
            if (mYfVodKit == null) return;
            mVideoProgressView.updateProgress(mYfVodKit.getCurrentRecordTimestamp());
            mUIHandler.postDelayed(this, 40);
        }
    };

    private YfVodKit.VodKitMonitor mMonitor = new YfVodKit.VodKitMonitor() {
        @Override
        public void onError(int err) {
            switch (err) {
                case YfVodKit.ERROR_MERGE_FAILED:
                    showToast("视频拼接失败...");
                    break;
                case YfVodKit.ERROR_REVERSE_FAILED:
                    showToast("时光倒流失败...");
                    break;
                case YfVodKit.ERROR_MUX_FAILED:
                    showToast("视频合成失败...");
                    break;
                case YfVodKit.ERROR_SPLIT_FAILED:
                    showToast("视频裁剪失败...");
                    break;
                case YfVodKit.ERROR_TRANSFORM_FAILED:
                    showToast("音频转码失败...");
                    break;

            }
        }

        @Override
        public void onInfo(int what, int arg1, int arg2, Object obj) {
//            Log.d(TAG, "onInfo: " + what);
//            switch (what) {
//                case YfVodKit.INFO_MERGE_START:
//                    break;
//                case YfVodKit.INFO_MERGE_END:
//                    break;
//                case YfVodKit.INFO_TRANSFORM_START:
//                    break;
//                case YfVodKit.INFO_TRANSFORM_END:
//                    break;
//                case YfVodKit.INFO_REVERSE_START:
//                    break;
//                case YfVodKit.INFO_REVERSE_END:
//                    break;
//                case YfVodKit.INFO_MUX_START:
//                    break;
//                case YfVodKit.INFO_MUX_END:
//                    break;
//                case YfVodKit.INFO_SPLIT_START:
//                    break;
//                case YfVodKit.INFO_SPLIT_END:
//                    showToast((String) obj);
//                    FilterPlayerActivity.startPlayerActivity(this, (String) obj, mInputBitrate);
//                    finish();
//                    break;
//            }
        }

        @Override
        public void onStartRecording() {
            mVideoProgressView.setCurrentState(VideoProgressView.State.START);
            mUIHandler.removeCallbacks(updateProgress);
            mUIHandler.postDelayed(updateProgress, 100);
        }

        @Override
        public void onRecordPaused() {
            Log.d(TAG, "onRecordPaused: " + mYfVodKit.getCurrentRecordTimestamp());
            mVideoProgressView.setCurrentState(VideoProgressView.State.PAUSE);
            mVideoProgressView.putTimeList(mYfVodKit.getCurrentRecordTimestamp());
        }

        @Override
        public void onFinish(String path) {
            Log.d(TAG, "onFinish: " + path);
            Intent intent = new Intent(VideoRecordActivity.this, VideoCutActivity.class);
            intent.putExtra(Const.KEY_PATH_MUX_VIDEO, path);

            intent.putExtra(Const.KEY_OUTPUT_FRAMERATE, mFramerate);
            intent.putExtra(Const.KEY_OUTPUT_BITRATE, mBitrate);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mYfVodKit.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mYfVodKit.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        mYfVodKit.release();
    }

    private void releasePlayer() {
        if (mYfCloudPlayer != null) {
            mYfCloudPlayer.reset();
            mYfCloudPlayer.release();
            mYfCloudPlayer = null;
        }
        mUIHandler.removeCallbacksAndMessages(null);
    }
}
