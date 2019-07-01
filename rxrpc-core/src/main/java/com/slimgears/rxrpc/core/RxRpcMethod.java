package com.slimgears.rxrpc.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface RxRpcMethod {
    String value() default "";
    boolean shared() default false;
    int sharedReplayCount() default 0;
}
