package com.slimgears.rxrpc.core;

import org.reactivestreams.Publisher;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public interface RxDecorator<A extends Annotation> {
    <T> Publisher<T> decorate(A annotation, Supplier<Publisher<T>> upstream);
}
