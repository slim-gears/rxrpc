/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.util.TypeScriptUtils;

@AutoService(CodeGenerationFinalizer.class)
public class TypeScriptIndexGenerator implements CodeGenerationFinalizer {
    @Override
    public void generate(Context context) {
        TypeScriptUtils.writeIndex(context.environment());
    }
}
