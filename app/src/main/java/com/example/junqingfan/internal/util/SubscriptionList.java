package com.example.junqingfan.internal.util;

import com.example.junqingfan.Subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by junqing.fan on 2016/3/15.
 */
public final class SubscriptionList implements Subscription {

    private LinkedList<Subscription> subscriptions;

    /**
     * 共享内存变量，处理并发操作
     */
    private volatile boolean unsubscribed;

    public SubscriptionList() {
    }

    public SubscriptionList(Subscription s) {
        this.subscriptions = new LinkedList<Subscription>();
        this.subscriptions.add(s);
    }


    public SubscriptionList(final Subscription... subscriptions) {
        this.subscriptions = new LinkedList<Subscription>(Arrays.asList(subscriptions));
    }

    public void add(final Subscription s) {
        if (s.isUnsubscribed()) {
            return;
        }

        if (!unsubscribed) {
            synchronized (this) {
                LinkedList<Subscription> subs = subscriptions;
                if (subs == null) {
                    subs = new LinkedList<Subscription>();
                    subscriptions = subs;
                }
                subs.add(s);
                return;
            }
        }
        // 在结束同步锁的时候调用，所以我们在执行时，不掌控同步锁。
        s.unsubscribe();
    }


    public void remove(final Subscription s) {
        if (!unsubscribed) {
            boolean unsubscribe = false;
            synchronized (this) {
                LinkedList<Subscription> subs = subscriptions;
                if (unsubscribed || subs == null) {
                    return;
                }
                unsubscribe = subs.remove(s);
            }

            if (unsubscribe) {
                s.unsubscribe();
            }
        }
    }


    private static void unsubscribeFromAll(Collection<Subscription> subscriptions) {
        if (subscriptions == null) {
            return;
        }

        List<Throwable> es = null;
        for (Subscription s : subscriptions) {
            try {
                s.unsubscribe();
            } catch (Throwable e) {
                if (es == null) {
                    es = new ArrayList<Throwable>();
                }

                es.add(e);
            }
        }

        //TODO 抛出异常
    }

    public void clear() {
        if (!unsubscribed) {
            List<Subscription> list;
            synchronized (this) {
                list = subscriptions;
                subscriptions = null;
            }
            unsubscribeFromAll(list);
        }
    }

    public boolean hasSubscriptions() {
        if (!unsubscribed) {
            synchronized (this) {
                return !unsubscribed && subscriptions != null && !subscriptions.isEmpty();
            }
        }
        return false;
    }


    @Override
    public void unsubscribe() {
        if(!unsubscribed){
            List<Subscription> list;
            synchronized (this){
                if(unsubscribed){
                    return;
                }
                unsubscribed = true;
                list = subscriptions;
                subscriptions = null;
            }
            unsubscribeFromAll(list);
        }
    }

    @Override
    public boolean isUnsubscribed() {
        return unsubscribed;
    }
}
