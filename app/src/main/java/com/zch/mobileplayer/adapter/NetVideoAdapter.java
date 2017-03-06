package com.zch.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zch.mobileplayer.R;
import com.zch.mobileplayer.entity.MediaItem;
import com.zch.mobileplayer.utils.ListUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 网络视频适配器
 * Created by zch on 2017/3/4.
 */

public class NetVideoAdapter extends BaseAdapter {

    private Context mContext;
    private final ArrayList<MediaItem> mMediaItemList;

    public NetVideoAdapter(Context context, ArrayList<MediaItem> mediaItems) {
        this.mContext = context;
        this.mMediaItemList = mediaItems;
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
            convertView = View.inflate(mContext, R.layout.item_net_video, null);
            hoder = new ViewHoder(convertView);
            convertView.setTag(hoder);
        } else {
            hoder = (ViewHoder) convertView.getTag();
        }

        MediaItem mediaItem = mMediaItemList.get(position);
        if (null != mediaItem) {
            hoder.videoNameTv.setText(mediaItem.name);
            hoder.videoDescTv.setText(mediaItem.desc);
            //x.image().bind(hoder.videoIconIv, mediaItem.imageUrl);
            Glide.with(mContext).load(mediaItem.imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.video_default)
                    .error(R.drawable.video_default)
                    .into(hoder.videoIconIv);
        }

        return convertView;
    }

    static final class ViewHoder {
        @BindView(R.id.itemNet_iv_videoIcon)
        ImageView videoIconIv;
        @BindView(R.id.itemNet_tv_videoName)
        TextView videoNameTv;
        @BindView(R.id.itemNet_tv_VideoDesc)
        TextView videoDescTv;

        public ViewHoder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
