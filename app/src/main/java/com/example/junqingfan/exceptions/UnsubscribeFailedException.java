package com.example.junqingfan.exceptions;

/**
 * Created by junqing.fan on 2016/3/16.
 */
public class UnsubscribeFailedException extends RuntimeException {
    public UnsubscribeFailedException(Throwable throwable) {
        super(throwable != null ? throwable : new NullPointerException());
    }

    public UnsubscribeFailedException(String message, Throwable throwable) {
        super(message, throwable != null ? throwable : new NullPointerException());
    }
}
