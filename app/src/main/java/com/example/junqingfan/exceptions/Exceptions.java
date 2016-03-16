package com.example.junqingfan.exceptions;

import com.example.junqingfan.Observer;
import com.example.junqingfan.annotation.Experimental;

import java.util.HashSet;
import java.util.List;

/**
 * Created by junqing.fan on 2016/3/16.
 */
public final class Exceptions {
    private Exceptions() {

    }

    public static RuntimeException propagate(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new RuntimeException(t);
        }
    }

    public static void throwIfFatal(Throwable t) {
        if (t instanceof OnErrorNotImplementedException) {
            throw (OnErrorNotImplementedException) t;
        } else if (t instanceof OnErrorFailedException) {
            throw (OnErrorFailedException) t;
        } else if (t instanceof StackOverflowError) {
            throw (StackOverflowError) t;
        } else if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        } else if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        } else if (t instanceof LinkageError) {
            throw (LinkageError) t;
        }
    }

    private static final int MAX_DEPTH = 25;

    public static void addCause(Throwable e, Throwable cause) {
        HashSet<Throwable> seenCauses = new HashSet<Throwable>();
        int i = 0;
        while (e.getCause() != null) {
            if (i++ >= MAX_DEPTH) {
                return;
            }
            e = e.getCause();

            if (seenCauses.contains(e.getCause())) {
                break;
            } else {
                seenCauses.add(e.getCause());
            }
        }

        try {
            e.initCause(cause);
        } catch (Throwable t) {

        }
    }

    public static Throwable getFinalCause(Throwable e) {
        int i = 0;
        while (e.getCause() != null) {
            if (i++ >= MAX_DEPTH) {
                return new RuntimeException("Stack too deep to get final cause");
            }
            e = e.getCause();
        }
        return e;
    }

    public static void throwIfAny(List<? extends Throwable> exceptions) {
        if (exceptions != null && !exceptions.isEmpty()) {
            if (exceptions.size() == 1) {
                Throwable t = exceptions.get(0);
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new RuntimeException(t);
                }
            }

            throw new CompositeException("Multipe exception", exceptions);
        }
    }

    @Experimental
    public static void throwOrReport(Throwable t, Observer<?> o, Object value) {
        Exceptions.throwIfFatal(t);
        o.onError(t);
    }


    @Experimental
    public static void throwOrReport(Throwable t,Observer<?> o){
        Exceptions.throwIfFatal(t);
        o.onError(t);
    }


}
