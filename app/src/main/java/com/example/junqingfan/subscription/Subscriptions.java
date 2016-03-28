package com.example.junqingfan.subscription;

import com.example.junqingfan.Subscription;
import com.example.junqingfan.exceptions.CompositeException;

import java.util.concurrent.Future;

import rx.functions.Action0;

/**
 * Created by junqing.fan on 2016/3/28.
 */
public final class Subscriptions {
    private Subscriptions() {
        throw new IllegalStateException("No instances!");
    }

    public static Subscription empty() {
        return BooleanSubscription.create();
    }

    public static Subscription unsubscribed() {
        return UNSUBSCRIBED;
    }

    public static Subscription create(final Action0 unsubscribe) {
        return BooleanSubscription.create(unsubscribe);
    }


    public static Subscription from(final Future<?> f) {
        return new FutureSubscription(f);
    }


    public static CompositeSubscription from(Subscription... subscriptions){
        return new CompositeSubscription(subscriptions);
    }

    private static final class FutureSubscription implements Subscription {
        final Future<?> f;

        public FutureSubscription(Future<?> f) {
            this.f = f;
        }

        @Override
        public void unsubscribe() {
            f.cancel(true);
        }

        @Override
        public boolean isUnsubscribed() {
            return f.isCancelled();
        }
    }


    /**
     * A {@link Subscription} that does nothing when its unsubscribe method is called.
     */
    private static final Unsubscribed UNSUBSCRIBED = new Unsubscribed();

    /**
     * Naming classes helps with debugging.
     */
    private static final class Unsubscribed implements Subscription {
        @Override
        public void unsubscribe() {
        }

        @Override
        public boolean isUnsubscribed() {
            return true;
        }
    }
}
