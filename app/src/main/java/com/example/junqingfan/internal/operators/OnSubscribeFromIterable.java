package com.example.junqingfan.internal.operators;

import com.example.junqingfan.Observable;
import com.example.junqingfan.Producer;
import com.example.junqingfan.Subscriber;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import rx.internal.operators.BackpressureUtils;

/**
 * 可变数组--->迭代队列--->Observable
 * Created by junqing.fan on 2016/3/28.
 */
public final class OnSubscribeFromIterable<T> implements Observable.OnSubscribe<T> {

    final Iterable<? extends T> is;

    public OnSubscribeFromIterable(Iterable<? extends T> iterable) {
        if (iterable == null) {
            throw new NullPointerException("iterable must not be null");
        }
        this.is = iterable;
    }
    @Override
    public void call(Subscriber<? super T> o) {
        final Iterator<? extends T> it = is.iterator();
        if (!it.hasNext() && !o.isUnsubscribed())
            o.onCompleted();
        else
            o.setProducer(new IterableProducer<T>(o, it));
    }


    private static final class IterableProducer<T> extends AtomicLong implements Producer {
        /** */
        private static final long serialVersionUID = -8730475647105475802L;
        private final Subscriber<? super T> o;
        private final Iterator<? extends T> it;

        private IterableProducer(Subscriber<? super T> o, Iterator<? extends T> it) {
            this.o = o;
            this.it = it;
        }

        @Override
        public void request(long n) {
            if (get() == Long.MAX_VALUE) {
                // already started with fast-path
                return;
            }
            if (n == Long.MAX_VALUE && compareAndSet(0, Long.MAX_VALUE)) {
                fastpath();
            } else
            if (n > 0 && BackpressureUtils.getAndAddRequest(this, n) == 0L) {
                slowpath(n);
            }

        }

        /**
         * 列出需要的回调（从代码看是前几个）
         * @param n
         */
        void slowpath(long n) {
            // backpressure is requested
            final Subscriber<? super T> o = this.o;
            final Iterator<? extends T> it = this.it;

            long r = n;
            while (true) {
                /*
                 * This complicated logic is done to avoid touching the
                 * volatile `requested` value during the loop itself. If
                 * it is touched during the loop the performance is
                 * impacted significantly.
                 *
                 * 这个复杂的逻辑是用来避免loop自己触发requested（volatile类型）。
                 *
                 * 如果被loop自己触发会对执行结果有明显的影响
                 *
                 * <避免并发的时候产生问题吧>个人理解：
                 *     JVM机制：先分配内存空间，然后再去初始化。
                 *     如果将其放在循环中，可能只是分配空间，但还未初始化，程序就结束了。
                 */
                long numToEmit = r;
                while (true) {
                    if (o.isUnsubscribed()) {
                        return;
                    } else if (it.hasNext()) {
                        if (--numToEmit >= 0) {
                            o.onNext(it.next());
                        } else
                            break;
                    } else if (!o.isUnsubscribed()) {
                        o.onCompleted();
                        return;
                    } else {
                        // is unsubscribed
                        return;
                    }
                }
                r = addAndGet(-r);
                if (r == 0L) {
                    // we're done emitting the number requested so
                    // return
                    return;
                }

            }
        }

        /**
         * 列出所有的回调
         */
        void fastpath() {
            // fast-path without backpressure
            final Subscriber<? super T> o = this.o;
            final Iterator<? extends T> it = this.it;

            while (true) {
                if (o.isUnsubscribed()) {
                    return;
                } else if (it.hasNext()) {
                    o.onNext(it.next());
                } else if (!o.isUnsubscribed()) {
                    o.onCompleted();
                    return;
                } else {
                    // is unsubscribed
                    return;
                }
            }
        }
    }
}
