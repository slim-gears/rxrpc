/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.slimgears.rxrpc.apt.CodeGenerationFinalizer;
import com.slimgears.rxrpc.apt.DataClassGenerator;
import com.slimgears.rxrpc.apt.EndpointGenerator;
import com.slimgears.rxrpc.apt.MetaEndpointGenerator;

import java.util.ServiceLoader;

public class ProcessingModule extends AbstractModule {
    @Override
    protected void configure() {
        ServiceLoader.load(Module.class).forEach(this::install);
        install(ServiceModules.forServiceSet(DataClassGenerator.class));
        install(ServiceModules.forServiceSet(EndpointGenerator.class));
        install(ServiceModules.forServiceSet(MetaEndpointGenerator.class));
        install(ServiceModules.forServiceSet(CodeGenerationFinalizer.class));
    }
}
