package com.example.junqingfan.internal.operators;

import com.example.junqingfan.Observable;
import com.example.junqingfan.Producer;
import com.example.junqingfan.Subscriber;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import rx.internal.operators.BackpressureUtils;

/**
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
