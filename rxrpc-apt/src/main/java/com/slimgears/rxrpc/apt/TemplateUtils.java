/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.slimgears.rxrpc.apt.data.TypeInfo;

import java.util.Optional;

public class TemplateUtils {
    private final static ImmutableSet<String> knownAsyncTypes = ImmutableSet.<String>builder()
            .add("io.reactivex.Observable")
            .add("io.reactivex.Single")
            .add("io.reactivex.Maybe")
            .add("io.reactivex.Completable")
            .add("java.util.concurrent.Future")
            .build();

    public boolean isKnownAsyncType(TypeInfo type) {
        return knownAsyncTypes.contains(type.name());
    }
}
