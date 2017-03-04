package com.zch.mobileplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zch.mobileplayer.pager.BasePager;

/**
 * 装Pager的Frament
 * Created by zch on 2017/3/4.
 */

public class PagerFragment extends Fragment {

    private BasePager mBasePager;

    public PagerFragment(BasePager basePager) {
        this.mBasePager = basePager;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mBasePager.mRootView;
    }
}
