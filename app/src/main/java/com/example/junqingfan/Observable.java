package com.example.junqingfan;

import com.example.junqingfan.exceptions.Exceptions;
import com.example.junqingfan.internal.operators.OnSubscribeDefer;
import com.example.junqingfan.internal.operators.OnSubscribeFromIterable;
import com.example.junqingfan.plugins.RxJavaObservableExecutionHook;
import com.example.junqingfan.plugins.RxJavaPlugins;
import com.example.junqingfan.subscription.Subscriptions;

import java.util.Arrays;

import rx.functions.Action1;
import rx.functions.Func0;
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

    public final static <T> Observable<T> create(OnSubscribe<T> f) {
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

        //if(!(subscriber instanceof SafeSubscriber)){
        //    subscriber = new SafeSubscriber<T>(subscriber);
        //}

        try {
            hook.onSubscribeStart(observable,observable.onSubscribe).call(subscriber);
            return hook.onSubscribeReturn(subscriber);
        }catch (Throwable e){
            Exceptions.throwIfFatal(e);
            try {
                subscriber.onError(hook.onSubscriberError(e));
            }catch (Throwable e2){
                Exceptions.throwIfFatal(e2);
                RuntimeException r = new RuntimeException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                hook.onSubscriberError(r);
                throw r;
            }
        }

        return Subscriptions.unsubscribed();

    }

    public final static <T> Observable<T> defer(Func0<Observable<T>> observableFactory) {
        return create(new OnSubscribeDefer<T>(observableFactory));
    }


    @SuppressWarnings("unchecked")
    public final static <T> Observable<T> just(T t1, T t2, T t3, T t4, T t5) {
        return from(Arrays.asList(t1, t2, t3, t4, t5));
    }

    public final static <T> Observable<T> from(Iterable<? extends T> iterable) {
        return create(new OnSubscribeFromIterable<T>(iterable));
    }


    public final Subscription subscribe(Subscriber<? super T> subscriber) {
        return Observable.subscribe(subscriber, this);
    }

    
    
    //public final Observable<T> subscribeOn(Scheduler scheduler){
        //TODO
    //    return nest();
    //}
    
    
    //public final Observable<Observable<T>> nest(){
    //    return just(this);
    //}

    //private Observable<Observable<T>> just(Observable<T> tObservable) {
    //}

}
