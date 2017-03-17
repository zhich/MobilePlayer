package com.zch.mobileplayer.pager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zch.mobileplayer.R;
import com.zch.mobileplayer.adapter.NetAudioPagerAdapter;
import com.zch.mobileplayer.common.Api;
import com.zch.mobileplayer.entity.NetAudioPagerData;
import com.zch.mobileplayer.utils.JsonUtils;
import com.zch.mobileplayer.utils.ListUtils;
import com.zch.mobileplayer.utils.SPUtils;
import com.zch.mobileplayer.utils.http.xutils3.MyCallBack;
import com.zch.mobileplayer.utils.http.xutils3.MyXUtils3;

import java.util.List;

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

    private List<NetAudioPagerData.ListEntity> mDataList;
    private NetAudioPagerAdapter mNetAudioPagerAdapter;

    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.pager_net, null);
        ButterKnife.bind(NetAudioPager.this, view);

        return view;
    }

    @Override
    public void initData() {
        super.initData();
        fetchVideoFromNet();
    }

    private void fetchVideoFromNet() {
        String savaJson = (String) SPUtils.getParam(Api.ALL_RES_URL, "");
        if (!TextUtils.isEmpty(savaJson)) {
            dealWithAudioDatas(savaJson);
        } else {
            MyXUtils3.xRequest(MyXUtils3.GET, Api.ALL_RES_URL, null, new MyCallBack<String>() {
                @Override
                public void onSuccess(String result) {
                    SPUtils.setParam(Api.ALL_RES_URL, result);
                    dealWithAudioDatas(result);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    super.onError(ex, isOnCallback);
                    mNoDataTv.setText(mContext.getString(R.string.tip_request_error));
                    mNoDataTv.setVisibility(View.VISIBLE);
                    mLodingPb.setVisibility(View.GONE);
                }

                @Override
                public void onFinished() {
                    super.onFinished();
                }
            });
        }
    }

    private void dealWithAudioDatas(String result) {
        NetAudioPagerData data = (NetAudioPagerData) JsonUtils.fromJson(result, NetAudioPagerData.class);
        mDataList = data.getList();
        if (!ListUtils.isEmpty(mDataList)) {
            mNoDataTv.setVisibility(View.GONE);
            mNetAudioPagerAdapter = new NetAudioPagerAdapter(mContext, mDataList);
            mDataLv.setAdapter(mNetAudioPagerAdapter);
        } else {
            mNoDataTv.setText(mContext.getString(R.string.tip_get_no_data));
            mNoDataTv.setVisibility(View.VISIBLE);
        }
        mLodingPb.setVisibility(View.GONE);
    }
}
