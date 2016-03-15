package com.example.junqingfan.myapplication;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    private static final Long NOT_SET = Long.MIN_VALUE;

    private long requested = NOT_SET;

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        System.out.print("测试");
        //addToRequested(100);
        //addToRequested(100);
        addToRequested(Long.MAX_VALUE);
        addToRequested(100);
        addToRequested(100);

    }

    private void addToRequested(long n) {
        if (requested == NOT_SET) {
            requested = n;
        } else {
            final long total = requested + n;
            if (total < 0) {
                requested = Long.MAX_VALUE;
            } else {
                requested = total;
            }
        }

        System.out.println("request"+ requested);
    }
}