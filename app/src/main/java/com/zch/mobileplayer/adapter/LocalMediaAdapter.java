package com.zch.mobileplayer.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zch.mobileplayer.R;
import com.zch.mobileplayer.entity.MediaItem;
import com.zch.mobileplayer.utils.ListUtils;
import com.zch.mobileplayer.utils.TimeUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 本地视频/音频适配器
 * Created by zch on 2017/3/4.
 */

public class LocalMediaAdapter extends BaseAdapter {

    private Context mContext;
    private final ArrayList<MediaItem> mMediaItemList;
    private boolean mIsVideo;

    public LocalMediaAdapter(Context context, ArrayList<MediaItem> mediaItems, boolean isVideo) {
        this.mContext = context;
        this.mMediaItemList = mediaItems;
        this.mIsVideo = isVideo;
    }

    @Override
    public int getCount() {
        return ListUtils.isEmpty(mMediaItemList) ? 0 : mMediaItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHoder hoder;
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.item_local_media, null);
            hoder = new ViewHoder(convertView);
            convertView.setTag(hoder);
        } else {
            hoder = (ViewHoder) convertView.getTag();
        }

        MediaItem mediaItem = mMediaItemList.get(position);
        if (null != mediaItem) {
            hoder.nameTv.setText(mediaItem.name);
            hoder.sizeTv.setText(Formatter.formatFileSize(mContext, mediaItem.size));
            hoder.timeTv.setText(TimeUtils.str2Time(mediaItem.duration));
            if (!mIsVideo) {
                hoder.iconIv.setImageResource(R.drawable.audio_default);
            }
        }

        return convertView;
    }

    static final class ViewHoder {
        @BindView(R.id.localMedia_iv_icon)
        ImageView iconIv;
        @BindView(R.id.localMedia_tv_name)
        TextView nameTv;
        @BindView(R.id.localMedia_tv_time)
        TextView timeTv;
        @BindView(R.id.localMedia_tv_size)
        TextView sizeTv;

        public ViewHoder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
