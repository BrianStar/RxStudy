package com.example.junqingfan.plugins;

import com.example.junqingfan.Observable;
import com.example.junqingfan.Subscription;

/**
 * Created by junqing.fan on 2016/3/18.
 */
public abstract class RxJavaObservableExecutionHook {
    public <T> Observable.OnSubscribe<T> onCreate(Observable.OnSubscribe<T> f) {
        return f;
    }

    public <T> Observable.OnSubscribe onSubscribeStart(Observable<? extends T> observableInstance, final Observable.OnSubscribe<T> onSubsribe) {

        return onSubsribe;
    }

    public <T> Subscription onSubscribeReturn(Subscription subscription) {
        return subscription;
    }

    public <T> Throwable onSubscriberError(Throwable e) {
        return e;
    }

    public <T, R> Observable.Operator<? extends R, ? super T> onLift(final Observable.Operator<? extends R, ? super T> lift) {
        return lift;
    }
}
