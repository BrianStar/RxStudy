package com.example.junqingfan.internal.util;

import com.example.junqingfan.Observable;
import com.example.junqingfan.Subscriber;

/**
 * Created by junqing.fan on 2016/3/28.
 */
public class ScalarSynchronousObservable<T> extends Observable {


    public static final <T> ScalarSynchronousObservable<T> create(T t){
        return new ScalarSynchronousObservable<T>(t);
    }


    private final T t;

    protected ScalarSynchronousObservable(final T t) {
        super(new OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> s) {
                s.onNext(t);
                s.onCompleted();
            }
        });

        this.t = t;
    }

    public T get(){
        return t;
    }

//    public Observable<T> scalarScheduleOn(Scheduler scheduler){
//        if(){
//
//        }
//
//    }
}
