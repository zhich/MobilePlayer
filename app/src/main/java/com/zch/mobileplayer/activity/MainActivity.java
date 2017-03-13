package com.zch.mobileplayer.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import com.zch.mobileplayer.R;
import com.zch.mobileplayer.fragment.PagerFragment;
import com.zch.mobileplayer.pager.BasePager;
import com.zch.mobileplayer.pager.LocalAudioPager;
import com.zch.mobileplayer.pager.LocalVideoPager;
import com.zch.mobileplayer.pager.NetAudioPager;
import com.zch.mobileplayer.pager.NetVideoPager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主页面
 * Created by zch on 2017/3/3.
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.main_rg_bottomTag)
    RadioGroup mBottomTagRg;

    private int mPosition;//选中的位置

    private ArrayList<BasePager> mPagerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mPagerList = new ArrayList<>();
        mPagerList.add(new LocalVideoPager(mContext));
        mPagerList.add(new LocalAudioPager(mContext));
        mPagerList.add(new NetVideoPager(mContext));
        mPagerList.add(new NetAudioPager(mContext));

        mBottomTagRg.setOnCheckedChangeListener(new MyOnCheckChangeListener());
        mBottomTagRg.check(R.id.main_rb_localVideo);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    class MyOnCheckChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.main_rb_localVideo:
                    mPosition = 0;
                    break;
                case R.id.main_rb_localAudio:
                    mPosition = 1;
                    break;
                case R.id.main_rb_netVideo:
                    mPosition = 2;
                    break;
                case R.id.main_rb_netAudio:
                    mPosition = 3;
                    break;
                default:
                    break;
            }
            setFragment();
        }
    }

    /**
     * 把页面添加到Fragment中
     */
    private void setFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_fl_content, new PagerFragment(getBasePager()));
        transaction.commit();
    }

    /**
     * 根据位置得到对应的页面
     *
     * @return
     */
    private BasePager getBasePager() {
        BasePager basePager = mPagerList.get(mPosition);
        if (null != basePager && !basePager.mIsInitData) {
            basePager.initData();//联网请求或者绑定数据
            basePager.mIsInitData = true;
        }
        return basePager;
    }

}
