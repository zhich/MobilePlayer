package com.zch.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.zch.mobileplayer.IMusicPlayerService;
import com.zch.mobileplayer.R;
import com.zch.mobileplayer.activity.AudioPlayerActivity;
import com.zch.mobileplayer.entity.MediaItem;
import com.zch.mobileplayer.utils.ListUtils;
import com.zch.mobileplayer.utils.SPUtils;
import com.zch.mobileplayer.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * 音乐播放器服务
 * Created by zch on 2017/3/9.
 */

public class MusicPlayerService extends Service {

    public static final int REPEAT_NORMAL = 1;//顺序播放
    public static final int REPEAT_SINGLE = 2;//单曲循环
    public static final int REPEAT_ALL = 3;//全部循环

    private static final int NOTIFICATION_ID = 1;
    private static final String PLAYMODE = "playmode";

    private int mPlaymode = REPEAT_NORMAL;//播放模式

    private ArrayList<MediaItem> mMediaItemList;
    private MediaItem mCurMediaItem;//当前播放的音频文件对象
    private int mPosition;
    private MediaPlayer mMediaPlayer;
    private NotificationManager mNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPlaymode = (int) SPUtils.getParam(PLAYMODE, REPEAT_NORMAL);
        getAudioFromLocal();
    }

    /**
     * 从本地获取音频
     */
    private void getAudioFromLocal() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                mMediaItemList = new ArrayList<MediaItem>();
                ContentResolver resolver = getApplication().getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    MediaItem mediaItem = null;
                    while (cursor.moveToNext()) {
                        mediaItem = new MediaItem();

                        mediaItem.name = cursor.getString(0);
                        mediaItem.duration = cursor.getLong(1);
                        mediaItem.size = cursor.getLong(2);
                        mediaItem.data = cursor.getString(3);
                        mediaItem.artist = cursor.getString(4);

                        mMediaItemList.add(mediaItem);
                    }
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {

        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException {
            service.setPlayMode(playmode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mMediaPlayer.seekTo(position);
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mMediaPlayer.getAudioSessionId();
        }
    };

    /**
     * 根据位置打开对应的音频文件,并且播放
     *
     * @param position
     */
    private void openAudio(int position) {
        this.mPosition = position;
        if (ListUtils.isEmpty(mMediaItemList)) {
            ToastUtils.showToastShort(getApplication(), getString(R.string.tip_get_no_data));
            return;
        }
        mCurMediaItem = mMediaItemList.get(position);
        if (null != mMediaPlayer) {
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onPrepared(MediaPlayer mp) {
                    EventBus.getDefault().post(mCurMediaItem);
                    start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    next();
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    next();
                    return true;
                }
            });
            mMediaPlayer.setDataSource(mCurMediaItem.data);
            if (mPlaymode == REPEAT_SINGLE) {
                mMediaPlayer.setLooping(true);//单曲循环播放-不会触发播放完成的回调
            } else {
                mMediaPlayer.setLooping(false);//不循环播放
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放音乐
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void start() {
        mMediaPlayer.start();

        //当播放歌曲的时候，在状态显示正在播放，点击的时候，可以进入音乐播放页面
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //最主要
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("notification", true);//标识来自状态拦
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("321音乐")
                .setContentText("正在播放:" + getName())
                .setContentIntent(pendingIntent)
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        mMediaPlayer.pause();
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * 停止音乐
     */
    private void stop() {
    }

    /**
     * 得到当前的播放进度
     *
     * @return
     */
    private int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 得到当前音频的总时长
     *
     * @return
     */
    private int getDuration() {
        return mMediaPlayer.getDuration();
    }

    /**
     * 获取艺术家
     *
     * @return
     */
    private String getArtist() {
        return mCurMediaItem.artist;
    }

    /**
     * 获取歌曲名称
     *
     * @return
     */
    private String getName() {
        return mCurMediaItem.name;
    }

    /**
     * 获取歌曲播放的路径
     *
     * @return
     */
    private String getAudioPath() {
        return mCurMediaItem.data;
    }

    /**
     * 播放下一个音频
     */
    private void next() {
        //1.根据当前的播放模式，设置下一个的位置
        setNextPosition();
        //2.根据当前的播放模式和下标位置去播放音频
        openNextAudio();
    }

    private void setNextPosition() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            mPosition++;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            mPosition++;
            if (mPosition >= mMediaItemList.size()) {
                mPosition = 0;
            }
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            mPosition++;
            if (mPosition >= mMediaItemList.size()) {
                mPosition = 0;
            }
        } else {
            mPosition++;
        }
    }

    private void openNextAudio() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            if (mPosition < mMediaItemList.size()) {
                openAudio(mPosition);
            } else {
                mPosition = mMediaItemList.size() - 1;
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            openAudio(mPosition);
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            openAudio(mPosition);
        } else {
            if (mPosition < mMediaItemList.size()) {
                openAudio(mPosition);
            } else {
                mPosition = mMediaItemList.size() - 1;
            }
        }
    }

    /**
     * 播放上一个音频
     */
    private void pre() {
        //1.根据当前的播放模式，设置上一个的位置
        setPrePosition();
        //2.根据当前的播放模式和下标位置去播放音频
        openPreAudio();
    }

    private void setPrePosition() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            mPosition--;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            mPosition--;
            if (mPosition < 0) {
                mPosition = mMediaItemList.size() - 1;
            }
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            mPosition--;
            if (mPosition < 0) {
                mPosition = mMediaItemList.size() - 1;
            }
        } else {
            mPosition--;
        }
    }

    private void openPreAudio() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            if (mPosition >= 0) {
                openAudio(mPosition);
            } else {
                mPosition = 0;
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            openAudio(mPosition);
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            openAudio(mPosition);
        } else {
            if (mPosition >= 0) {
                openAudio(mPosition);
            } else {
                mPosition = 0;
            }
        }
    }

    /**
     * 设置播放模式
     *
     * @param playmode
     */
    private void setPlayMode(int playmode) {
        this.mPlaymode = playmode;
        SPUtils.setParam(PLAYMODE, playmode);

        if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            mMediaPlayer.setLooping(true);//单曲循环播放-不会触发播放完成的回调
        } else {
            mMediaPlayer.setLooping(false); //不循环播放
        }
    }

    /**
     * 获取播放模式
     *
     * @return
     */
    private int getPlayMode() {
        return mPlaymode;
    }

    /**
     * 是否在播放音频
     *
     * @return
     */
    private boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }
}
