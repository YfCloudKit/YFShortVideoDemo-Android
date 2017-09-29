/**
 * @版权 : 深圳云帆世纪科技有限公司
 * @作者 : 刘群山
 * @日期 : 2015年4月20日
 */
package com.yfcloud.shortvideo.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;


public class Util {
    public static final String TAG = "Yf_Util";

    public static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

    public static int getScreenWidth(Context context) {
        final Point p = new Point();
        final Display d = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        d.getSize(p);
        if (p.x > p.y) {
            return p.y;
        } else {
            return p.x;
        }
    }

    public static int getDisplayWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    public static int dip2px(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((float) dip * scale + 0.5F);
    }

    public static int getScreenHeight(Context context) {
        final Point p = new Point();
        final Display d = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        d.getSize(p);
        if (p.x > p.y) {
            return p.x;
        } else {
            return p.y;
        }
    }

    public static boolean deleteFile(String path) {
        if (path != null) {
            File file = new File(path);
            boolean exists = file.exists();
            if (exists) {
                return file.delete();
            }
        }
        return false;
    }

    public static void CopyAssets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    CopyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean copyAssetsFileToSD(Context context, String path, String[] filenames, String[] savedNames) {
        android.util.Log.d(TAG, "copyAssetsFileToSD: " + path + Arrays.toString(filenames));
        long startTime = System.currentTimeMillis();
        File yunfanPath = new File(path);
        if (!yunfanPath.exists()) {
            yunfanPath.mkdirs();
        }
        for (int i = 0; i < filenames.length; i++) {
            AssetManager am = context.getAssets();
            try {
                InputStream inputStream = am.open(filenames[i]);
                File file = new File(path, savedNames[i]);
                if (file.exists()) {
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(file);
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        long copyTime = System.currentTimeMillis() - startTime;
        android.util.Log.d(TAG, "copyAssetsFileToSD copyTime: " + copyTime);
        return true;
    }

    public static final String ping(String ip) {
        Log.d(TAG, "start to ping in thread:" + ip);
        String result = null;
        try {

//            String ip = "www.baidu.com";// 除非百度挂了，否则用这个应该没问题~

            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);//ping3次
// 读取ping的内容，可不加。
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.e(TAG, "result content : " + stringBuffer.toString());
// PING的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                return result;
            } else {
                result = "failed~ cannot reach the IP address";
            }
        } catch (IOException e) {
            result = "failed~ IOException";
        } catch (InterruptedException e) {
            result = "failed~ InterruptedException";
        } finally {
            Log.e(TAG, "result = " + result);
        }
        return result;

    }

    static StringBuilder mFormatBuilder = new StringBuilder();
    static Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static String getFormatTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM_dd_HH_mm_ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    /**
     * 保存到视频到本地，并插入MediaStore以保证相册可以查看到
     * 这是更优化的方法，防止读取的视频获取不到宽高
     *
     * @param context    上下文
     * @param filePath   文件路径
     * @param createTime 创建时间 <=0时为当前时间 ms
     * @param duration   视频长度 ms
     * @param width      宽度
     * @param height     高度
     */
    public static boolean insertVideoToMediaStore(Context context, String filePath, long createTime,
                                                  int width, int height, long duration) {
        if (filePath != null) {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                return false;
            }
        }

        createTime = getTimeWrap(createTime);
        try {
            ContentValues values = initCommonContentValues(filePath, createTime);
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, createTime);
            if (duration > 0)
                values.put(MediaStore.Video.VideoColumns.DURATION, duration);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                if (width > 0) values.put(MediaStore.Video.VideoColumns.WIDTH, width);
                if (height > 0) values.put(MediaStore.Video.VideoColumns.HEIGHT, height);
            }
            values.put(MediaStore.MediaColumns.MIME_TYPE, getVideoMimeType(filePath));
            context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 插入时初始化公共字段
     *
     * @param filePath 文件
     * @param time     ms
     * @return ContentValues
     */
    private static ContentValues initCommonContentValues(String filePath, long time) {
        ContentValues values = new ContentValues();
        File saveFile = new File(filePath);
        long timeMillis = getTimeWrap(time);
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, timeMillis);
        values.put(MediaStore.MediaColumns.DATE_ADDED, timeMillis);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        return values;
    }

    // 获取video的mine_type,暂时只支持mp4,3gp
    private static String getVideoMimeType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith("mp4") || lowerPath.endsWith("mpeg4")) {
            return "video/mp4";
        } else if (lowerPath.endsWith("3gp")) {
            return "video/3gp";
        }
        return "video/mp4";
    }

    // 获得转化后的时间
    private static long getTimeWrap(long time) {
        if (time <= 0) {
            return System.currentTimeMillis();
        }
        return time;
    }

    /**
     * 删除一些垃圾...
     */
    public static void clearCacheFiles(final boolean onlyThumbnails) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File thumbnails = new File(Const.PATH_THUMBNAILS);
                if (thumbnails.exists() && thumbnails.isDirectory()) {
                    File[] files = thumbnails.listFiles();
                    for (File f : files) {
                        f.delete();
                    }
                }
                if (!onlyThumbnails) {
                    File records = new File(Const.PATH_RECORD);
                    if (records.exists() && records.isDirectory()) {
                        File[] files = records.listFiles();
                        if (files.length > 50) {
                            for (File f : files) {
                                if (!f.getName().contains("test") || !f.getName().contains("mux")) {
                                    f.delete();
                                }
                            }
                        }
                    }
                }
            }
        }).start();
    }

}
