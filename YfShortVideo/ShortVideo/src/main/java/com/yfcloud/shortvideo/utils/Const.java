package com.yfcloud.shortvideo.utils;

import android.os.Environment;

/**
 * Created by yunfan on 2017/7/25.
 */

public class Const {
    /**
     * 短视频相关文件缓存根目录
     */
    public static String PATH = Environment.getExternalStorageDirectory() + "/yunfanencoder";
    /**
     * 音频目录，可自行拷贝音乐至该目录
     */
    public static String PATH_AUDIO = PATH + "/audio";
    /**
     * gif图标目录
     */
    public static String PATH_GIF = PATH + "/gif";
    /**
     * 视频帧缩略图目录
     */
    public static String PATH_THUMBNAILS = Const.PATH + "/Thumbnails";
    /**
     * 录制的视频目录，目录保存有分段录制的视频、转码后的音频、合并分段的视频的视频、
     */
    public static String PATH_RECORD = Const.PATH + "/Record";
    /**
     * 点播地址
     */
    public static String PATH_PLAY = "http://hls.yfzk-test.yflive.net/yflive/2017/";

    /**
     * 最大录制时长
     */
    public static float MAX_RECORD_TIME = 15 * 1000f;
    /**
     * 最小录制时长
     */
    public static float MIN_RECORD_TIME = 5 * 1000f;
    /**
     * 输出分辨率
     */
    public static int RESOLUTION_360 = 360;
    public static int RESOLUTION_540 = 540;
    /**
     * 默认输出码率
     */
    public static int DEFAULT_OUTPUT_BITRATE = 3000;
    /**
     * 默认录制码率
     */
    public static int DEFAULT_RECORD_BITRATE = 5000;
    /**
     * 默认帧率
     */
    public static int DEFAULT_FRAMERATE = 24;

    /*****intent相关******/
    public static String KEY_PATH_AUDIO = "audio_path";
    public static String KEY_PATH_MUX_VIDEO = "mux_video_path";
    public static String KEY_VIDEO_PLAY_URL = "video_play_url";
    public static String KEY_OUTPUT_RESOLUTION = "KEY_OUTPUT_RESOLUTION";
    public static String KEY_OUTPUT_ENCODER = "KEY_ENCODER_MODE";
    public static String KEY_OUTPUT_BITRATE = "KEY_OUTPUT_BITRATE";
    public static String KEY_OUTPUT_FRAMERATE = "KEY_OUTPUT_FRAMERATE";
}
