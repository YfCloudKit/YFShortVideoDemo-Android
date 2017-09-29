package com.yfcloud.shortvideo.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kuaipai.fangyan.core.shooting.jni.RecorderJni;
import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.utils.Configure;
import com.yfcloud.shortvideo.utils.Const;
import com.yfcloud.shortvideo.utils.FileUtils;
import com.yfcloud.shortvideo.utils.Util;
import com.yunfan.encoder.entity.YfVideo;
import com.yunfan.encoder.widget.YfMediaKit;
import com.yunfan.player.utils.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;

/**
 * 参数设置界面
 */
public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "Yf_SettingActivity";
    private static final int CODE_FOR_READ_WRITE = 111;
    private final int MERGE_INDEX = 0x100;

    /**
     * 参数设置
     **/
    private EditText mEtBitrate;
    private EditText mEtFramerate;
    private EditText mEtEncodeBitrate;
    private RadioGroup rgResolution, rgEncoder, rgQuality;

    private boolean DEBUG = false;
    private YfMediaKit mYfMediaKit;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkEncoderPermission();
        } else {
            initView();
        }
    }

    @TargetApi(M)
    private void checkEncoderPermission() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                | checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                | checkSelfPermission(Manifest.permission.CAMERA)
                | checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                | checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE}, CODE_FOR_READ_WRITE);
        } else {
            initView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CODE_FOR_READ_WRITE) {
            if (permissions[0].equals(Manifest.permission.CAMERA)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && permissions[1].equals(Manifest.permission.RECORD_AUDIO)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && permissions[2].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && permissions[3].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && permissions[4].equals(Manifest.permission.READ_PHONE_STATE)
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                //用户同意使用camera
                initView();
            } else {
                finish();
            }
        }
    }

    private void initView() {
        TextView tvBack = (TextView) findViewById(R.id.tv_back);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        Button tvStart = (Button) findViewById(R.id.btn_start_record);
        Button tvImport = (Button) findViewById(R.id.btn_import);
        rgResolution = (RadioGroup) findViewById(R.id.rg_resolution);
        rgEncoder = (RadioGroup) findViewById(R.id.rg_encoder);
        rgQuality = (RadioGroup) findViewById(R.id.rg_quality);
        View llEncodeBItrate = findViewById(R.id.ll_encode_bitrate);
        llEncodeBItrate.setVisibility(DEBUG ? View.VISIBLE : View.GONE);
//        rgEncoder.setVisibility(View.GONE);
        mEtEncodeBitrate = (EditText) findViewById(R.id.et_encode_bitrate);
        mEtBitrate = (EditText) findViewById(R.id.et_output_bitrate);
        mEtFramerate = (EditText) findViewById(R.id.et_framerate);
        rgResolution.setOnCheckedChangeListener(mOnCheckedChangeListener);
        rgEncoder.setOnCheckedChangeListener(mOnCheckedChangeListener);
        rgQuality.setOnCheckedChangeListener(mOnCheckedChangeListener);

        tvBack.setOnClickListener(mOnClickListener);
        tvStart.setOnClickListener(mOnClickListener);
        tvImport.setOnClickListener(mOnClickListener);
    }

    /**
     * 初始化YfMediaKit及回调
     */
    private void initMediaKit() {

        RecorderJni.getInstance();
        mMediaKitCallback = new YfMediaKit.MediaKitCallback() {
            @Override
            public void onMediaHandledFinish(int id, final int result, final String path) {
                if (id == MERGE_INDEX) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingActivity.this, result == YfMediaKit.RESULT_SUCCESS ?
                                    "合并成功:" + path : "合并失败", Toast.LENGTH_SHORT).show();
                            if (result == YfMediaKit.RESULT_SUCCESS) {
                                gotoVideoCutActivity(path);
                            }
                        }
                    });
                }
            }
        };
        mYfMediaKit = new YfMediaKit(mMediaKitCallback);
    }

    YfMediaKit.MediaKitCallback mMediaKitCallback;

    @Override
    protected void onResume() {
        super.onResume();
        initMediaKit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaKitCallback = null;
        mYfMediaKit = null;
        Util.clearCacheFiles(false);
    }

    int resolution = Const.RESOLUTION_360;
    boolean hevcEncoder = false;
    boolean highQuality = true;
    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener
            = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            if (group == rgResolution)
                switch (checkedId) {
                    case R.id.rb_360:
                        resolution = Const.RESOLUTION_360;
                        break;
                    case R.id.rb_540:
                        resolution = Const.RESOLUTION_540;
                        break;
                }
            else if (group == rgEncoder)
                switch (checkedId) {
                    case R.id.rb_h264:
                        hevcEncoder = false;
                        break;
                    case R.id.rb_h265:
                        hevcEncoder = true;
                        break;
                }
            else if (group == rgQuality)
                switch (checkedId) {
                    case R.id.rb_low:
                        highQuality = false;
                        break;
                    case R.id.rb_high:
                        highQuality = true;
                        break;

                }
        }
    };

    private final int REQUEST_VIDEO_CODE = 0x101;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_back:
                    onBackPressed();
                    break;
                case R.id.btn_import:
                    configParams();
                    startImportActivity();
                    break;
                case R.id.btn_start_record:
                    configParams();
                    Intent intent = new Intent(SettingActivity.this, ChooseMusicActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    /**
     * 本地导入
     */
    private void startImportActivity() {
        final Intent intentImport = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter
        intentImport.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        intentImport.addCategory(Intent.CATEGORY_OPENABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intentImport.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        Intent intentChoose = Intent.createChooser(
                intentImport, "导入视频");
        try {
            startActivityForResult(intentChoose, REQUEST_VIDEO_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置参数
     */
    private void configParams() {
        int frameRate;
        try {
            frameRate = Integer.parseInt(mEtFramerate.getText().toString());
        } catch (NumberFormatException e) {
            frameRate = Const.DEFAULT_FRAMERATE;
        }

        int bitrate;
        try {
            bitrate = Integer.parseInt(mEtBitrate.getText().toString());
        } catch (NumberFormatException e) {
            bitrate = Const.DEFAULT_OUTPUT_BITRATE;
        }
        if (DEBUG) {
            int recordBitrate;
            try {
                recordBitrate = Integer.parseInt(mEtEncodeBitrate.getText().toString());
            } catch (NumberFormatException e) {
                recordBitrate = Const.DEFAULT_RECORD_BITRATE;
            }
            Configure.RECORD_BITRATE = recordBitrate;
        }
        Configure.BITRATE = bitrate;
        Configure.FRAME_RATE = frameRate;
        Configure.RESOLUTION = resolution;
        Configure.HEVC_ENCODER = hevcEncoder;
        Configure.HIGH_QUALITY = highQuality;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.util.Log.d(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        if (requestCode == REQUEST_VIDEO_CODE && resultCode == RESULT_OK) {

            List<Uri> uris = new ArrayList<>();
            ClipData clipData = data.getClipData();

            if (clipData != null) {
                //多选导入
                android.util.Log.d(TAG, "clipData.getItemCount(): " + clipData.getItemCount());
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    uris.add(item.getUri());
                    Log.i(TAG, "Uri = " + item.getUri());
                }
                startTranscodeAndMerge(uris);
            } else {
                //单选导入
                Uri uri = data.getData();
                File file = FileUtils.getFile(SettingActivity.this, uri);
                String videoPath = null;
                if (file != null && file.exists()) {
                    videoPath = file.getAbsolutePath();
                }
                boolean supportedMediaFile = isSupportedMediaFile(videoPath);
                Toast.makeText(this, supportedMediaFile ? "videoPath: " + videoPath :
                        FileUtils.getMimeType(videoPath) + " is Not Supported!", Toast.LENGTH_SHORT).show();
                if (supportedMediaFile) {
                    gotoVideoCutActivity(videoPath);
                }
            }

        }
    }

    /**
     * 判断文件类型是否支持
     *
     * @param videoPath 视频路径
     * @return 是否支持
     */
    private boolean isSupportedMediaFile(String videoPath) {
        String mimeType = FileUtils.getMimeType(videoPath);
        String[] SUPPORTED_MIME_TYPE = new String[]{
                "video/mp4",
                "video/ext-mp4",
//                "video/3gpp",
//                "video/mov"
        };
        for (String aSUPPORT_FILE_MIME_TYPE : SUPPORTED_MIME_TYPE) {
            if (mimeType.equals(aSUPPORT_FILE_MIME_TYPE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 合并视频（暂未做转码处理）
     *
     * @param uris 视频uris
     */
    private void startTranscodeAndMerge(List<Uri> uris) {
        ArrayList<YfVideo> videoList = new ArrayList<>();
        for (Uri uri : uris) {
            File file = FileUtils.getFile(SettingActivity.this, uri);
            if (file != null && file.exists() && file.isFile()) {
                String absolutePath = file.getAbsolutePath();
                if (isSupportedMediaFile(absolutePath)) {
                    videoList.add(new YfVideo(0, absolutePath));
                } else {
                    Toast.makeText(SettingActivity.this, FileUtils.getMimeType(absolutePath)
                            + " is Not Supported!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (videoList.isEmpty()) return;
        String savePath = Const.PATH_RECORD + File.separator + Util.getFormatTime() + "_merge_import.mp4";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("合并中");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        // TODO: 2017/8/24  可能需要转码
        mYfMediaKit.mergeMedia(videoList, savePath, 0, MERGE_INDEX);

    }

    private void gotoVideoCutActivity(String videoPath) {
        Intent intent = new Intent(SettingActivity.this, VideoCutActivity.class);
        intent.putExtra(Const.KEY_PATH_MUX_VIDEO, videoPath);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
