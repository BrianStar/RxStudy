package com.example.junqingfan.subscription;

import com.example.junqingfan.Subscription;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by junqing.fan on 2016/3/28.
 */
public class MultipleAssignmentSubscription implements Subscription {

    final AtomicReference<State> state = new AtomicReference<State>(new State(false, Subscriptions.empty()));

    private static final class State {
        final boolean isUnsubscribed;
        final Subscription subscription;

        State(boolean u, Subscription s) {
            this.isUnsubscribed = u;
            this.subscription = s;
        }

        State unsubscribe() {
            return new State(true, subscription);
        }

        State set(Subscription s) {
            return new State(isUnsubscribed, s);
        }

    }

    @Override
    public void unsubscribe() {
        State oldState;
        State newState;
        final AtomicReference<State> localState = this.state;

        do {
            oldState = localState.get();
            if (oldState.isUnsubscribed) {
                return;
            } else {
                newState = oldState.unsubscribe();
            }
        } while (!localState.compareAndSet(oldState, newState));

        oldState.subscription.unsubscribe();
    }

    @Override
    public boolean isUnsubscribed() {
        return state.get().isUnsubscribed;
    }

    public void set(Subscription s) {
        if (s == null) {
            throw new IllegalArgumentException("Subscription can not be null");
        }

        State oldState;
        State newState;
        final AtomicReference<State> localState = this.state;

        do {
            oldState = localState.get();
            if (oldState.isUnsubscribed) {
                s.unsubscribe();
                return;
            } else {
                newState = oldState.set(s);
            }
        } while (!localState.compareAndSet(oldState, newState));
    }

    public Subscription get() {
        return state.get().subscription;
    }
}
