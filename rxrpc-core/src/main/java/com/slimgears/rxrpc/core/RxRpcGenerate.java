/**
 *
 */
package com.slimgears.rxrpc.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RxRpcGenerate {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Endpoint {
        String className() default "";
        String endpointName() default "";
        Class[] params();
    }

    String endpointName() default "";
    String className() default "";
    Endpoint[] value();
}
