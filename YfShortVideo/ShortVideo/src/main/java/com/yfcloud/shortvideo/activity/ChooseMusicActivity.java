package com.yfcloud.shortvideo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.adapter.MusicAdapter;
import com.yfcloud.shortvideo.utils.Const;
import com.yfcloud.shortvideo.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 选择录制的背景音乐，音乐路径{@link Const#PATH_AUDIO}
 */
public class ChooseMusicActivity extends AppCompatActivity {
    private static final String TAG = "Yf_ChooseMusicActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_music);
        initView();
    }

    private void copyGifToSdCard() {
        String[] musics = getResources().getStringArray(R.array.effect_gif_name);
        boolean copy = Util.copyAssetsFileToSD(ChooseMusicActivity.this, Const.PATH_GIF,
                musics, musics);
        Log.d(TAG, "copyAssetsFileToSD: " + copy);
    }

    private List<String> copyMp3ToSdCard() {
        String[] musics = getResources().getStringArray(R.array.music_name);
        boolean copy = Util.copyAssetsFileToSD(ChooseMusicActivity.this, Const.PATH_AUDIO,
                musics, getResources().getStringArray(R.array.music_saved_name));
        Log.d(TAG, "copyAssetsFileToSD: " + copy);
        File file = new File(Const.PATH_AUDIO);
        List<String> musicList = new ArrayList<>();
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                for (File f : files) {
                    musicList.add(f.getAbsolutePath());
                }
            }
        }
        return musicList;
    }

    private void initView() {
        findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView textView = (TextView) findViewById(R.id.tv_title);
        textView.setText("选择音乐");

        List<String> strings = copyMp3ToSdCard();
        ListView lvMusic = (ListView) findViewById(R.id.lv_music);
        MusicAdapter musicAdapter = new MusicAdapter(strings, this);
        musicAdapter.setOnItemClickListener(new MusicAdapter.ItemClickListener() {
            @Override
            public void onItemClickListener(String path) {
                Intent intent = new Intent(ChooseMusicActivity.this, VideoRecordActivity.class);
                intent.putExtra(Const.KEY_PATH_AUDIO, path);
                startActivity(intent);
            }
        });
        lvMusic.setAdapter(musicAdapter);

        copyGifToSdCard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
