package com.example.junqingfan.plugins;

/**
 * Created by junqing.fan on 2016/3/18.
 */
class RxJavaObservableExecutionHookDefault extends RxJavaObservableExecutionHook {

    private static RxJavaObservableExecutionHookDefault INSTANCE = new RxJavaObservableExecutionHookDefault();

    public static RxJavaObservableExecutionHook getInstance() {
        return INSTANCE;
    }
}
