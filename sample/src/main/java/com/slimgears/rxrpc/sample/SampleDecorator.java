package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxDecorator;
import com.slimgears.rxrpc.core.RxRpcDecorator;
import com.slimgears.util.generic.ScopedInstance;
import org.reactivestreams.Publisher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@RxRpcDecorator(SampleDecorator.Decorator.class)
public @interface SampleDecorator {
    String name();

    class Decorator implements RxDecorator<SampleDecorator> {
        private final static ScopedInstance<String> scopedString = ScopedInstance.create();

        public static String currentName() {
            return scopedString.current();
        }

        @Override
        public <T> Publisher<T> decorate(SampleDecorator annotation, Supplier<Publisher<T>> upstream) {
            return scopedString.withScope(annotation.name(), upstream::get);
        }
    }
}
