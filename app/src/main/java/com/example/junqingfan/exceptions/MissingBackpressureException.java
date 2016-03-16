package com.example.junqingfan.exceptions;

/**
 * Created by junqing.fan on 2016/3/16.
 */
public class MissingBackpressureException extends Exception {
    private static final long serialVersionUID = 7250870679677032194L;

    public MissingBackpressureException() {

    }

    public MissingBackpressureException(String message) {
        super(message);
    }

}
