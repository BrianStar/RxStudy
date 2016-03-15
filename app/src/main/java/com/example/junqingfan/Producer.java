package com.example.junqingfan;

/**
 * Created by junqing.fan on 2016/3/15.
 */
public interface Producer {

    /**
     * 生产者请求一个确定的最大条目数，这是一种反压力请求（？），不进行这种，使用Long.MAX_VALUE。
     * @param n
     */
    void request(long n);
}
