/**
 *
 */
package com.slimgears.rxrpc.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Supplier;

public interface HasObjectMapper {
    Supplier<ObjectMapper> objectMapperProvider();

    default ObjectMapper objectMapper() {
        return objectMapperProvider().get();
    }

    interface Builder<B extends Builder<B>> {
        B objectMapperProvider(Supplier<ObjectMapper> mapperSupplier);

        default B objectMapper(Supplier<ObjectMapper> supplier) {
            return objectMapperProvider(ObjectMappers.fromSupplier(supplier));
        }

        default B objectMapper(ObjectMapper mapper) {
            return objectMapperProvider(() -> mapper);
        }
    }
}
