package com.example.junqingfan.subscription;

import com.example.junqingfan.Subscription;
import com.example.junqingfan.exceptions.Exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by junqing.fan on 2016/3/28.
 */
public class CompositeSubscription implements Subscription {

    private Set<Subscription> subscriptions;

    private volatile boolean unsubscribed;

    public CompositeSubscription() {
    }

    public CompositeSubscription(final Subscription... subscriptions) {
        this.subscriptions = new HashSet<Subscription>(Arrays.asList(subscriptions));
    }

    public void add(final Subscription s) {
        if (s.isUnsubscribed()) {
            return;
        }

        if (!unsubscribed) {
            synchronized (this) {
                if (!unsubscribed) {
                    if (subscriptions == null) {
                        subscriptions = new HashSet<Subscription>(4);
                    }
                    subscriptions.add(s);
                    return;
                }
            }
        }
        s.unsubscribe();
    }

    public void remove(final Subscription s) {
        if (!unsubscribed) {
            boolean unsubscribe = false;
            synchronized (this) {
                if (unsubscribe || subscriptions == null) {
                    return;
                }

                unsubscribe = subscriptions.remove(s);
            }

            if (unsubscribe) {
                s.unsubscribe();
            }
        }
    }

    public void clear() {
        if (!unsubscribed) {
            Collection<Subscription> unsubscribe = null;
            synchronized (this) {
                if (unsubscribed || subscriptions == null) {
                    return;
                } else {
                    unsubscribe = subscriptions;
                    subscriptions = null;
                }
            }
            unsubscribeFromAll(unsubscribe);
        }
    }

    private static void unsubscribeFromAll(Collection<Subscription> unsubscribes) {
        if (unsubscribes == null) {
            return;
        }

        List<Throwable> es = null;
        for (Subscription s :
                unsubscribes) {
            try {
                s.unsubscribe();
            } catch (Throwable e) {
                if (es == null) {
                    es = new ArrayList<Throwable>();
                }
                es.add(e);
            }

            Exceptions.throwIfAny(es);
        }
    }

    public boolean hasSubscriptions(){
        if (!unsubscribed) {
            synchronized (this) {
                return !unsubscribed && subscriptions != null && !subscriptions.isEmpty();
            }
        }
        return false;
    }

    @Override
    public void unsubscribe() {
        if (!unsubscribed) {
            Collection<Subscription> unsubscribe = null;
            synchronized (this) {
                if (unsubscribed) {
                    return;
                }
                unsubscribed = true;
                unsubscribe = subscriptions;
                subscriptions = null;
            }
            // we will only get here once
            unsubscribeFromAll(unsubscribe);
        }
    }

    @Override
    public boolean isUnsubscribed() {
        return unsubscribed;
    }
}
