package com.example.junqingfan.internal.operators;

import com.example.junqingfan.Observable;
import com.example.junqingfan.Subscriber;
import com.example.junqingfan.exceptions.Exceptions;

import rx.functions.Func0;

/**
 * Created by junqing.fan on 2016/3/28.
 */
public final class OnSubscribeDefer<T> implements Observable.OnSubscribe<T> {
    final Func0<? extends Observable<? extends T>> observableFactory;

    public OnSubscribeDefer(Func0<? extends Observable<? extends T>> observableFactory) {
        this.observableFactory = observableFactory;
    }
    @Override
    public void call(Subscriber<? super T> s) {
        Observable<? extends T> o;
        try {
            o = observableFactory.call();
        } catch (Throwable t) {
            Exceptions.throwOrReport(t, s);
            return;
        }
        //o.unsafeSubscribe(Subscribers.wrap(s));
    }
}
