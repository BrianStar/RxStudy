package com.example.junqingfan.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by junqing.fan on 2016/3/11.
 */

@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.FIELD,
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR
})
@Documented
@Experimental
public @interface Experimental {
}
