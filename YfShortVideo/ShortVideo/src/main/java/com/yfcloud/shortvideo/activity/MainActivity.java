package com.yfcloud.shortvideo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yfcloud.shortvideo.BuildConfig;
import com.yfcloud.shortvideo.R;
import com.yunfan.auth.YfAuthentication;
import com.yunfan.encoder.widget.YfEncoderKit;

/**
 * 首页（鉴权）
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Yf_MainActivity";
    private static final String AccessKey = "65592eeddc3d646db903a7367d58792268804f09";
    private static final String Token = "9e7299afc12d793a23d913d88be6fa6383f5876e";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvVersion = (TextView) findViewById(R.id.tv_version);
        tvVersion.setText("v" + BuildConfig.VERSION_NAME + "_" + YfEncoderKit.getSDKVersion());
        //发起鉴权
        YfAuthentication.getInstance().authenticate(AccessKey, Token, mCallback);
        findViewById(R.id.tv_music_short_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (YfAuthentication.getInstance().isAuthenticateSucceed()) {
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    finish();
                } else
                    Toast.makeText(MainActivity.this, "鉴权失败", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //如果因为网络异常而鉴权失败，可以在恢复网络的时候确认一下是否鉴权成功，如果没有则再次发起鉴权。
    private YfAuthentication.AuthCallBack mCallback = new YfAuthentication.AuthCallBack() {
        @Override
        public void onAuthenticateSuccess() {
            Log.d(TAG, "onAuthenticateSuccess: ");
        }

        @Override
        public void onAuthenticateError(int errorCode) {
            Log.d(TAG, "onAuthenticateError: " + errorCode);
        }
    };
}
