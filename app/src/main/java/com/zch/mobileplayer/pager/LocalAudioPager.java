package com.zch.mobileplayer.pager;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zch.mobileplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 本地音频页面
 * Created by zch on 2017/3/4.
 */
public class LocalAudioPager extends BasePager {

    @BindView(R.id.local_lv_data)
    ListView mDataLv;
    @BindView(R.id.local_pb_loading)
    ProgressBar mLodingPb;
    @BindView(R.id.local_tv_noData)
    TextView mNoDataTv;

    public LocalAudioPager(Context context) {
        super(context);
    }

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.pager_local, null);
        ButterKnife.bind(LocalAudioPager.this, view);

        return view;
    }
}
