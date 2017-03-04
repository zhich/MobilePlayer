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
 * 网络音频页面
 * Created by zch on 2017/3/4.
 */
public class NetAudioPager extends BasePager {

    @BindView(R.id.net_lv_data)
    ListView mDataLv;
    @BindView(R.id.net_pb_loading)
    ProgressBar mLodingPb;
    @BindView(R.id.net_tv_noData)
    TextView mNoDataTv;

    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.pager_net, null);
        ButterKnife.bind(NetAudioPager.this, view);

        return view;
    }
}
