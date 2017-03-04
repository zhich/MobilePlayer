package com.zch.mobileplayer.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zch.mobileplayer.R;
import com.zch.mobileplayer.constant.IntentConstant;
import com.zch.mobileplayer.entity.MediaItem;
import com.zch.mobileplayer.utils.CommonUtils;
import com.zch.mobileplayer.utils.ListUtils;
import com.zch.mobileplayer.utils.TimeUtils;
import com.zch.mobileplayer.utils.ToastUtils;
import com.zch.mobileplayer.view.VideoView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 系统视频播放器
 * Created by zch on 2017/3/4.
 */
public class SystemVideoPlayerActivity extends BaseActivity {

    private static final int PROGRESS = 1;//视频进度更新
    private static final int HIDE_MEDIA_CONTROLLER = 2;//隐藏控制面板
    private static final int SHOW_SPEED = 3;//显示网络速度

    private static final int FULL_SCREEN = 11;//全屏
    private static final int DEFAULT_SCREEN = 12;//默认屏幕

    private static final long SHOW_MEDIA_CONTROLLER_TIME = 4000;//显示控制面板的时间
    private static final long UPDATE_NET_SPEED_INTERVAL = 2000;//更新网速时间间隔
    private static final long UPDATE_PROGRESS_INTERVAL = 1000;//更新视频进度的时间间隔

    @BindView(R.id.systemVideo_vv_videoView)
    VideoView mVideoView;
    @BindView(R.id.mediaController_iv_battery)
    ImageView mBatteryIv;
    @BindView(R.id.mediaController_seekbar_video)
    SeekBar mVideoSeekbar;
    @BindView(R.id.mediaController_tv_duration)
    TextView mDurationTv;
    @BindView(R.id.systemVideo_view_mediaController)
    RelativeLayout mMediaControllerView;
    @BindView(R.id.mediaController_btn_switchScreenVideo)
    Button mSwitchScreenVideoBtn;
    @BindView(R.id.systemVideo_view_loading)
    LinearLayout mLoadingView;
    @BindView(R.id.mediaController_tv_videoName)
    TextView mVideoNameTv;
    @BindView(R.id.mediaController_btn_preVideo)
    Button mPreVideoBtn;
    @BindView(R.id.mediaController_btn_nextVideo)
    Button mNextVideoBtn;
    @BindView(R.id.mediaController_btn_startOrPauseVideo)
    Button mStartOrPauseBtn;
    @BindView(R.id.mediaController_tv_currentTime)
    TextView mCurrentTimeTv;
    @BindView(R.id.mediaController_tv_systemTime)
    TextView mSystemTimeTv;
    @BindView(R.id.systemVideo_view_buffer)
    LinearLayout mBufferView;
    @BindView(R.id.mediaController_seekbar_voice)
    SeekBar mVoiceSeekbar;
    @BindView(R.id.laoding_tv_netSpeed)
    TextView mLoadingNetSpeedTv;
    @BindView(R.id.buffer_tv_netSpeed)
    TextView mBufferNetSpeedTv;

    private MyReceiver myReceiver;//监听电量变化，只能以动态注册方式注册广播
    private boolean mIsUseSystemJudgeLag = true;//是否使用系统的方式来监听视频卡顿
    private boolean mIsShowMediaController;//是否显示控制面板
    private boolean mIsFullScreen;//是否全屏
    private Uri mUri;//传入进来的视频地址
    private ArrayList<MediaItem> mMediaItemList;//传入进来的视频列表
    private int mPosition;//要播放的列表中的具体位置
    private boolean mIsNetUri;//是否网络Uri
    private int mPrePlayPosition;//上一次的播放进度
    private GestureDetector mGestureDetector;//手势识别器
    private AudioManager mAudioManager;//声音管理器
    private int mCurrentVoice;//当前的音量
    private int mMaxVoice;//最大音量 0~15
    private boolean mIsMute;//是否静音

    /**
     * 视频实际宽高
     */
    private int mVideoWidth;
    private int mVideoHeight;

    /**
     * 屏幕宽高
     */
    private int mScreenWidth;
    private int mScreenHeight;

    private float mStartY;
    private float mTouchRang;//屏幕的高
    private int mVol;//当一按下的音量
    private Vibrator mVibrator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_video_player);
        ButterKnife.bind(this);

        init();
        setListener();
    }

    @Override
    protected void onDestroy() {
        if (null != myReceiver) {
            unregisterReceiver(myReceiver);
            myReceiver = null;
        }
        if (null != myHandler) {
            myHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //把事件传递给手势识别器
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = event.getY();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mTouchRang = Math.min(mScreenWidth, mScreenHeight);//mScreenHeight，因为横屏
                myHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                float endX = event.getX();
                float distanceY = mStartY - endY;

                if (endX < mScreenWidth / 2) {
                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }
                } else {
                    //右边屏幕-调节声音
                    //改变声音 = （滑动屏幕的距离： 总距离）*音量最大值
                    float delta = (distanceY / mTouchRang) * mMaxVoice;
                    //最终声音 = 原来的 + 改变声音；
                    int voice = (int) Math.min(Math.max(mVol + delta, 0), mMaxVoice);
                    if (delta != 0) {
                        mIsMute = false;
                        updataVoice(voice, mIsMute);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                myHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, SHOW_MEDIA_CONTROLLER_TIME);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 监听物理健，实现声音的调节大小
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mCurrentVoice--;
            updataVoice(mCurrentVoice, false);
            myHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
            myHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, SHOW_MEDIA_CONTROLLER_TIME);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mCurrentVoice++;
            updataVoice(mCurrentVoice, false);
            myHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
            myHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, SHOW_MEDIA_CONTROLLER_TIME);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick({R.id.mediaController_btn_voice, R.id.mediaController_btn_switchPlayer,
            R.id.mediaController_btn_exitVideo, R.id.mediaController_btn_preVideo,
            R.id.mediaController_btn_startOrPauseVideo, R.id.mediaController_btn_nextVideo,
            R.id.mediaController_btn_switchScreenVideo})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mediaController_btn_voice:
                mIsMute = !mIsMute;
                updataVoice(mCurrentVoice, mIsMute);
                break;
            case R.id.mediaController_btn_switchPlayer:
                showSwitchPlayerDialog();
                break;
            case R.id.mediaController_btn_exitVideo:
                finish();
                break;
            case R.id.mediaController_btn_preVideo:
                playPreVideo();
                break;
            case R.id.mediaController_btn_startOrPauseVideo:
                setStartOrPause();
                break;
            case R.id.mediaController_btn_nextVideo:
                playNextVideo();
                break;
            case R.id.mediaController_btn_switchScreenVideo:
                setFullScreenOrDefault();
                break;
            default:
                break;
        }
        myHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
        myHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, SHOW_MEDIA_CONTROLLER_TIME);
    }

    private void init() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //当电量变化的时候发这个广播
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(myReceiver, intentFilter);

        //实例化手势识别器，并且重写双击，点击，长按
        mGestureDetector = new GestureDetector(mContext, new MySimpleOnGestureListener());

        Intent intent = getIntent();
        mUri = intent.getData();
        mMediaItemList = (ArrayList<MediaItem>) intent.getSerializableExtra(IntentConstant.VIDEO_LIST);
        mPosition = intent.getIntExtra(IntentConstant.POSITION, 0);

        if (!ListUtils.isEmpty(mMediaItemList)) {
            MediaItem mediaItem = mMediaItemList.get(mPosition);
            mVideoNameTv.setText(mediaItem.name);
            mIsNetUri = CommonUtils.isNetUri(mediaItem.data);
            mVideoView.setVideoPath(mediaItem.data);
        } else if (null != mUri) {
            mVideoNameTv.setText(mUri.toString());
            mIsNetUri = CommonUtils.isNetUri(mUri.toString());
            mVideoView.setVideoURI(mUri);
        } else {
            ToastUtils.showToastLong(mContext, mContext.getString(R.string.tip_get_no_data));
        }
        setButtonState();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mCurrentVoice = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mVoiceSeekbar.setMax(mMaxVoice);
        mVoiceSeekbar.setProgress(mCurrentVoice);

        myHandler.sendEmptyMessage(SHOW_SPEED);//开始更新网络速度
    }

    private void setListener() {
        mVideoView.setOnPreparedListener(new OnVideoPreparedListener());
        mVideoView.setOnErrorListener(new OnVideoErrorListener());
        mVideoView.setOnCompletionListener(new OnVideoCompletionListener());

        mVideoSeekbar.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        mVoiceSeekbar.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        if (mIsUseSystemJudgeLag) {
            //监听视频播放卡-系统的api
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mVideoView.setOnInfoListener(new MyOnInfoListener());
            }
        }
    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:
                    //当前的视频播放进度
                    int currentPosition = mVideoView.getCurrentPosition();
                    mVideoSeekbar.setProgress(currentPosition);
                    mCurrentTimeTv.setText(TimeUtils.str2Time(currentPosition));//播放进度时间
                    mSystemTimeTv.setText(TimeUtils.getSystemTime());
                    if (mIsNetUri) {
                        //只有网络资源才有缓存效果
                        int buffer = mVideoView.getBufferPercentage();//0~100
                        int totalBuffer = buffer * mVideoSeekbar.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        mVideoSeekbar.setSecondaryProgress(secondaryProgress);
                    } else {
                        //本地视频没有缓冲效果
                        mVideoSeekbar.setSecondaryProgress(0);
                    }
                    //监听视频是否卡顿
                    if (!mIsUseSystemJudgeLag) {
                        if (mVideoView.isPlaying()) {
                            int buffer = currentPosition - mPrePlayPosition;
                            if (buffer < 500) {
                                //视频卡了
                                mBufferView.setVisibility(View.VISIBLE);
                            } else {
                                //视频不卡了
                                mBufferView.setVisibility(View.GONE);
                            }
                        } else {
                            mBufferView.setVisibility(View.GONE);
                        }
                    }
                    mPrePlayPosition = currentPosition;

                    //3.每秒更新一次
                    myHandler.removeMessages(PROGRESS);
                    myHandler.sendEmptyMessageDelayed(PROGRESS, UPDATE_PROGRESS_INTERVAL);
                    break;
                case HIDE_MEDIA_CONTROLLER:
                    hideMediaController();
                    break;
                case SHOW_SPEED:
                    //得到网络速度
                    String netSpeed = CommonUtils.getNetSpeed(mContext);

                    //显示网络速
                    mLoadingNetSpeedTv.setText(mContext.getString(R.string.tip_loading) + netSpeed);
                    mBufferNetSpeedTv.setText(mContext.getString(R.string.tip_cacheing) + netSpeed);

                    //每两秒更新一次
                    myHandler.removeMessages(SHOW_SPEED);
                    myHandler.sendEmptyMessageDelayed(SHOW_SPEED, UPDATE_NET_SPEED_INTERVAL);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 该页面的广播
     */
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra("level", 0);//0~100;
                setBattery(level);
            }
        }
    }

    /**
     * 视频准备好的监听
     */
    class OnVideoPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            mVideoView.start();

            int duration = mVideoView.getDuration();
            mVideoSeekbar.setMax(duration);
            mDurationTv.setText(TimeUtils.str2Time(duration));

            hideMediaController();//默认隐藏
            myHandler.sendEmptyMessage(PROGRESS);
            setVideoScreenType(DEFAULT_SCREEN);//默认的屏幕播放
            mLoadingView.setVisibility(View.GONE);
        }
    }

    /**
     * 视频出错的监听
     */
    class OnVideoErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //1.播放的视频格式不支持--跳转到万能播放器继续播放
            startVitamioPlayer();
            //2.播放网络视频的时候，网络中断---1.如果网络确实断了，可以提示用于网络断了；2.网络断断续续的，重新播放
            //3.播放的时候本地文件中间有空白---下载做完成
            return true;
        }
    }

    /**
     * 视频播放完成的监听
     */
    class OnVideoCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            playNextVideo();
        }
    }

    /**
     * 手势识别监听器
     */
    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {//长按
            super.onLongPress(e);
            setStartOrPause();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {//双击
            setFullScreenOrDefault();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {//单击
            if (mIsShowMediaController) {
                hideMediaController();
                myHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
            } else {
                showMediaController();
                myHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, SHOW_MEDIA_CONTROLLER_TIME);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    /**
     * 视频进度条改变监听
     */
    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 当手指滑动的时候，会引起SeekBar进度变化，会回调这个方法
         *
         * @param seekBar
         * @param progress
         * @param fromUser 用户引起-true,不是用户引起-false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mVideoView.seekTo(progress);
            }
        }

        /**
         * 当手指触碰的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            myHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
        }

        /**
         * 当手指离开的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            myHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, SHOW_MEDIA_CONTROLLER_TIME);
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    mIsMute = false;
                } else {
                    mIsMute = true;
                }
                updataVoice(progress, mIsMute);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            myHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            myHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, SHOW_MEDIA_CONTROLLER_TIME);
        }
    }

    /**
     * 视频卡顿监听器
     */
    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，拖动卡
                    mBufferView.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END://视频卡结束了，拖动卡结束了
                    mBufferView.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }

    /**
     * 显示切换播放器对话框
     */
    private void showSwitchPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(mContext.getString(R.string.system_player_dialog_title_tip));
        builder.setMessage(mContext.getString(R.string.system_player_dialog_msg_tip));
        builder.setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel), null);
        builder.show();
    }

    /**
     * 启动Vitamio万能播放器
     */
    private void startVitamioPlayer() {
        if (null != mVideoView) {
            mVideoView.stopPlayback();
        }

        Intent intent = new Intent(this, VitamioVideoPlayerActivity.class);
        if (!ListUtils.isEmpty(mMediaItemList)) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentConstant.VIDEO_LIST, mMediaItemList);
            intent.putExtras(bundle);
            intent.putExtra(IntentConstant.POSITION, mPosition);
        } else if (null != mUri) {
            intent.setData(mUri);
        }
        startActivity(intent);
        finish();
    }

    /**
     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
     */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            mVibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            mVibrator.vibrate(pattern, -1);
        }
        getWindow().setAttributes(lp);
    }

    /**
     * 设置音量的大小
     *
     * @param progress
     * @param isMute
     */
    private void updataVoice(int progress, boolean isMute) {
        if (isMute) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            mVoiceSeekbar.setProgress(0);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            mVoiceSeekbar.setProgress(progress);
            mCurrentVoice = progress;
        }
    }

    /**
     * 播放上一个视频
     */
    private void playPreVideo() {
        if (!ListUtils.isEmpty(mMediaItemList)) {
            mPosition--;
            if (mPosition >= 0) {
                mLoadingView.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mMediaItemList.get(mPosition);
                mVideoNameTv.setText(mediaItem.name);
                mIsNetUri = CommonUtils.isNetUri(mediaItem.data);
                mVideoView.setVideoPath(mediaItem.data);
                setButtonState();
            }
        } else if (null != mUri) {
            setButtonState();
        }
    }

    /**
     * 播放下一个视频
     */
    private void playNextVideo() {
        if (!ListUtils.isEmpty(mMediaItemList)) {
            if (mPosition < mMediaItemList.size() - 1) {
                mPosition++;
                mLoadingView.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mMediaItemList.get(mPosition);
                mVideoNameTv.setText(mediaItem.name);
                mIsNetUri = CommonUtils.isNetUri(mediaItem.data);
                mVideoView.setVideoPath(mediaItem.data);
                setButtonState();
            } else {//mPosition == mMediaItemList.size() - 1
                mStartOrPauseBtn.setBackgroundResource(R.drawable.selector_start_video_btn);
            }
        } else if (null != mUri) {
            setButtonState();
        }
    }

    /**
     * 设置开始或暂停
     */
    private void setStartOrPause() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            mStartOrPauseBtn.setBackgroundResource(R.drawable.selector_start_video_btn);
        } else {
            mVideoView.start();
            mStartOrPauseBtn.setBackgroundResource(R.drawable.selector_pause_video_btn);
        }
    }

    /**
     * 设置全屏或默认屏幕
     */
    private void setFullScreenOrDefault() {
        if (mIsFullScreen) {
            setVideoScreenType(DEFAULT_SCREEN);
        } else {
            setVideoScreenType(FULL_SCREEN);
        }
    }

    /**
     * 设置按钮状态
     */
    private void setButtonState() {
        if (!ListUtils.isEmpty(mMediaItemList)) {
            if (mMediaItemList.size() == 1) {
                setBtnEnable(false);
            } else if (mMediaItemList.size() == 2) {
                if (mPosition == 0) {
                    mPreVideoBtn.setBackgroundResource(R.drawable.pre_btn_gray);
                    mPreVideoBtn.setEnabled(false);
                    mNextVideoBtn.setBackgroundResource(R.drawable.selector_next_video_btn);
                    mNextVideoBtn.setEnabled(true);
                } else if (mPosition == mMediaItemList.size() - 1) {
                    mPreVideoBtn.setBackgroundResource(R.drawable.selector_pre_video_btn);
                    mPreVideoBtn.setEnabled(true);
                    mNextVideoBtn.setBackgroundResource(R.drawable.next_btn_gray);
                    mNextVideoBtn.setEnabled(false);
                }
            } else {
                if (mPosition == 0) {
                    mPreVideoBtn.setBackgroundResource(R.drawable.pre_btn_gray);
                    mPreVideoBtn.setEnabled(false);
                } else if (mPosition == mMediaItemList.size() - 1) {
                    mNextVideoBtn.setBackgroundResource(R.drawable.next_btn_gray);
                    mNextVideoBtn.setEnabled(false);
                } else {
                    setBtnEnable(true);
                }
            }
        } else if (null != mUri) {
            setBtnEnable(false);
        }
    }

    /**
     * 设置按钮可否点击
     *
     * @param isEnable
     */
    private void setBtnEnable(boolean isEnable) {
        if (isEnable) {
            mPreVideoBtn.setBackgroundResource(R.drawable.selector_pre_video_btn);
            mNextVideoBtn.setBackgroundResource(R.drawable.selector_next_video_btn);
        } else {
            mPreVideoBtn.setBackgroundResource(R.drawable.pre_btn_gray);
            mNextVideoBtn.setBackgroundResource(R.drawable.next_btn_gray);
        }
        mPreVideoBtn.setEnabled(isEnable);
        mNextVideoBtn.setEnabled(isEnable);
    }

    /**
     * 设置视频屏幕播放类型，有全屏和默认屏幕两种
     *
     * @param screenType
     */
    private void setVideoScreenType(int screenType) {
        switch (screenType) {
            case FULL_SCREEN:
                //设置视频画面的大小-屏幕有多大就是多大
                mVideoView.setVideoSize(mScreenWidth, mVideoHeight);
                mSwitchScreenVideoBtn.setBackgroundResource(R.drawable.selector_switch_screen_default_btn);
                mIsFullScreen = true;
                break;
            case DEFAULT_SCREEN:
                //视频真实的宽和高
                int videoWidth = mVideoWidth;
                int videoHeight = mVideoHeight;
                //屏幕的宽和高
                int width = mScreenWidth;
                int height = mScreenHeight;

                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight;
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth;
                }
                mVideoView.setVideoSize(width, height);
                mSwitchScreenVideoBtn.setBackgroundResource(R.drawable.selector_switch_screen_full_btn);
                mIsFullScreen = false;
                break;
            default:
                break;
        }
    }

    /**
     * 显示控制面板
     */
    private void showMediaController() {
        mMediaControllerView.setVisibility(View.VISIBLE);
        mIsShowMediaController = true;
    }

    /**
     * 隐藏控制面板
     */
    private void hideMediaController() {
        mMediaControllerView.setVisibility(View.GONE);
        mIsShowMediaController = false;
    }

    /**
     * 设置电量变化图片
     *
     * @param level
     */
    private void setBattery(int level) {
        if (level <= 0) {
            mBatteryIv.setImageResource(R.drawable.battery_0);
        } else if (level <= 10) {
            mBatteryIv.setImageResource(R.drawable.battery_10);
        } else if (level <= 20) {
            mBatteryIv.setImageResource(R.drawable.battery_20);
        } else if (level <= 40) {
            mBatteryIv.setImageResource(R.drawable.battery_40);
        } else if (level <= 60) {
            mBatteryIv.setImageResource(R.drawable.battery_60);
        } else if (level <= 80) {
            mBatteryIv.setImageResource(R.drawable.battery_80);
        } else if (level <= 100) {
            mBatteryIv.setImageResource(R.drawable.battery_100);
        } else {
            mBatteryIv.setImageResource(R.drawable.battery_100);
        }
    }

}
