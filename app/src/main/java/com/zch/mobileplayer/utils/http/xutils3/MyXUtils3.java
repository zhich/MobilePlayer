package com.zch.mobileplayer.utils.http.xutils3;

import org.xutils.HttpManager;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

/**
 * xUtils3请求封装类
 *
 * @author zch 2016-5-11
 */
public class MyXUtils3 {

    //支持的请求的方法
    public static final String GET = "get";
    public static final String POST = "post";
    public static final String PUT = "put";
    //编码
    public static final String CHARSET = "utf-8";

    private MyXUtils3() {
    }

    /**
     * xutils请求
     *
     * @param method
     * @param url
     * @param params
     * @param cb
     * @param <T>
     * @return Helper对象
     */
    public static <T> Helper xRequest(String method, String url, HashMap<String, Object> params, Callback.CommonCallback<T> cb) {

        HttpManager httpManager = x.http();
        HttpMethod httpMethod = null;
        //目前只判断3种常用方式
        if (method.equalsIgnoreCase(POST)) {
            httpMethod = HttpMethod.POST;
        } else if (method.equalsIgnoreCase(PUT)) {
            httpMethod = HttpMethod.PUT;
        } else {
            httpMethod = HttpMethod.GET;
        }

        RequestParams requestParams = new RequestParams(url);

        //这里可以设置 token 参数之类的
        //requestParams.addBodyParameter("token", "");

        //get请求
        if (httpMethod == HttpMethod.GET && null != params && !params.isEmpty()) {
            for (Iterator i = params.keySet().iterator(); i.hasNext(); ) {
                String key = i.next().toString();
                Object value = params.get(key);
                if (null != value) {
                    requestParams.addQueryStringParameter(key, value.toString());
                }
            }
        }
        //非get请求
        if (httpMethod != HttpMethod.GET && null != params && !params.isEmpty()) {
            try {
                for (Iterator i = params.keySet().iterator(); i.hasNext(); ) {
                    String key = i.next().toString();
                    Object value = params.get(key);
                    if (value != null && value instanceof File) {
                        //上传文件
                        requestParams.setMultipart(false);//上传原始文件流
                        requestParams.addBodyParameter(key, (File) value);
                    } else if (value != null) {
                        //普通字段
                        requestParams.addBodyParameter(key, URLEncoder.encode(value.toString(), CHARSET));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        Callback.Cancelable cancelable = httpManager.request(httpMethod, requestParams, cb);
        Helper helper = new Helper();
        helper.cancelable = cancelable;
        helper.httpManager = httpManager;

        return helper;
    }


    public static class Helper {
        private Callback.Cancelable cancelable;
        private HttpManager httpManager;

        public Callback.Cancelable getCancelable() {
            return cancelable;
        }

        public HttpManager getHttpManager() {
            return httpManager;
        }
    }

}
