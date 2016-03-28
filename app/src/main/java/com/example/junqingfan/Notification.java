package com.example.junqingfan;

/**
 * Created by junqing.fan on 2016/3/25.
 */
public class Notification<T> {
    private final Kind kind;
    private final Throwable throwable;
    private final T value;

    private static final Notification<Void> ON_COMPLETED = new Notification<Void>(Kind.OnCompleted, null, null);

    private Notification(Kind kind, T value, Throwable e) {
        this.value = value;
        this.throwable = e;
        this.kind = kind;
    }

    public static <T> Notification<T> createOnNext(T t) {
        return new Notification<T>(Kind.OnNext, t, null);
    }

    public static <T> Notification<T> createOnError(Throwable e) {
        return new Notification<T>(Kind.OnError, null, e);
    }

    public static <T> Notification<T> createOnCompledted() {
        return (Notification<T>) ON_COMPLETED;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public T getValue() {
        return value;
    }

    public boolean hasValue() {
        return isOnNext() && value != null;
    }

    public boolean hasThrowable() {
        return isOnError() && throwable != null;
    }

    public boolean isOnError() {
        return getKind() == Kind.OnError;
    }

    public boolean isOnNext() {
        return getKind() == Kind.OnNext;
    }

    public boolean isOnCompleted() {
        return getKind() == Kind.OnCompleted;
    }

    private Kind getKind() {
        return kind;
    }

    public void accept(Observer<? super T> observer) {
        if (isOnNext()) {
            observer.onNext(getValue());
        } else if (isOnCompleted()) {
            observer.onCompleted();
        } else if (isOnError()) {
            observer.onError(getThrowable());
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[").append(super.toString()).append(" ").append(getKind());

        if (hasValue()) {
            str.append(" ").append(getValue());
        }

        if (hasThrowable()) {
            str.append(" ").append(getThrowable().getMessage());
        }

        str.append("]");
        return str.toString();
    }

    @Override
    public int hashCode() {
        int hash = getKind().hashCode();
        if (hasValue()) {
            hash = hash * 31 + getValue().hashCode();
        }

        if (hasThrowable()) {
            hash = hash * 31 + getThrowable().hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (o.getClass() != getClass()) {
            return false;
        }

        Notification<?> notification = (Notification<?>) o;

        if (notification.getKind() != getKind()) {
            return false;
        }

        if (hasValue() && !getValue().equals(notification.getValue())) {
            return false;
        }

        if (hasThrowable() && !getThrowable().equals(notification.getThrowable())) {
            return false;
        }

        if (!hasValue() && !hasThrowable() && notification.hasThrowable()) {
            return false;
        }

        if (!hasValue() && !hasThrowable() && notification.hasValue()) {
            return false;
        }
        return true;
    }

    public enum Kind {
        OnNext, OnError, OnCompleted;
    }
}
