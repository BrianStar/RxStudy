package com.example.junqingfan.plugins;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by junqing.fan on 2016/3/25.
 */
public class RxJavaPlugins {
    private final static RxJavaPlugins INSTANCE = new RxJavaPlugins();

    private final AtomicReference<RxJavaErrorHandler> errorHandler = new AtomicReference<RxJavaErrorHandler>();

    private final AtomicReference<RxJavaObservableExecutionHook> observableExecutionHook = new AtomicReference<RxJavaObservableExecutionHook>();

    private final AtomicReference<RxJavaSchedulersHook> schedulersHook = new AtomicReference<RxJavaSchedulersHook>();

    public static RxJavaPlugins getInstance() {
        return INSTANCE;
    }

    RxJavaPlugins() {
    }

    void reset() {
        INSTANCE.errorHandler.set(null);
        INSTANCE.observableExecutionHook.set(null);
        INSTANCE.schedulersHook.set(null);
    }

    static final RxJavaErrorHandler DEFAULT_ERROR_HANDLER = new RxJavaErrorHandler() {
    };

    public RxJavaErrorHandler getErrorHandler() {
        if (errorHandler.get() == null) {
            Object impl = getPlugininImplementationViaProperty(RxJavaErrorHandler.class, System.getProperties());

            if (impl == null) {
                errorHandler.compareAndSet(null, DEFAULT_ERROR_HANDLER);
            } else {
                errorHandler.compareAndSet(null, (RxJavaErrorHandler) impl);
            }
        }

        return errorHandler.get();
    }

    public void registerErrorHandler(RxJavaErrorHandler impl) {
        if (!errorHandler.compareAndSet(null, impl)) {
            throw new IllegalStateException("Another strategy was already registered:" + errorHandler.get());
        }
    }

    public RxJavaObservableExecutionHook getObservableExecutionHook() {
        if (observableExecutionHook.get() == null) {
            Object impl = getPlugininImplementationViaProperty(RxJavaObservableExecutionHook.class, System.getProperties());

            if (impl == null) {
                observableExecutionHook.compareAndSet(null, RxJavaObservableExecutionHookDefault.getInstance());
            } else {
                observableExecutionHook.compareAndSet(null, (RxJavaObservableExecutionHook) impl);
            }
        }
        return observableExecutionHook.get();
    }

    public void registerObservableExecutionHook(RxJavaObservableExecutionHook impl) {
        if (!observableExecutionHook.compareAndSet(null, impl)) {
            throw new IllegalStateException("Another strategy was already registered:" + observableExecutionHook.get());
        }
    }

    public RxJavaSchedulersHook getSchedulersHook() {
        if (schedulersHook.get() == null) {
            // check for an implementation from System.getProperty first
            Object impl = getPlugininImplementationViaProperty(RxJavaSchedulersHook.class, System.getProperties());
            if (impl == null) {
                // nothing set via properties so initialize with default
                schedulersHook.compareAndSet(null, RxJavaSchedulersHook.getDefaultInstance());
                // we don't return from here but call get() again in case of thread-race so the winner will always get returned
            } else {
                // we received an implementation from the system property so use it
                schedulersHook.compareAndSet(null, (RxJavaSchedulersHook) impl);
            }
        }
        return schedulersHook.get();
    }

    public void registerSchedulersHook(RxJavaSchedulersHook impl) {
        if (!schedulersHook.compareAndSet(null, impl)) {
            throw new IllegalStateException("Another strategy was already registered: " + schedulersHook.get());
        }
    }

    private Object getPlugininImplementationViaProperty(Class<?> pluginClass, Properties props) {
        final String classSimpleName = pluginClass.getSimpleName();

        final String pluginPrefix = "rxjava.plugin.";

        String defaultKey = pluginPrefix + classSimpleName + ".implementation";
        String implementingClass = props.getProperty(defaultKey);

        if (implementingClass == null) {
            final String classSuffix = ".class";
            final String implSuffix = ".impl";

            for (Map.Entry<Object, Object> e :
                    props.entrySet()) {
                String key = e.getKey().toString();
                if (key.startsWith(pluginPrefix) && key.endsWith(classSuffix)) {
                    String value = e.getValue().toString();

                    if (classSimpleName.equals(value)) {
                        String index = key.substring(0, key.length() - classSuffix.length()).substring(pluginPrefix.length());
                        String implKey = pluginPrefix + index + implSuffix;
                        implementingClass = props.getProperty(implKey);

                        if (implementingClass == null) {
                            throw new RuntimeException("Implementing class declaration for" + classSimpleName + "missing:" + implKey);
                        }
                        break;
                    }

                }
            }

        }

        if (implementingClass != null) {
            try {
                Class<?> cls = Class.forName(implementingClass);
                cls = cls.asSubclass(pluginClass);
                return cls.newInstance();
            } catch (ClassCastException e) {
                throw new RuntimeException(classSimpleName + " implementation is not an instance of " + classSimpleName + ": " + implementingClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(classSimpleName + " implementation class not found: " + implementingClass, e);
            } catch (InstantiationException e) {
                throw new RuntimeException(classSimpleName + " implementation not able to be instantiated: " + implementingClass, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(classSimpleName + " implementation not able to be accessed: " + implementingClass, e);
            }

        }
        return null;
    }
}
