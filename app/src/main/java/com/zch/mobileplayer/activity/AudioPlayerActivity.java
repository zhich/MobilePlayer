package com.zch.mobileplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zch.mobileplayer.IMusicPlayerService;
import com.zch.mobileplayer.R;
import com.zch.mobileplayer.constant.IntentConstant;
import com.zch.mobileplayer.entity.MediaItem;
import com.zch.mobileplayer.service.MusicPlayerService;
import com.zch.mobileplayer.utils.TimeUtils;
import com.zch.mobileplayer.utils.ToastUtils;
import com.zch.mobileplayer.utils.business.LyricUtils;
import com.zch.mobileplayer.view.BaseVisualizerView;
import com.zch.mobileplayer.view.LyricView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 音频播放界面
 * Created by zch on 2017/3/6.
 */
public class AudioPlayerActivity extends BaseActivity {

    private static final int PROGRESS = 1;//进度更新
    private static final int SHOW_LYRIC = 2;//显示歌词
    private static final int UPDATE_LYRIC_INTERVAL = 1000;//更新歌词时间间隔

    @BindView(R.id.audioPlayer_seekbar)
    SeekBar mAudioSeekbar;
    @BindView(R.id.audioPlayer_view_lyricView)
    LyricView mLyricView;
    @BindView(R.id.audioPlayer_tv_time)
    TextView mTimeTv;
    @BindView(R.id.audioPlayer_tv_artist)
    TextView mArtistTv;
    @BindView(R.id.audioPlayer_tv_worksName)
    TextView mWorkNameTv;
    @BindView(R.id.audioPlayer_view_baseVisualizerView)
    BaseVisualizerView mBaseVisualizerView;
    @BindView(R.id.audioPlayer_btn_playmode)
    Button mPlaymodeBtn;
    @BindView(R.id.audioPlayer_btn_startOrPause)
    Button mStartOrPauseBtn;

    private int mPosition;
    private boolean isFromNotification;//true:从状态栏进入的，不需要重新播放；false:从播放列表进入的
    private IMusicPlayerService mService;//服务的代理类，通过它可以调用服务的方法
    private Visualizer mVisualizer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);
        //设置视频的拖动
        mAudioSeekbar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());

        isFromNotification = getIntent().getBooleanExtra(IntentConstant.IS_FROM_NOTIFICATION, false);
        if (!isFromNotification) {
            mPosition = getIntent().getIntExtra(IntentConstant.POSITION, 0);
        }

        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_OPENAUDIO);
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent);//不至于实例化多个服务
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mVisualizer) {
            mVisualizer.release();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
        if (null != con) {
            unbindService(con);
            con = null;
        }
        super.onDestroy();
    }

    @OnClick({R.id.audioPlayer_btn_playmode, R.id.audioPlayer_btn_pre,
            R.id.audioPlayer_btn_startOrPause, R.id.audioPlayer_btn_next,
            R.id.audioPlayer_btn_lyrc})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audioPlayer_btn_playmode:
                setPlaymode();
                break;
            case R.id.audioPlayer_btn_pre:
                if (null != mService) {
                    try {
                        mService.pre();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.audioPlayer_btn_startOrPause:
                if (null != mService) {
                    try {
                        if (mService.isPlaying()) {
                            mService.pause();
                            mStartOrPauseBtn.setBackgroundResource(R.drawable.selector_start_audio_btn);
                        } else {
                            mService.start();
                            mStartOrPauseBtn.setBackgroundResource(R.drawable.selector_pause_audio_btn);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.audioPlayer_btn_next:
                if (null != mService) {
                    try {
                        mService.next();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.audioPlayer_btn_lyrc:
                break;
            default:
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_LYRIC://显示歌词
                    //1.得到当前的进度
                    try {
                        int currentPosition = mService.getCurrentPosition();
                        //2.把进度传入LyricView控件，并且计算该高亮哪一句
                        mLyricView.setHighligtLyric(currentPosition);
                        //3.实时的发消息
                        handler.removeMessages(SHOW_LYRIC);
                        handler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PROGRESS:
                    try {
                        //1.得到当前进度
                        int currentPosition = mService.getCurrentPosition();
                        //2.设置SeekBar.setProgress(进度)
                        mAudioSeekbar.setProgress(currentPosition);
                        //3.时间进度跟新
                        mTimeTv.setText(TimeUtils.str2Time(currentPosition) + "/" + TimeUtils.str2Time(mService.getDuration()));
                        //4.每秒更新一次
                        handler.removeMessages(PROGRESS);
                        handler.sendEmptyMessageDelayed(PROGRESS, UPDATE_LYRIC_INTERVAL);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private ServiceConnection con = new ServiceConnection() {

        /**
         * 当连接成功的时候回调这个方法
         * @param name
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            mService = IMusicPlayerService.Stub.asInterface(iBinder);

            if (null != mService) {
                try {
                    if (!isFromNotification) {//从列表
                        mService.openAudio(mPosition);
                    } else {
                        System.out.println("onServiceConnected==Thread-name==" + Thread.currentThread().getName());
                        showViewData();//从状态栏
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当断开连接的时候回调这个方法
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (null != mService) {
                    mService.stop();
                    mService = null;
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                //拖动进度
                try {
                    mService.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private void setPlaymode() {
        try {
            int playmode = mService.getPlayMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                playmode = MusicPlayerService.REPEAT_SINGLE;
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                playmode = MusicPlayerService.REPEAT_ALL;
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            } else {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }
            mService.setPlayMode(playmode);//保持

            showPlaymode();//设置图片

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showPlaymode() {
        try {
            int playmode = mService.getPlayMode();

            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                mPlaymodeBtn.setBackgroundResource(R.drawable.selector_audio_playmode_order_btn);
                ToastUtils.showToastLong(mContext, mContext.getString(R.string.order_play));
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                mPlaymodeBtn.setBackgroundResource(R.drawable.selector_audio_playmode_single_btn);
                ToastUtils.showToastLong(mContext, mContext.getString(R.string.single_play));
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                mPlaymodeBtn.setBackgroundResource(R.drawable.selector_audio_playmode_all_repeat_btn);
                ToastUtils.showToastLong(mContext, mContext.getString(R.string.all_repeat_play));
            } else {
                mPlaymodeBtn.setBackgroundResource(R.drawable.selector_audio_playmode_order_btn);
                ToastUtils.showToastLong(mContext, mContext.getString(R.string.order_play));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void showViewData() {
        try {
            mArtistTv.setText(mService.getArtist());
            mWorkNameTv.setText(mService.getName());
            mAudioSeekbar.setMax(mService.getDuration());//设置进度条的最大值
            handler.sendEmptyMessage(PROGRESS);//发消息
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showLyric() {
        //解析歌词
        LyricUtils lyricUtils = new LyricUtils();

        try {
            String path = mService.getAudioPath();//得到歌曲的绝对路径

            //传歌词文件
            //mnt/sdcard/audio/beijingbeijing.mp3
            //mnt/sdcard/audio/beijingbeijing.lrc
            path = path.substring(0, path.lastIndexOf("."));
            File file = new File(path + ".lrc");
            if (!file.exists()) {
                file = new File(path + ".txt");
            }
            lyricUtils.readLyricFile(file);//解析歌词

            mLyricView.setLyricList(lyricUtils.getLyrics());

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (lyricUtils.isExistsLyric()) {
            handler.sendEmptyMessage(SHOW_LYRIC);
        }
    }

    /**
     * 校验状态
     */
    private void checkPlaymode() {
        try {
            int playmode = mService.getPlayMode();

            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                mPlaymodeBtn.setBackgroundResource(R.drawable.selector_audio_playmode_order_btn);
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                mPlaymodeBtn.setBackgroundResource(R.drawable.selector_audio_playmode_single_btn);
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                mPlaymodeBtn.setBackgroundResource(R.drawable.selector_audio_playmode_all_repeat_btn);
            } else {
                mPlaymodeBtn.setBackgroundResource(R.drawable.selector_audio_playmode_order_btn);
            }

            //校验播放和暂停的按钮
            if (mService.isPlaying()) {
                mStartOrPauseBtn.setBackgroundResource(R.drawable.selector_start_audio_btn);
            } else {
                mStartOrPauseBtn.setBackgroundResource(R.drawable.selector_pause_audio_btn);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private void setupVisualizerFxAndUi() {
        try {
            int audioSessionid = mService.getAudioSessionId();
            mVisualizer = new Visualizer(audioSessionid);
            // 参数内必须是2的位数
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 设置允许波形表示，并且捕获它
            mBaseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void onEventMainThread(MediaItem mediaItem) {
        showLyric();
        showViewData();
        checkPlaymode();
        setupVisualizerFxAndUi();
    }
}
