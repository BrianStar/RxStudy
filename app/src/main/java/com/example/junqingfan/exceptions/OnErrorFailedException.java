package com.example.junqingfan.exceptions;

/**
 * Created by junqing.fan on 2016/3/16.
 */
public class OnErrorFailedException extends RuntimeException {
    private static final long serialVersionUID = -419289748403337611L;

    public OnErrorFailedException(String message, Throwable e) {
        super(message, e != null ? e : new NullPointerException());
    }

    public OnErrorFailedException(Throwable e) {
        super(e != null ? e.getMessage() : null, e != null ? e : new NullPointerException());
    }
}
