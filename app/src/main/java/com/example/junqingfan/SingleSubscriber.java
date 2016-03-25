package com.example.junqingfan;

import com.example.junqingfan.annotation.Beta;
import com.example.junqingfan.internal.util.SubscriptionList;

/**
 * Created by junqing.fan on 2016/3/16.
 * <p>
 * 提供一个模板接受基本推送通知
 * 当SingleSubcriber调用Single的subscribe方法，Single调用SingleSubsriber的OnSuccess 和onError发送通知。
 */
@Beta
public abstract class SingleSubscriber<T> implements Subscription {

    private final SubscriptionList cs = new SubscriptionList();


    public abstract void onSuccess(T value);

    public abstract void onError(Throwable error);

    public void add(Subscription s){
        cs.add(s);
    };

    @Override
    public void unsubscribe() {
        cs.unsubscribe();
    }

    @Override
    public boolean isUnsubscribed() {
        return cs.isUnsubscribed();
    }
}
