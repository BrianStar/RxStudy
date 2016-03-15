package com.example.junqingfan;

import com.example.junqingfan.internal.util.SubscriptionList;

/**
 * Created by junqing.fan on 2016/3/15.
 */
public abstract class Subscriber<T> implements Observer<T>, Subscription {

    private static final Long NOT_SET = Long.MIN_VALUE;

    private final SubscriptionList subscriptions;

    private final Subscriber<?> subscriber;

    private Producer producer;

    private long requested = NOT_SET;


    protected Subscriber() {
        this(null, false);
    }

    protected Subscriber(Subscriber<?> subscriber) {
        this(subscriber, true);
    }

    protected Subscriber(Subscriber<?> subscriber, boolean shareSubscriptions) {
        this.subscriber = subscriber;
        this.subscriptions = shareSubscriptions && subscriber != null ? subscriber.subscriptions : new SubscriptionList();
    }

    public final void add(Subscription s) {
        subscriptions.add(s);
    }


    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void unsubscribe() {
        subscriptions.unsubscribe();
    }

    @Override
    public boolean isUnsubscribed() {
        return subscriptions.isUnsubscribed();
    }

    public void onStart() {
    }

    protected final void request(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("number requested cannot be negative:" + n);
        }

        Producer producerToRequestFrom = null;
        synchronized (this) {
            if (producer != null) {
                producerToRequestFrom = producer;
            } else {
                addToRequested(n);
                return;
            }
        }

        producerToRequestFrom.request(n);
    }

    private void addToRequested(long n) {
        if (requested == NOT_SET) {
            requested = n;
        } else {
            final long total = requested + n;
            if (total < 0) {
                requested = Long.MAX_VALUE;
            } else {
                requested = total;
            }
        }
    }

    public void setProducer(Producer p) {
        long toRequest;
        boolean passToSubscriber = false;
        synchronized (this) {
            toRequest = requested;
            producer = p;
            if (subscriber != null) {
                if (toRequest == NOT_SET) {
                    passToSubscriber = true;
                }
            }
        }

        if (passToSubscriber) {
            subscriber.setProducer(producer);
        } else {
            if (toRequest == NOT_SET) {
                producer.request(Long.MAX_VALUE);
            } else {
                producer.request(toRequest);
            }
        }
    }
}
