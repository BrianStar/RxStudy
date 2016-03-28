package com.example.junqingfan.subscription;

import com.example.junqingfan.Subscription;

import java.util.concurrent.atomic.AtomicReference;

import rx.functions.Action0;

/**
 * Created by junqing.fan on 2016/3/28.
 */
public class BooleanSubscription implements Subscription {

    final AtomicReference<Action0> actionRef;

    public BooleanSubscription() {
        this.actionRef = new AtomicReference<Action0>();
    }

    private BooleanSubscription(Action0 action) {
        this.actionRef = new AtomicReference<Action0>(action);
    }

    public static BooleanSubscription create() {
        return new BooleanSubscription();
    }

    public static BooleanSubscription create(Action0 onUnsubscribe) {
        return new BooleanSubscription(onUnsubscribe);
    }

    @Override
    public void unsubscribe() {
        Action0 action = actionRef.get();
        if (action != EMPTY_ACTION) {
            action = actionRef.getAndSet(EMPTY_ACTION);

            if(action != null && action != EMPTY_ACTION){
                action.call();
            }
        }
    }

    @Override
    public boolean isUnsubscribed() {
        return false;
    }

    static final Action0 EMPTY_ACTION = new Action0() {
        @Override
        public void call() {

        }
    };
}
