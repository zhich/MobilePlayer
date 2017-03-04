package com.zch.mobileplayer.pager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zch.mobileplayer.R;
import com.zch.mobileplayer.activity.SystemVideoPlayerActivity;
import com.zch.mobileplayer.adapter.LocalMediaAdapter;
import com.zch.mobileplayer.constant.IntentConstant;
import com.zch.mobileplayer.entity.MediaItem;
import com.zch.mobileplayer.utils.ListUtils;
import com.zch.mobileplayer.utils.async.Async;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * 本地视频页面
 * Created by zch on 2017/3/3.
 */
public class LocalVideoPager extends BasePager {

    @BindView(R.id.local_lv_data)
    ListView mDataLv;
    @BindView(R.id.local_pb_loading)
    ProgressBar mLodingPb;
    @BindView(R.id.local_tv_noData)
    TextView mNoDataTv;

    private ArrayList<MediaItem> mMediaItemList;
    private LocalMediaAdapter mLocalMediaAdapter;

    public LocalVideoPager(Context context) {
        super(context);
    }

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.pager_local, null);
        ButterKnife.bind(LocalVideoPager.this, view);

        return view;
    }

    @OnItemClick(R.id.local_lv_data)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, SystemVideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(IntentConstant.VIDEO_LIST, mMediaItemList);
        intent.putExtras(bundle);
        intent.putExtra(IntentConstant.POSITION, position);
        mContext.startActivity(intent);
    }

    @Override
    public void initData() {
        super.initData();

        getVideoFromLocal();

    }

    /**
     * 从本地获取视频
     */
    private void getVideoFromLocal() {
        Async.run(new Runnable() {
            @Override
            public void run() {
                mMediaItemList = new ArrayList<MediaItem>();
                ContentResolver resolver = mContext.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.ARTIST,
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

                Async.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showVideo();
                    }
                });
            }

        });
    }

    /**
     * 显示视频
     */
    private void showVideo() {
        if (!ListUtils.isEmpty(mMediaItemList)) {
            mLocalMediaAdapter = new LocalMediaAdapter(mContext, mMediaItemList, true);
            mDataLv.setAdapter(mLocalMediaAdapter);

            mNoDataTv.setVisibility(View.GONE);
        } else {
            mNoDataTv.setText(mContext.getString(R.string.tip_not_found_video));
            mNoDataTv.setVisibility(View.VISIBLE);
        }

        mLodingPb.setVisibility(View.GONE);
    }

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }

}
