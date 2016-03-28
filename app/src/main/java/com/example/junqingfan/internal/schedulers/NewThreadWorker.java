package com.example.junqingfan.internal.schedulers;

import com.example.junqingfan.Scheduler;
import com.example.junqingfan.Subscription;
import com.example.junqingfan.exceptions.Exceptions;
import com.example.junqingfan.internal.util.SubscriptionList;
import com.example.junqingfan.plugins.RxJavaPlugins;
import com.example.junqingfan.plugins.RxJavaSchedulersHook;
import com.example.junqingfan.subscription.CompositeSubscription;
import com.example.junqingfan.subscription.Subscriptions;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import rx.functions.Action0;
import rx.internal.util.PlatformDependent;
import rx.internal.util.RxThreadFactory;

import static rx.internal.util.PlatformDependent.ANDROID_API_VERSION_IS_NOT_ANDROID;


/**
 * Created by junqing.fan on 2016/3/28.
 */
public class NewThreadWorker extends Scheduler.Worker implements Subscription {

    private final ScheduledExecutorService executor;
    private final RxJavaSchedulersHook schedulersHook;
    volatile boolean isUnsubscribed;
    /**
     * The purge frequency in milliseconds.
     */
    private static final String FREQUENCY_KEY = "rx.scheduler.jdk6.purge-frequency-millis";
    /**
     * Force the use of purge (true/false).
     */
    private static final String PURGE_FORCE_KEY = "rx.scheduler.jdk6.purge-force";
    private static final String PURGE_THREAD_PREFIX = "RxSchedulerPurge-";
    private static final boolean SHOULD_TRY_ENABLE_CANCEL_POLICY;
    /**
     * The purge frequency in milliseconds.
     */
    public static final int PURGE_FREQUENCY;
    private static final ConcurrentHashMap<ScheduledThreadPoolExecutor, ScheduledThreadPoolExecutor> EXECUTORS;
    private static final AtomicReference<ScheduledExecutorService> PURGE;


    static {
        EXECUTORS = new ConcurrentHashMap<ScheduledThreadPoolExecutor, ScheduledThreadPoolExecutor>();
        PURGE = new AtomicReference<ScheduledExecutorService>();
        PURGE_FREQUENCY = Integer.getInteger(FREQUENCY_KEY, 1000);

        // Forces the use of purge even if setRemoveOnCancelPolicy is available
        final boolean purgeForce = Boolean.getBoolean(PURGE_FORCE_KEY);

        final int androidApiVersion = PlatformDependent.getAndroidApiVersion();

        // According to http://developer.android.com/reference/java/util/concurrent/ScheduledThreadPoolExecutor.html#setRemoveOnCancelPolicy(boolean)
        // setRemoveOnCancelPolicy available since Android API 21
        SHOULD_TRY_ENABLE_CANCEL_POLICY = !purgeForce
                && (androidApiVersion == ANDROID_API_VERSION_IS_NOT_ANDROID || androidApiVersion >= 21);
    }

    public static void registerExecutor(ScheduledThreadPoolExecutor service) {
        do {
            ScheduledExecutorService exec = PURGE.get();
            if (exec != null) {
                break;
            }
            exec = Executors.newScheduledThreadPool(1, new RxThreadFactory(PURGE_THREAD_PREFIX));
            if (PURGE.compareAndSet(null, exec)) {
                exec.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        purgeExecutors();
                    }
                }, PURGE_FREQUENCY, PURGE_FREQUENCY, TimeUnit.MILLISECONDS);

                break;
            } else {
                exec.shutdownNow();
            }
        } while (true);

        EXECUTORS.putIfAbsent(service, service);
    }

    public static void deregisterExecutor(ScheduledExecutorService service) {
        EXECUTORS.remove(service);
    }

    static void purgeExecutors() {
        try {
            Iterator<ScheduledThreadPoolExecutor> it = EXECUTORS.keySet().iterator();
            while (it.hasNext()) {
                ScheduledThreadPoolExecutor exec = it.next();
                if (!exec.isShutdown()) {
                    exec.purge();
                } else {
                    it.remove();
                }
            }
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            RxJavaPlugins.getInstance().getErrorHandler().handleError(t);
        }
    }

    private static volatile Object cachedSetRemoveOnCancelPolicyMethod;

    private static final Object SET_REMOVE_ON_CANCEL_POLICY_METHOD_NOT_SUPPORTED = new Object();

    public static boolean tryEnableCancelPolicy(ScheduledExecutorService executor) {
        if (SHOULD_TRY_ENABLE_CANCEL_POLICY) {
            final boolean isInstanceOfScheduledThreadPoolExecutor = executor instanceof ScheduledThreadPoolExecutor;

            final Method methodToCall;

            if (isInstanceOfScheduledThreadPoolExecutor) {
                final Object localSetRemoveOnCancelPolicyMethod = cachedSetRemoveOnCancelPolicyMethod;

                if (localSetRemoveOnCancelPolicyMethod == SET_REMOVE_ON_CANCEL_POLICY_METHOD_NOT_SUPPORTED) {
                    return false;
                }

                if (localSetRemoveOnCancelPolicyMethod == null) {
                    Method method = findSetRemoveOnCancelPolicyMethod(executor);

                    cachedSetRemoveOnCancelPolicyMethod = method != null
                            ? method
                            : SET_REMOVE_ON_CANCEL_POLICY_METHOD_NOT_SUPPORTED;

                    methodToCall = method;
                } else {
                    methodToCall = (Method) localSetRemoveOnCancelPolicyMethod;
                }
            } else {
                methodToCall = findSetRemoveOnCancelPolicyMethod(executor);
            }

            if (methodToCall != null) {
                try {
                    methodToCall.invoke(executor, true);
                    return true;
                } catch (Exception e) {
                    RxJavaPlugins.getInstance().getErrorHandler().handleError(e);
                }
            }
        }

        return false;
    }

    static Method findSetRemoveOnCancelPolicyMethod(ScheduledExecutorService executor) {
        // The reason for the loop is to avoid NoSuchMethodException being thrown on JDK 6
        // which is more costly than looping through ~70 methods.
        for (final Method method : executor.getClass().getMethods()) {
            if (method.getName().equals("setRemoveOnCancelPolicy")) {
                final Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length == 1 && parameterTypes[0] == Boolean.TYPE) {
                    return method;
                }
            }
        }

        return null;
    }

    /* package */
    public NewThreadWorker(ThreadFactory threadFactory) {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1, threadFactory);
        // Java 7+: cancelled future tasks can be removed from the executor thus avoiding memory leak
        boolean cancelSupported = tryEnableCancelPolicy(exec);
        if (!cancelSupported && exec instanceof ScheduledThreadPoolExecutor) {
            registerExecutor((ScheduledThreadPoolExecutor) exec);
        }
        schedulersHook = RxJavaPlugins.getInstance().getSchedulersHook();
        executor = exec;
    }


    @Override
    public Subscription schedule(final Action0 action) {
        return schedule(action, 0, null);
    }

    @Override
    public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
        if (isUnsubscribed) {
            return Subscriptions.unsubscribed();
        }
        return scheduleActual(action, delayTime, unit);
    }

    @Override
    public void unsubscribe() {
        isUnsubscribed = true;
        executor.shutdownNow();
        deregisterExecutor(executor);
    }

    @Override
    public boolean isUnsubscribed() {
        return isUnsubscribed;
    }

    public ScheduledAction scheduleActual(final Action0 action, long delayTime, TimeUnit unit) {
        Action0 decoratedAction = schedulersHook.onSchedule(action);
        ScheduledAction run = new ScheduledAction(decoratedAction);
        Future<?> f;
        if (delayTime <= 0) {
            f = executor.submit(run);
        } else {
            f = executor.schedule(run, delayTime, unit);
        }
        run.add(f);

        return run;
    }

    public ScheduledAction scheduleActual(final Action0 action, long delayTime, TimeUnit unit, CompositeSubscription parent) {
        Action0 decoratedAction = schedulersHook.onSchedule(action);
        ScheduledAction run = new ScheduledAction(decoratedAction, parent);
        parent.add(run);

        Future<?> f;
        if (delayTime <= 0) {
            f = executor.submit(run);
        } else {
            f = executor.schedule(run, delayTime, unit);
        }
        run.add(f);

        return run;
    }
    public ScheduledAction scheduleActual(final Action0 action, long delayTime, TimeUnit unit, SubscriptionList parent) {
        Action0 decoratedAction = schedulersHook.onSchedule(action);
        ScheduledAction run = new ScheduledAction(decoratedAction, parent);
        parent.add(run);

        Future<?> f;
        if (delayTime <= 0) {
            f = executor.submit(run);
        } else {
            f = executor.schedule(run, delayTime, unit);
        }
        run.add(f);

        return run;
    }

}
