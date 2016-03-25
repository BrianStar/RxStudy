package com.example.junqingfan.plugins;

import rx.Scheduler;
import rx.functions.Action0;

/**
 * Created by junqing.fan on 2016/3/25.
 */
public class RxJavaSchedulersHook {
    protected RxJavaSchedulersHook() {

    }

    private final static RxJavaSchedulersHook DEFAULT_INSTANCE = new RxJavaSchedulersHook();

    public Scheduler getComputationScheduler() {
        return null;
    }

    public Scheduler getIOScheduler() {
        return null;
    }

    public Scheduler getNewThreadScheduler() {
        return null;
    }

    public Action0 onSchedule(Action0 action) {
        return action;
    }

    public static RxJavaSchedulersHook getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }
}
