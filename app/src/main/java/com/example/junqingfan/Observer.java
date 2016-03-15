package com.example.junqingfan;

/**
 * Created by junqing.fan on 2016/3/15.
 */
public interface Observer<T> {

    /**
     * 被观察者向观察者发送完成通知，如果调用过onError，此方法不会触发
     */
    void onCompleted();

    /**
     * 被观察者向观察者发送处于error状态的通知，如果调用此方法，不会调用onNext（），onCompleted()
     * @param e
     */
    void onError(Throwable e);

    /**
     * 被观察者向观察者提供观察的对象（可以这么理解）。
     * 此方法可能被回调0-N次，如果触发onError或onCompleted结束
     * @param t
     *  参数  由被观察者发射。。。
     */
    void onNext(T t);
}
