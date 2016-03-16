package com.example.junqingfan.exceptions;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by junqing.fan on 2016/3/16.
 */
public class OnErrorThrowable extends RuntimeException {
    private static final long serialVersionUID = -569558213262703934L;

    private final boolean hasValue;
    private final Object value;

    private OnErrorThrowable(Throwable exception) {
        super(exception);
        hasValue = false;
        this.value = null;
    }

    private OnErrorThrowable(Throwable exception, Object value) {
        super(exception);
        hasValue = true;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public boolean isValueNull() {
        return hasValue;
    }


    public static OnErrorThrowable from(Throwable t) {
        if (t == null) {
            t = new NullPointerException();
        }

        Throwable cause = Exceptions.getFinalCause(t);

        if (cause instanceof OnErrorThrowable.OnNextValue) {
            return new OnErrorThrowable(t, ((OnNextValue) cause).getValue());
        }
        return new OnErrorThrowable(t);
    }

    public static Throwable addValueAsLastCause(Throwable e, Object value) {
        if (e == null) {
            e = new NullPointerException();
        }

        Throwable lastCause = Exceptions.getFinalCause(e);
        if (lastCause != null && lastCause instanceof OnNextValue) {
            if (((OnNextValue) lastCause).getValue() == value) {
                return e;
            }
        }
        Exceptions.addCause(e, new OnNextValue(value));
        return e;
    }

    /**
     * 表示当可观察者试图发送条目或持有将来要响应的条目的时候异常发生
     */
    public static class OnNextValue extends RuntimeException {

        private static final long serialVersionUID = -3454462756050397899L;

        private static final class Primitives {
            static final Set<Class<?>> INSTANCE = creat();

            private static Set<Class<?>> creat() {
                Set<Class<?>> set = new HashSet<Class<?>>();
                set.add(Boolean.class);
                set.add(Character.class);
                set.add(Byte.class);
                set.add(Short.class);
                set.add(Integer.class);
                set.add(Long.class);
                set.add(Float.class);
                set.add(Double.class);
                return set;
            }
        }

        private final Object value;

        public OnNextValue(Object value) {
            super("OnError while emitting onNext value:" + renderValue(value));
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        static String renderValue(Object value) {
            if (value == null) {
                return "null";
            }

            if (Primitives.INSTANCE.contains(value.getClass())) {
                return value.toString();
            }

            if (value instanceof String) {
                return (String) value;
            }

            if (value instanceof Enum) {
                return ((Enum<?>) value).name();
            }

            //TODO rxjava 插件检测异常


            return value.getClass().getName() + ".class";
        }
    }
}
