package com.slimgears.rxrpc.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RxRpcEndpoint {
    String value() default "";
    String module() default "";
    String[] options() default {};
    boolean generateServer() default true;
    boolean generateClient() default true;
}
