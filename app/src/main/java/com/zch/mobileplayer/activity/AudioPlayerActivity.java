package com.zch.mobileplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.zch.mobileplayer.R;

/**
 * 音频播放界面
 * Created by zch on 2017/3/6.
 */
public class AudioPlayerActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
    }
}
