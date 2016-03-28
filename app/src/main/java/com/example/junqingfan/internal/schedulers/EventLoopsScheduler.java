package com.example.junqingfan.internal.schedulers;

import com.example.junqingfan.Scheduler;
import com.example.junqingfan.Subscription;
import com.example.junqingfan.internal.util.SubscriptionList;
import com.example.junqingfan.subscription.CompositeSubscription;
import com.example.junqingfan.subscription.Subscriptions;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import rx.functions.Action0;
import rx.internal.util.RxThreadFactory;

/**
 * Created by junqing.fan on 2016/3/28.
 *
 * Tip:
 *
 * （静态变量、静态初始化块）>（变量、初始化块）>构造器
 *
 * 静态变量和静态代码块由其顺序决定先后
 */
public class EventLoopsScheduler extends Scheduler implements SchedulerLifecycle {
    private static final String THREAD_NAME_PREFIX = "RxComputationThreadPool-";
    private static final RxThreadFactory THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX);
    static final String KEY_MAX_THREADS = "rx.scheduler.max-computation-threads";
    static final int MAX_THREADS;


    static {
        int maxThreads = Integer.getInteger(KEY_MAX_THREADS, 0);
        int ncpu = Runtime.getRuntime().availableProcessors();
        int max;
        if (maxThreads <= 0 || maxThreads > ncpu) {
            max = ncpu;
        } else {
            max = maxThreads;
        }
        MAX_THREADS = max;
    }

    static final PoolWorker SHUTDOWN_WORKER;
    static {
        SHUTDOWN_WORKER = new PoolWorker(new RxThreadFactory("RxComputationShutdown-"));
        SHUTDOWN_WORKER.unsubscribe();
    }

    static final class FixedSchedulerPool {
        final int cores;

        final PoolWorker[] eventLoops;
        long n;

        FixedSchedulerPool(int maxThreads) {
            // initialize event loops
            this.cores = maxThreads;
            this.eventLoops = new PoolWorker[maxThreads];
            for (int i = 0; i < maxThreads; i++) {
                this.eventLoops[i] = new PoolWorker(THREAD_FACTORY);
            }
        }

        public PoolWorker getEventLoop() {
            int c = cores;
            if (c == 0) {
                return SHUTDOWN_WORKER;
            }
            // simple round robin, improvements to come
            return eventLoops[(int)(n++ % c)];
        }

        public void shutdown() {
            for (PoolWorker w : eventLoops) {
                w.unsubscribe();
            }
        }
    }

    /** This will indicate no pool is active. */
    static final FixedSchedulerPool NONE = new FixedSchedulerPool(0);

    final AtomicReference<FixedSchedulerPool> pool;

    /**
     * Create a scheduler with pool size equal to the available processor
     * count and using least-recent worker selection policy.
     */
    public EventLoopsScheduler() {
        this.pool = new AtomicReference<FixedSchedulerPool>(NONE);
        start();
    }


    @Override
    public void start() {
        FixedSchedulerPool update = new FixedSchedulerPool(MAX_THREADS);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }

    @Override
    public void shutdown() {
        for (;;) {
            FixedSchedulerPool curr = pool.get();
            if (curr == NONE) {
                return;
            }
            if (pool.compareAndSet(curr, NONE)) {
                curr.shutdown();
                return;
            }
        }
    }

    public Subscription scheduleDirect(Action0 action) {
        PoolWorker pw = pool.get().getEventLoop();
        return pw.scheduleActual(action, -1, TimeUnit.NANOSECONDS);
    }

    private static class EventLoopWorker extends Scheduler.Worker {
        private final SubscriptionList serial = new SubscriptionList();
        private final CompositeSubscription timed = new CompositeSubscription();
        private final SubscriptionList both = new SubscriptionList(serial, timed);
        private final PoolWorker poolWorker;

        EventLoopWorker(PoolWorker poolWorker) {
            this.poolWorker = poolWorker;

        }

        @Override
        public void unsubscribe() {
            both.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return both.isUnsubscribed();
        }

        @Override
        public Subscription schedule(Action0 action) {
            if (isUnsubscribed()) {
                return Subscriptions.unsubscribed();
            }

            return poolWorker.scheduleActual(action, 0, null, serial);
        }
        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            if (isUnsubscribed()) {
                return Subscriptions.unsubscribed();
            }

            return poolWorker.scheduleActual(action, delayTime, unit, timed);
        }
    }

    private static final class PoolWorker extends NewThreadWorker {
        PoolWorker(ThreadFactory threadFactory) {
            super(threadFactory);
        }
    }
}
