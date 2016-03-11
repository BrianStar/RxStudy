package com.example.junqingfan.myapplication;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.Arrays;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void test(){
        Arrays.asList( "a", "b", "d" ).forEach(e -> System.out.println( e ) );
    }
}