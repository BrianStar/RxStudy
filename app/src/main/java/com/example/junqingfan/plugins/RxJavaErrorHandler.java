package com.example.junqingfan.plugins;

import com.example.junqingfan.annotation.Beta;
import com.example.junqingfan.exceptions.Exceptions;

/**
 * Created by junqing.fan on 2016/3/18.
 * 定义error控制逻辑，所有的Exception都能通过此类打印，尽管onError已经忽略。
 */
public abstract class RxJavaErrorHandler {
    public void handleError(Throwable e) {
        // do nothing by default
    }

    protected static final String ERROR_IN_RENDERING_SUFFIX = ".errorRendering";

    @Beta
    public final String handleOnNextValueRendering(Object item) {

        try {
            return render(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
        }
        return item.getClass().getName() + ERROR_IN_RENDERING_SUFFIX;
    }

    @Beta
    protected String render(Object item) throws InterruptedException {
        return null;
    }
}


