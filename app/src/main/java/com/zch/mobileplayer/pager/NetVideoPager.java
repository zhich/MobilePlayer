package com.zch.mobileplayer.pager;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zch.mobileplayer.R;
import com.zch.mobileplayer.adapter.NetVideoAdapter;
import com.zch.mobileplayer.common.Api;
import com.zch.mobileplayer.entity.MediaItem;
import com.zch.mobileplayer.utils.ListUtils;
import com.zch.mobileplayer.utils.http.xutils3.MyCallBack;
import com.zch.mobileplayer.utils.http.xutils3.MyXUtils3;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 网络视频页面
 * Created by zch on 2017/3/4.
 */
public class NetVideoPager extends BasePager {

    @BindView(R.id.net_lv_data)
    ListView mDataLv;
    @BindView(R.id.net_pb_loading)
    ProgressBar mLodingPb;
    @BindView(R.id.net_tv_noData)
    TextView mNoDataTv;

    private ArrayList<MediaItem> mMediaItemList;
    private NetVideoAdapter mNetVideoAdapter;

    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.pager_net, null);
        ButterKnife.bind(NetVideoPager.this, view);

        return view;
    }

    @Override
    public void initData() {
        super.initData();

        fetchVideoFromNet();
    }

    /**
     * 从网络抓取数据
     */
    private void fetchVideoFromNet() {
        MyXUtils3.xRequest(MyXUtils3.GET, Api.NET_VIDEO_URL, null, new MyCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                dealWithVideoDatas(result);
            }
        });
    }

    /**
     * 处理视频数据
     *
     * @param result
     */
    private void dealWithVideoDatas(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            JSONObject jsonObjectItem = null;
            MediaItem mediaItem = null;
            if (null != jsonArray && jsonArray.length() >= 0) {
                mMediaItemList = new ArrayList<>();
                for (int i = 0, len = jsonArray.length(); i < len; i++) {
                    jsonObjectItem = (JSONObject) jsonArray.get(i);
                    if (null != jsonObjectItem) {
                        mediaItem = new MediaItem();

                        mediaItem.name = jsonObjectItem.optString("movieName");
                        mediaItem.desc = jsonObjectItem.optString("videoTitle");
                        mediaItem.imageUrl = jsonObjectItem.optString("coverImg");
                        mediaItem.data = jsonObjectItem.optString("hightUrl");

                        mMediaItemList.add(mediaItem);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!ListUtils.isEmpty(mMediaItemList)) {
            mNetVideoAdapter = new NetVideoAdapter(mContext, mMediaItemList);
            mDataLv.setAdapter(mNetVideoAdapter);

            mNoDataTv.setVisibility(View.GONE);
        } else {
            mNoDataTv.setText(mContext.getString(R.string.tip_not_found_video));
            mNoDataTv.setVisibility(View.VISIBLE);
        }
        mLodingPb.setVisibility(View.GONE);
    }
}
