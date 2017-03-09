package com.zch.mobileplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.zch.mobileplayer.IMusicPlayerService;
import com.zch.mobileplayer.entity.MediaItem;

import java.util.ArrayList;

/**
 * 音乐播放器服务
 * Created by zch on 2017/3/9.
 */

public class MusicPlayerService extends Service {

    public static final int REPEAT_NORMAL = 1;//顺序播放
    public static final int REPEAT_SINGLE = 2;//单曲循环
    public static final int REPEAT_ALL = 3;//全部循环

    private int playmode = REPEAT_NORMAL;//播放模式

    private ArrayList<MediaItem> mMediaItemList;
    private MediaItem mCurMediaItem;//当前播放的音频文件对象
    private int mPosition;
    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {

        @Override
        public void openAudio(int position) throws RemoteException {

        }

        @Override
        public void start() throws RemoteException {

        }

        @Override
        public void pause() throws RemoteException {

        }

        @Override
        public void stop() throws RemoteException {

        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return 0;
        }

        @Override
        public int getDuration() throws RemoteException {
            return 0;
        }

        @Override
        public String getArtist() throws RemoteException {
            return null;
        }

        @Override
        public String getName() throws RemoteException {
            return null;
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return null;
        }

        @Override
        public void next() throws RemoteException {

        }

        @Override
        public void pre() throws RemoteException {

        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException {

        }

        @Override
        public int getPlayMode() throws RemoteException {
            return 0;
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return false;
        }

        @Override
        public void seekTo(int position) throws RemoteException {

        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return 0;
        }
    };

}
