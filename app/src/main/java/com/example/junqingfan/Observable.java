package com.example.junqingfan;

import com.example.junqingfan.plugins.RxJavaObservableExecutionHook;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.SafeSubscriber;

/**
 * Created by junqing.fan on 2016/3/16.
 */
public class Observable<T> {

    final OnSubscribe<T> onSubscribe;

    protected Observable(OnSubscribe<T> f) {
        this.onSubscribe = f;
    }

    private static final RxJavaObservableExecutionHook hook = RxJavaPlugins.getInstance().getObservableExecutionHook();

    public final static <T> Observer<T> create(OnSubscribe<T> f) {
        return new Observable<T>(hook.onCreate(f));
    }


    public interface OnSubscribe<T> extends Action1<Subscriber<? super T>> {

    }

    public interface Operator<R, T> extends Func1<Subscriber<? super R>, Subscriber<? super T>> {

    }




    private static <T> Subscription subscribe(Subscriber<? super T> subscriber,Observable<T> observable){

        if (subscriber == null) {
            throw new IllegalArgumentException("observer can not be null");
        }

        if(observable.onSubscribe == null){
            throw new IllegalArgumentException("onSubscribe function can not be null");
        }

        subscriber.onStart();

        if(!(subscriber instanceof SafeSubscriber)){
            subscriber = new SafeSubscriber<T>(subscriber);
        }

        hook.onSubscribeStart()

    }
}
