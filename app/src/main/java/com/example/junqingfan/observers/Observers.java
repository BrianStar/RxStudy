package com.example.junqingfan.observers;

import com.example.junqingfan.Observer;
import com.example.junqingfan.exceptions.OnErrorNotImplementedException;

/**
 * Created by junqing.fan on 2016/3/16.
 */
public final class Observers {
    private Observers(){
        throw new IllegalStateException("No instances!");
    }

    private static final Observer<Object> EMPTY = new Observer<Object>() {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            throw new OnErrorNotImplementedException(e);
        }

        @Override
        public void onNext(Object o) {

        }
    };

    public static <T> Observer<T> empty(){
        return (Observer<T>) EMPTY;
    }

    //public static final <T> Observer<T> create(final Action1<? super T> onNext){

    //}

}
