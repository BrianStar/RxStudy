package com.example.junqingfan;

import java.util.concurrent.TimeUnit;

import rx.functions.Action0;

/**
 * Created by junqing.fan on 2016/3/25.
 */
public abstract class Scheduler {


    /**
     * 顺序调度执行的活动在一个线程或事件（循）环中。
     * 取消订阅 work会取消所有未完成的工作并且允许所有资源清空
     */
    public abstract static class Worker implements Subscription{

        public abstract Subscription schedule(Action0 action);

        public abstract Subscription schedule(final Action0 action, final long delayTime, final TimeUnit unit);

        public Subscription schedulePeriodically(final Action0 action,long initialDelay,long period,TimeUnit unit){
            final long periodInNanos = unit.toNanos(period);
            final long startInNanos = TimeUnit.MILLISECONDS.toNanos(now()) + unit.toNanos(initialDelay);

            final com.example.junqingfan.subscription.MultipleAssignmentSubscription mas = new com.example.junqingfan.subscription.MultipleAssignmentSubscription();

            final Action0 recursiveAction = new Action0() {
                long count = 0;
                @Override
                public void call() {
                    if(!mas.isUnsubscribed()){
                        action.call();

                        long nextTick = startInNanos + (++ count * periodInNanos);
                        mas.set(schedule(this,nextTick - TimeUnit.MILLISECONDS.toNanos(now()),TimeUnit.NANOSECONDS));
                    }
                }
            };

            com.example.junqingfan.subscription.MultipleAssignmentSubscription s = new com.example.junqingfan.subscription.MultipleAssignmentSubscription();
            mas.set(s);
            s.set(schedule(recursiveAction,initialDelay,unit));
            return mas;
        }

        public long now() {
            return System.currentTimeMillis();
        }

    }

}
