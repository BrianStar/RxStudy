package com.example.junqingfan;

/**
 * Created by junqing.fan on 2016/3/15.
 */
public interface Subscription {
    //取消订阅
    void unsubscribe();
    //是否正在取消订阅
    boolean isUnsubscribed();
}
