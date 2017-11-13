# YfShortVideo

* 该项目为**Android短视频demo**示例代码。该demo演示了分段录制、变速录制、视频裁剪、滤镜特效和时间特效等功能。
* [短视频sdk文档地址](http://doc.yfcloud.com/index.php?s=/5&page_id=36)
* [下载安装短视频demo](http://www.yfcloud.com/yunfansdkdownload/YFShortVideoSDK_Android_demo.apk)

 ![短视频demo](https://i.imgur.com/Qnmer1v.png)

---

#### 1. 开发准备
准备 Android 开发环境：安装有 ADT 插件的 Eclipse 开发环境或Android Studio

#### 2. SDK下载
在云帆加速官网下载最新的[短视频 Android SDK](https://github.com/YfCloudKit/YfKitLibrary "短视频 Android SDK")

#### 3. SDK集成
本文是根据官网的短视频 Demo 来介绍 SDK 的集成，可在云帆加速官网下载最新的[短视频 Android Demo](https://github.com/YfCloudKit/YfShortVideo-Android "短视频 Android Demo")，来查看更多的实现细节。

##### 3.1 创建新工程
##### 3.2 导入SDK
SDK目录结构说明。如下图所示：
```
根据实际架构替换目前展示的图
Demo Project-SDK-Android-x.x.x
       │
       ├── libs
       │    │
       │    ├── YfEncoder.jar          // 推流 SDK Java依赖包
	   │    ├── YfPlayerKit.jar          // 推流 SDK Java依赖包
       │    │
       │    ├── armeabi-v7a            // armv7/armv7a 架构的动态链接库
       │    │    ├── libyfplayer.so
       │    │    ├── libyfsdl.so
       │    │    ├── libname.so
       │    │    ├── libffmpeg.so
       │    │    ├── libmuxer.so
       │    │    └── libmuxer-m.so
       │    │
       │    └── arm64-v8a                         // arm64 架构的动态链接库
       │          ├── libyfplayer.so
       │          ├── libyfsdl.so
       │    　    ├── libname.so
       │    　    ├── libffmpeg.so
       │          └── libmuxer.so
       │
       │
       └── demo                         // 示例工程，用于演示推流 API 的使用
       　    ├── res                   // 工程资源文件目录
       　    ├── libs                  // Java 依赖包以及动态链接库文件目录
       　    ├── src                  // 源代码文件目录
       　    └──  ... ...               // 其他工程文件
```
如果是使用Android Studio开发，别忘了在module的build.gradle目录添加对YfEncoderKit.jar及YfPlayerKit.jar的依赖。
也可以通过在线依赖的方式集成，详情参考[github项目](https://github.com/YfCloudKit/YfKitLibrary "github项目")
##### 3.3 配置权限
直播推流SDK需要蓝牙、摄像头、录音、访问网络、读写SD卡、获取设备信息等权限。在AndroidManifest.xml中加入以下配置：
```
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```
##### 3.4 鉴权
获取SDK使用许可的Token，在app启动的时候调用全局静态鉴权方法，这里需要对推流sdk及播放sdk都进行鉴权。
```
 YfAuthentication.AuthCallBack mCallback = new YfAuthentication.AuthCallBack() {
        @Override
        public void onAuthenticateSuccess() {
            Log.d(TAG, "onAuthenticateSuccess: ");
        }

        @Override
        public void onAuthenticateError(int errorCode) {
            Log.d(TAG, "onAuthenticateError: " + errorCode);
        }
    };
YfAuthentication.getInstance().authenticate(AccessKey, Token, mCallback);
```
为了防止在鉴权的时候因为网络异常而鉴权失败，可以在恢复网络的时候确认一下是否鉴权成功，如果没有则再次发起鉴权。
```
 if(!EncoderAuthentication.getInstance().isAuthenticateSucceed()){
        //retry
    }  
```

##### 3.5 基础录制功能集成
1、实现回调接口YfEncoderKit.RecordMonitor
    
```
 	/**
     * 成功连接上服务器
     */
    void onServerConnected();

    /**
     * 错误回调
     *
     * @param mode
     * @param err
     * @param msg
     */
    void onError(int mode,int err, String msg);

    /**
     * 录制状态发生变化
     *
     * @param mode
     * @param oldState
     * @param newState
     */
    void onStateChanged(int mode, int oldState,
                        int newState);

    /**
     * 生成了新的录制片段
     *
     * @param mode
     * @param fragPath
     */
    void onFragment(int mode, String fragPath);

    /**
     * 底层信息的回调，包括IP等
     *
     * @param what 消息类型
     * @param arg1
     * @param arg2
     * @param obj
     */
    void onInfo(int what, int arg1, int arg2, Object obj);

```


2、初始化编码器并配置摄像头及预览界面

```
 //初始化编码工具：context、截图/录制视频等文件保存的根目录、摄像头输出宽度、摄像头输出高度、编码宽度、编码高度、是否硬编、视频帧率
mYfVodKit = new YfVodKit(this, CACHE_DIRS, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                VIDEO_WIDTH, VIDEO_HEIGHT, mHardEncoder, VIDEO_FRAME_RATE, mInputBitrate, this);
```
3、注册生命周期

```
    @Override
    protected void onResume() {
        super.onResume();
        if (mYfVodKit != null)
            mYfVodKit.onResume();
    }
```

```
    @Override
    protected void onStop() {
        super.onStop();
		if (mYfVodKit != null)
            mYfVodKit.onStop();
    }
```
```
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        mYfVodKit.release();
    }
```
4、开始录制本地视频

```
mYfVodKit.startRecord();
```

5、暂停本地录制
    
```
mYfVodKit.pauseRecord()；
```
6、删除一段视频
```
mYfVodKit.deleteVideo(mYfVodKit.getVideoList().getLast().getId());
```
7、设置是否视频倒流
```
mYfVodKit.enableReverseVideo(mCbReverse.isChecked());
```
8、完成录制
```
mYfVodKit.finishRecord();
```
通过上述步骤，我们就可以简单地录制出一段本地视频了。
##### 3.6 基础播放功能集成
1、初始化播放器，详细代码可参考Demo。在本例中，我们要使用播放器播放一段音频，并用该音频与录制的视频进行混合。
```
private YfCloudPlayer mYfCloudPlayer;
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
	mYfCloudPlayer.setDisplay(null);
    try {
    	mYfCloudPlayer.setDataSource(mAudioPath);
	} catch (IOException e) {
    	e.printStackTrace();
	}
    mYfCloudPlayer.prepareAsync();
}
 
```
2、播放/倍速播放、暂停、停止音乐
```
	private void playMusic() {
        startPlayMusic = true;
        if (prepared) {
            Log.d(TAG, "开始播放音乐~~~");
            //每次播放音乐时记录当前时间点
            mYfCloudPlayer.setSpeed(mSpeed);
            mYfCloudPlayer.start();
            musicIsPaused = false;
        }
    }
    private void pauseMusic() {
        if (mYfCloudPlayer != null && mYfCloudPlayer.isPlaying()) {
            mYfCloudPlayer.pause();
            musicIsPaused = true;
        }
    }

    private void releasePlayer() {
        if (mYfCloudPlayer != null) {
            mYfCloudPlayer.reset();
            mYfCloudPlayer.release();
            mYfCloudPlayer = null;
        }
    }
```
希望倍速播放功能，调用此方法即可（在上述代码playMusic()方法中）
```
mYfCloudPlayer.setSpeed(mSpeed);
```
##### 3.7 多段变速录制流程
以下为短视频制作的经典流程，可根据项目需求自定义流程。
实现目标：多段倍速录制、使用歌曲音频替代录制音频
```
/**
 * 多段变速录制步骤：
 * 0、拖动绿色滑块选择音频起始时间，并点击确定
 * 1、设置播放器播放速度
 * 2、准备音乐
 * 3、开始录制
 * 4、推流回调第一帧视频帧可用时，立即播放音乐
 * 5、在音乐开始播放第一帧音频帧时，通知编码器开始正式编码视频
 * 6、停止第一段录制、暂停音乐
 * <p>
 * 7、设置音频播放速度并开始第二段录制
 * 8、推流回调第一帧视频帧可用时，立即播放音乐，因为播放器从暂停状态到播放状态速度很快，需立即通知编码器开始正式编码
 * 9、停止第二段录制、暂停音乐
 * 10、保存当前音频播放位置
 * <p>
 * 11、删除第二段录制的视频
 * 12、音乐播放器seek回步骤6里的位置
 * 13、编码器重置时间戳为步骤6里的位置
 * <p>
 * 14、重复7~13直至最大录制时长
 * 15、合成之前设置是否倒流视频
 * 16、点击合成，开始合成
 * 17、设置起始时间截取视频片段
 */
```
##### 3.8 高级录制使用方法
下面我们开始按照步骤3.7的流程进行更高级有趣的短视频制作。
因为制作的短视频是使用外部音频流和录制的视频进行合成，为了使后期合成的音视频能同步，需要进行一点额外的操作。
1、视频录制与音频播放保持同步开始
1.1 在相关回调中对播放器进行对应操作
```
mYfVodKit.setInputPlayer(mPlayerInterface);
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
```
1.2 当音频帧开始播放时，通知编码器可以开始录制视频，即播放器要监听onInfoListener
```
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
```
2、录制时长
录制视频的计时请以mYfVodKit的内部时间戳为准，即通过以下方式获得：
```
//实时获取当前录制时间戳，可根据该时间戳绘制UI
mYfVodKit.getCurrentRecordTimestamp()；
```
3、删除视频
```
mYfVodKit.deleteVideo(mYfVodKit.getVideoList().getLast().getId());
```
4、截取视频
```
mYfVodKit.splitMedia(mMuxPath, cutStartTime, cutEndTime);
```
##### 3.9 媒体编辑
YfEncoderKit的SDK提供了一个视频编辑的工具集合YfMediaKit，可以对媒体文件进行混流、拼接、裁剪、转码等功能。
1、初始化YfMediaKit并设置任务监听
```
private YfMediaKit mYfMediaKit = new YfMediaKit(mediaCallback);
private final int MERGE_INDEX = 10086, REVERSE_INDEX = 10088, 
	MUX_INDEX = 10000, SPLIT_INDEX = 10010, TRANSCODE_INDEX = 11111;
	//用于标识任务ID，必须定义为不一样的数值以作区分
private YfMediaKit.MediaKitCallback mediaCallback = new YfMediaKit.MediaKitCallback() {

        @Override
        public void onMediaHandledFinish(final int id, final int result, String path) {
            //拼接视频、音视频合成、裁剪
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, String.format("id: %d result: %d", id, result));
                    switch (id) {
                        case MERGE_INDEX:
                            onMergeFinish(result);
                            break;
                        case REVERSE_INDEX:
                            onReverseFinish(result);
                            break;
                        case TRANSCODE_INDEX:
                            onTransCodeFinish(result);
                            break;
                        case MUX_INDEX:
                            onMuxFinish(result);
                            break;
                        case SPLIT_INDEX:
                            onSplitFinish(result);
                            break;
                    }
                }
            });

        }
    };
```
2、通过多次录制生成多个片段后，可以调用方法将多段视频合成(请自行管理之前录制的视频)
```
public void mergeMedia(List<YfVideo> videoList, String savePath, int tag) 
```
3、合成视频之后，如果希望实现倒叙播放，则调用此方法：
```
public void reverseMedia(String inputFile, String outputFile, int id)
```
4、然后进行音视频混流，混流前需要将音频文件转码兼容性更好的音频文件，可以在播放音乐即录制视频的同时对音频进行转码，以缩短流程。
```
public void transcodeMedia(String inputFile, String outputFile, int id)
```
接着将视频文件与转码后的音频文件进行混流：
```
public void muxMedia(String audioFile, String videoFile, String savePath, double startTime,int id)
```
5、视频裁剪。
通过以下方法可以对视频进行裁剪，以保留需要的部分：
```
public void splitMedia(String videoPath, int startIndexMs, int endIndexMs, String savePath, int tag) 
```
**以上视频编辑步骤可以任意调整顺序，根据需求进行操作。**
##### 3.10 视频处理
对于录制成功后的视频，我们提供了丰富的滤镜特效及时间特效用于后期添加。并且支持实时预览。其中，所有的滤镜特效均继承自BaseFilter，不管是预览还是后期视频输出，均用的一套特效添加方式。
###### 3.11 实时预览
通过将视频的渲染控件指定为YfGlSurfaceView，利用该控件的接口，可以对视频实时添加滤镜效果。其中，主要通过以下两个接口添加/移除滤镜。
```
    public void addFilter(BaseFilter filter)
```

```
    public void removeFilter(int index)
```
通过下面这个接口获取到内置的Surface，然后将播放器的渲染目标设置为该Surface即可
```
    public Surface getSurface()
```
示例代码：
```
//使用YfGlSurfaceView渲染视频
mYfCloudPlayer.setSurface(mYfGlSurfaceView.getSurface());

//...
//添加滤镜
final int INDEX=10088;
YfWhiteCatFilter temp = new YfWhiteCatFilter();
temp.setIndex(INDEX);
mYfGlSurfaceView.addFilter(temp);
//...
//移除滤镜：
mYfGlSurfaceView.removeFilter(INDEX);

```
###### 3.12 时间特效
时光倒流特效通过YfMediaKit实现即可。
慢动作及重复动作均通过YfPlayerKit/YfCloudPlayer实现。接口如下：
```
void setVideoSpeed(double startMs, double durationMs, double speed);
//慢动作，将时间设置为Double.NaN表示取消动作
```
```
void setVideoRepeat(double startMs, double durationMs, int repeatTimes);
//重复动作，将时间设置为Double.NaN表示取消动作
```
 
###### 3.13 输出视频
输出添加各种特效后的视频主要依赖YfMediaEditor2这个类。
主要方法如下：
1、添加滤镜列表
```
public void addFilters(List<BaseFilter> filters)
```
2、视频输出配置
```
public void setBitrate(int bitrate)//输出码率
```
```
public void setSize(int width, int height)//输出分辨率，需设置为与原视频一致
```
3、高级配置
```
public void encodeHEVC(boolean enable)//输出HEVC视频格式
```
```
public void changeVideoSpeed(double start, double duration, double ratio)
//在指定时间段更改视频播放速度（不影响音频）
```
```
public void repeatVideo(double start, double duration, int times)
//在指定时间段重复播放视频（不影响音频）
```
4、开始处理
```
public void setSaveProgressListener(long duration, SaveProgressListener saveProgressListener)
//设置处理进度回调
```
```
public void startEdit(Context context, String sourcePath, String outputPath)
//开始处理视频
```
#### 4. 注意事项
官网或github上下载的sdk仅支持使用测试域名进行推流/播放，正式使用请与我们联系获取正式token并设置推流域名。

github地址：https://github.com/YfCloudKit/YfShortVideo-Android






