package com.zch.mobileplayer.utils.http.xutils3;

import com.zch.mobileplayer.utils.LogUtils;

import org.xutils.common.Callback;

/**
 * xUtils3自定义回调类
 *
 * @author zch 2016-5-11
 */
public abstract class MyCallBack<ResultType> implements Callback.CommonCallback<ResultType> {

//    @Override
//    public void onSuccess(ResultType result) {
//        //可以根据公司的需求进行统一的请求成功的逻辑处理
//        LogUtils.i("IMyCallBack onSuccess");
//    }

    @Override
    public abstract void onSuccess(ResultType result);

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        //可以根据公司的需求进行统一的请求网络失败的逻辑处理
        LogUtils.e("IMyCallBack onError-->" + ex.getMessage());
    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }

}
